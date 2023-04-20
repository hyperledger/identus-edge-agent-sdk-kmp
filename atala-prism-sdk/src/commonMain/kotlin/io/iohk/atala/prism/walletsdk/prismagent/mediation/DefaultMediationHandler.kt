package io.iohk.atala.prism.walletsdk.prismagent.mediation

import io.iohk.atala.prism.apollo.uuid.UUID
import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Mercury
import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Pluto
import io.iohk.atala.prism.walletsdk.domain.models.AttachmentBase64
import io.iohk.atala.prism.walletsdk.domain.models.AttachmentDescriptor
import io.iohk.atala.prism.walletsdk.domain.models.AttachmentJsonData
import io.iohk.atala.prism.walletsdk.domain.models.DID
import io.iohk.atala.prism.walletsdk.domain.models.Mediator
import io.iohk.atala.prism.walletsdk.domain.models.Message
import io.iohk.atala.prism.walletsdk.domain.models.PrismAgentError
import io.iohk.atala.prism.walletsdk.prismagent.protocols.mediation.MediationGrant
import io.iohk.atala.prism.walletsdk.prismagent.protocols.mediation.MediationKeysUpdateList
import io.iohk.atala.prism.walletsdk.prismagent.protocols.mediation.MediationRequest
import io.iohk.atala.prism.walletsdk.prismagent.protocols.pickup.PickupDelivery
import io.iohk.atala.prism.walletsdk.prismagent.protocols.pickup.PickupReceived
import io.iohk.atala.prism.walletsdk.prismagent.protocols.pickup.PickupRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlin.jvm.Throws

final class DefaultMediationHandler(
    override val mediatorDID: DID,
    private val mercury: Mercury,
    private val store: MediatorRepository
) : MediationHandler {
    final class PlutoMediatorRepositoryImpl(private val pluto: Pluto) : MediatorRepository {
        override suspend fun getAllMediators(): List<Mediator> {
            return pluto.getAllMediators().first()
        }

        override fun storeMediator(mediator: Mediator) {
            pluto.storeMediator(mediator.mediatorDID, mediator.hostDID, mediator.routingDID)
        }
    }

    override var mediator: Mediator? = null
        private set

    init {
        this.mediator = null
    }

    @Throws(PrismAgentError.NoMediatorAvailableError::class)
    override suspend fun bootRegisteredMediator(): Mediator? {
        if (mediator == null) {
            mediator = store.getAllMediators().firstOrNull()
        }
        if (mediator == null) {
            throw PrismAgentError.NoMediatorAvailableError()
        }
        return mediator
    }

    @Throws(PrismAgentError.MediationRequestFailedError::class)
    override fun achieveMediation(host: DID): Flow<Mediator> {
        val requestMessage = MediationRequest(from = host, to = mediatorDID).makeMessage()
        return flow {
            emit(mercury.sendMessageParseMessage(message = requestMessage))
        }.map {
            val grantedMessage = it?.let { MediationGrant(it) } ?: throw PrismAgentError.MediationRequestFailedError()
            val routingDID = DID(grantedMessage.body.routingDid)
            Mediator(
                id = UUID.randomUUID4().toString(),
                mediatorDID = mediatorDID,
                hostDID = host,
                routingDID = routingDID
            )
        }
    }

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

    @Throws(PrismAgentError.NoMediatorAvailableError::class)
    override fun pickupUnreadMessages(limit: Int): Flow<Array<Pair<String, Message>>> {
        val requestMessage = mediator?.let {
            PickupRequest(
                from = it.hostDID,
                to = it.mediatorDID,
                body = PickupRequest.Body(null, limit.toString())
            ).makeMessage()
        } ?: throw PrismAgentError.NoMediatorAvailableError()

        return flow {
            emit(mercury.sendMessageParseMessage(requestMessage))
        }.map {
            val receivedMessage = it?.let { PickupDelivery(it) }
            receivedMessage?.let {
                it.attachments.mapNotNull { attachment: AttachmentDescriptor ->
                    val data = attachment.data
                    when (data) {
                        is AttachmentBase64 -> Pair(it.id, data.base64)
                        is AttachmentJsonData -> Pair(it.id, data.data)
                        else -> null
                    }
                }.map {
                    Pair(it.first, mercury.unpackMessage(it.second))
                }.toTypedArray()
            } ?: emptyArray()
        }
    }

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
}
