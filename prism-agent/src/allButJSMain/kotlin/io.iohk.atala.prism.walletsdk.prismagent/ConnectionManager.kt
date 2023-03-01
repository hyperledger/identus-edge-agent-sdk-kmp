package io.iohk.atala.prism.walletsdk.prismagent

import io.iohk.atala.prism.walletsdk.domain.buildingBlocks.Castor
import io.iohk.atala.prism.walletsdk.domain.buildingBlocks.Mercury
import io.iohk.atala.prism.walletsdk.domain.buildingBlocks.Pluto
import io.iohk.atala.prism.walletsdk.domain.models.DID
import io.iohk.atala.prism.walletsdk.domain.models.DIDPair
import io.iohk.atala.prism.walletsdk.domain.models.Message
import io.iohk.atala.prism.walletsdk.domain.models.PrismAgentError
import io.iohk.atala.prism.walletsdk.prismagent.connectionsManager.ConnectionsManager
import io.iohk.atala.prism.walletsdk.prismagent.mediation.MediationHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

actual class ConnectionManager : ConnectionsManager {
    private val mercury: Mercury
    private val castor: Castor
    private val pluto: Pluto
    private var pairings: MutableList<DIDPair>
    internal val mediationHandler: MediationHandler

    actual constructor(
        mercury: Mercury,
        castor: Castor,
        pluto: Pluto,
        mediationHandler: MediationHandler,
        pairings: MutableList<DIDPair>,
    ) {
        this.mercury = mercury
        this.castor = castor
        this.pluto = pluto
        this.mediationHandler = mediationHandler
        this.pairings = pairings
    }

    @Throws()
    suspend fun startMediator() {
        mediationHandler.bootRegisteredMediator()
    }

    @Throws()
    suspend fun registerMediator(host: DID) {
        mediationHandler.achieveMediation(host)
            .first()
    }

    @Throws()
    suspend fun sendMessage(message: Message): Message? {
        if (mediationHandler.mediator == null) {
            throw PrismAgentError.noMediatorAvailableError()
        }
        pluto.storeMessage(message)
        return mercury.sendMessageParseMessage(message)
    }

    @Throws()
    fun awaitMessages(): Flow<Array<Message>> {
        return mediationHandler.pickupUnreadMessages(NUMBER_OF_MESSAGES)
            .map {
                val messagesIds = it.map { it.first }.toTypedArray()
                mediationHandler.registerMessagesAsRead(messagesIds)
                it.map { it.second }.toTypedArray()
            }
            .map {
                pluto.storeMessages(it)
                it
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
