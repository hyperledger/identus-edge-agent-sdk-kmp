@file:Suppress("ktlint:standard:import-ordering")

package io.iohk.atala.prism.walletsdk.prismagent.mediation

import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Mercury
import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Pluto
import io.iohk.atala.prism.walletsdk.domain.models.DID
import io.iohk.atala.prism.walletsdk.domain.models.Mediator
import io.iohk.atala.prism.walletsdk.domain.models.Message
import io.iohk.atala.prism.walletsdk.domain.models.UnknownError
import io.iohk.atala.prism.walletsdk.prismagent.PrismAgentError
import io.iohk.atala.prism.walletsdk.prismagent.protocols.ProtocolType
import io.iohk.atala.prism.walletsdk.prismagent.protocols.mediation.MediationGrant
import io.iohk.atala.prism.walletsdk.prismagent.protocols.mediation.MediationKeysUpdateList
import io.iohk.atala.prism.walletsdk.prismagent.protocols.mediation.MediationRequest
import io.iohk.atala.prism.walletsdk.prismagent.protocols.pickup.PickupReceived
import io.iohk.atala.prism.walletsdk.prismagent.protocols.pickup.PickupRequest
import io.iohk.atala.prism.walletsdk.prismagent.protocols.pickup.PickupRunner
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import java.util.UUID
import kotlinx.coroutines.isActive

/**
 * A class that provides an implementation of [MediationHandler] using a Pluto instance and a Mercury instance. It can
 * be used to register, retrieve and update mediator information, achieve mediation, pick up unread messages, and
 * register messages as read.
 */
