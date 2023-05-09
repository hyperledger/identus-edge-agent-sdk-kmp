package io.iohk.atala.prism.walletsdk.prismagent

import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Castor
import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Mercury
import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Pluto
import io.iohk.atala.prism.walletsdk.domain.models.DID
import io.iohk.atala.prism.walletsdk.domain.models.DIDPair
import io.iohk.atala.prism.walletsdk.domain.models.Message
import io.iohk.atala.prism.walletsdk.domain.models.PrismAgentError
import io.iohk.atala.prism.walletsdk.prismagent.connectionsmanager.ConnectionsManager
import io.iohk.atala.prism.walletsdk.prismagent.mediation.MediationHandler
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlin.jvm.Throws

class ConnectionManager(
    private val mercury: Mercury,
    private val castor: Castor,
    private val pluto: Pluto,
    internal val mediationHandler: MediationHandler,
    private var pairings: MutableList<DIDPair>
) : ConnectionsManager {

    suspend fun startMediator() {
        mediationHandler.bootRegisteredMediator() ?: throw PrismAgentError.NoMediatorAvailableError()
    }

    suspend fun registerMediator(host: DID) {
        mediationHandler.achieveMediation(host).collect {
            println("Achieve mediation")
        }
    }

    @Throws(PrismAgentError.NoMediatorAvailableError::class)
    suspend fun sendMessage(message: Message): Message? {
        if (mediationHandler.mediator == null) {
            throw PrismAgentError.NoMediatorAvailableError()
        }
        pluto.storeMessage(message)
        return mercury.sendMessageParseResponse(message)
    }

    suspend fun awaitMessages() {
        mediationHandler.pickupUnreadMessages(NUMBER_OF_MESSAGES)
            .collect { array ->
                val messagesIds = mutableListOf<String>()
                val messages = mutableListOf<Message>()
                array.map { pair ->
                    messagesIds.add(pair.first)
                    messages.add(pair.second)
                }
                if (messagesIds.isNotEmpty()) {
                    mediationHandler.registerMessagesAsRead(messagesIds.toTypedArray())
                    pluto.storeMessages(messages)
                }
            }
    }

    override suspend fun addConnection(paired: DIDPair) {
        if (pairings.contains(paired)) return
        pluto.storeDIDPair(paired.host, paired.receiver, paired.name ?: "")
        pairings.add(paired)
    }

    override suspend fun removeConnection(pair: DIDPair): DIDPair? {
        val index = pairings.indexOf(pair)
        if (index > -1) {
            pairings.removeAt(index)
        }
        return null
    }

    companion object {
        const val NUMBER_OF_MESSAGES = 10
    }
}
