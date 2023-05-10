package io.iohk.atala.prism.walletsdk.prismagent

import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Castor
import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Mercury
import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Pluto
import io.iohk.atala.prism.walletsdk.domain.models.DID
import io.iohk.atala.prism.walletsdk.domain.models.DIDPair
import io.iohk.atala.prism.walletsdk.domain.models.Message
import io.iohk.atala.prism.walletsdk.domain.models.PrismAgentError
import io.iohk.atala.prism.walletsdk.prismagent.connectionsmanager.ConnectionsManager
import io.iohk.atala.prism.walletsdk.prismagent.connectionsmanager.DIDCommConnection
import io.iohk.atala.prism.walletsdk.prismagent.mediation.MediationHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlin.jvm.Throws

class ConnectionManager(
    private val mercury: Mercury,
    private val castor: Castor,
    private val pluto: Pluto,
    internal val mediationHandler: MediationHandler,
    private var pairings: MutableList<DIDPair>
) : ConnectionsManager, DIDCommConnection {

    suspend fun startMediator() {
        mediationHandler.bootRegisteredMediator() ?: throw PrismAgentError.NoMediatorAvailableError()
    }

    suspend fun registerMediator(host: DID) {
        mediationHandler.achieveMediation(host).collect {
            println("Achieve mediation")
        }
    }

    @Throws(PrismAgentError.NoMediatorAvailableError::class)
    override suspend fun sendMessage(message: Message): Message? {
        if (mediationHandler.mediator == null) {
            throw PrismAgentError.NoMediatorAvailableError()
        }
        pluto.storeMessage(message)
        return mercury.sendMessageParseResponse(message)
    }

    override suspend fun awaitMessages(): Flow<Array<Pair<String, Message>>> {
        return mediationHandler.pickupUnreadMessages(NUMBER_OF_MESSAGES)
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

    override suspend fun awaitMessageResponse(id: String): Message? {
        return try {
            awaitMessages().first().map {
                it.second
            }.first {
                it.thid == id
            }
        } catch (e: NoSuchElementException) {
            null
        }
    }

    companion object {
        const val NUMBER_OF_MESSAGES = 10
    }
}