class BasicMediatorHandler(
    override val mediatorDID: DID,
    private val mercury: Mercury,
    private val store: MediatorRepository
) : MediationHandler {

    /**
     * A class that provides an implementation of [MediatorRepository] using a [Pluto] instance.
     */
    class PlutoMediatorRepositoryImpl(private val pluto: Pluto) : MediatorRepository {

        /**
         * Fetches all the mediators from the [Pluto] store.
         *
         * @return An array of [Mediator] objects.
         */
        override suspend fun getAllMediators(): List<Mediator> {
            return pluto.getAllMediators().first()
        }

        /**
         * Stores a mediator in the [Pluto] store.
         *
         * @param mediator The [Mediator] object to store.
         */
        override fun storeMediator(mediator: Mediator) {
            pluto.storeMediator(mediator.mediatorDID, mediator.hostDID, mediator.routingDID)
        }
    }

    /**
     * The active mediator associated with the mediator handler
     */
    override var mediator: Mediator? = null
        private set

    /**
     * Boots the registered mediator associated with the mediator handler.
     *
     * @return The mediator that was booted.
     */
    override suspend fun bootRegisteredMediator(): Mediator? {
        if (mediator == null) {
            mediator = store.getAllMediators().firstOrNull()
        }

        return mediator
    }

    /**
     * Achieves mediation with the mediatorDID with the specified host DID as a user.
     *
     * @param host The DID of the entity to mediate with.
     * @return The mediator associated with the achieved mediation.
     */
    @Throws(PrismAgentError.MediationRequestFailedError::class)
    override fun achieveMediation(host: DID): Flow<Mediator> {
        return flow {
            val registeredMediator = bootRegisteredMediator()
            if (registeredMediator == null) {
                try {
                    val requestMessage =
                        MediationRequest(from = host, to = mediatorDID).makeMessage()
                    val message = mercury.sendMessageParseResponse(message = requestMessage)
                        ?: throw UnknownError.SomethingWentWrongError(
                            message = "BasicMediatorHandler => mercury.sendMessageParseResponse returned null"
                        )

                    val grantedMessage = MediationGrant(message)
                    val routingDID = DID(grantedMessage.body.routingDid)
                    val tmpMediator = Mediator(
                        id = UUID.randomUUID().toString(),
                        mediatorDID = mediatorDID,
                        hostDID = host,
                        routingDID = routingDID
                    )
                    store.storeMediator(tmpMediator)
                    mediator = tmpMediator
                    emit(tmpMediator)
                } catch (e: UnknownError) {
                    throw PrismAgentError.MediationRequestFailedError(arrayOf(e))
                }
            } else {
                emit(registeredMediator)
            }
        }
    }

    /**
     * Updates the key list with the specified DIDs.
     *
     * @param dids An array of DIDs to add to the key list.
     */
    @Throws(PrismAgentError.NoMediatorAvailableError::class)
    override suspend fun updateKeyListWithDIDs(dids: Array<DID>) {
        val keyListUpdateMessage = mediator?.let {
            MediationKeysUpdateList(
                from = it.hostDID,
                to = it.mediatorDID,
                recipientDids = dids
            ).makeMessage()
        } ?: throw PrismAgentError.NoMediatorAvailableError()
        keyListUpdateMessage.let { message -> mercury.sendMessage(message) }
    }

    /**
     * Picks up the specified number of unread messages.
     *
     * @param limit The maximum number of messages to pick up.
     * @return An array of pairs containing the message ID and the message itself.
     */
    @Throws(PrismAgentError.NoMediatorAvailableError::class)
    override fun pickupUnreadMessages(limit: Int): Flow<Array<Pair<String, Message>>> {
        val requestMessage = mediator?.let {
            PickupRequest(
                from = it.hostDID,
                to = it.mediatorDID,
                body = PickupRequest.Body(limit = limit)
            ).makeMessage()
        } ?: throw PrismAgentError.NoMediatorAvailableError()

        return flow {
            val message = mercury.sendMessageParseResponse(requestMessage)
            message?.let {
                emit(PickupRunner(message, mercury).run())
            }
        }
    }

    /**
     * Registers the specified message IDs as read.
     *
     * @param ids An array of message IDs to register as read.
     */
    @Throws(PrismAgentError.NoMediatorAvailableError::class)
    override suspend fun registerMessagesAsRead(ids: Array<String>) {
        val requestMessage = mediator?.let {
            PickupReceived(
                from = it.hostDID,
                to = it.mediatorDID,
                body = PickupReceived.Body(messageIdList = ids)
            ).makeMessage()
        } ?: throw PrismAgentError.NoMediatorAvailableError()
        mercury.sendMessage(requestMessage)
    }

    /**
     * Listens for unread messages from a specified WebSocket service endpoint.
     *
     * This function creates a WebSocket connection to the provided service endpoint URI
     * and listens for incoming messages. Upon receiving messages, it processes and
     * dispatches them to the specified callback function.
     *
     * @param serviceEndpointUri The URI of the service endpoint. It should be a valid WebSocket URI.
     * @param onMessageCallback A callback function that is invoked when a message is received.
     *                          This function is responsible for handling the incoming message.
     */
    override suspend fun listenUnreadMessages(
        serviceEndpointUri: String,
        onMessageCallback: OnMessageCallback
    ) {
        val client = HttpClient {
            install(WebSockets)
            install(HttpTimeout) {
                requestTimeoutMillis = WEBSOCKET_TIMEOUT
                connectTimeoutMillis = WEBSOCKET_TIMEOUT
                socketTimeoutMillis = WEBSOCKET_TIMEOUT
            }
        }
        if (serviceEndpointUri.contains("wss://") || serviceEndpointUri.contains("ws://")) {
            client.webSocket(serviceEndpointUri) {
                if (isActive) {
                    val liveDeliveryMessage = Message(
                        body = "{\"live_delivery\":true}",
                        piuri = ProtocolType.LiveDeliveryChange.value,
                        id = UUID.randomUUID().toString(),
                        from = mediator?.hostDID,
                        to = mediatorDID
                    )
                    val packedMessage = mercury.packMessage(liveDeliveryMessage)
                    send(Frame.Text(packedMessage))
                }
                while (isActive) {
                    try {
                        for (frame in incoming) {
                            if (frame is Frame.Text) {
                                val messages =
                                    handleReceivedMessagesFromSockets(frame.readText())
                                onMessageCallback.onMessage(messages)
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        continue
                    }
                }
            }
        }
    }

    private suspend fun handleReceivedMessagesFromSockets(text: String): Array<Pair<String, Message>> {
        val decryptedMessage = mercury.unpackMessage(text)
        if (decryptedMessage.piuri == ProtocolType.PickupStatus.value ||
            decryptedMessage.piuri == ProtocolType.PickupDelivery.value
        ) {
            return PickupRunner(decryptedMessage, mercury).run()
        } else {
            return emptyArray()
        }
    }
}

fun interface OnMessageCallback {
    fun onMessage(messages: Array<Pair<String, Message>>)
}

const val WEBSOCKET_TIMEOUT: Long = 15_000
