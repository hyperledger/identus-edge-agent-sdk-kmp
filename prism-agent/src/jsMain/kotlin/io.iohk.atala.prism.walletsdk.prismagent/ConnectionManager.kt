package io.iohk.atala.prism.walletsdk.prismagent

import io.iohk.atala.prism.walletsdk.domain.buildingBlocks.Castor
import io.iohk.atala.prism.walletsdk.domain.buildingBlocks.Mercury
import io.iohk.atala.prism.walletsdk.domain.buildingBlocks.Pluto
import io.iohk.atala.prism.walletsdk.domain.models.DID
import io.iohk.atala.prism.walletsdk.domain.models.Mediator
import io.iohk.atala.prism.walletsdk.domain.models.Message
import io.iohk.atala.prism.walletsdk.domain.models.PrismAgentError
import io.iohk.atala.prism.walletsdk.prismagent.mediation.MediationHandler
import kotlin.js.Promise

actual class ConnectionManager {
    private val mercury: Mercury
    private val castor: Castor
    private val pluto: Pluto
    internal val mediationHandler: MediationHandler

    actual constructor(
        mercury: Mercury,
        castor: Castor,
        pluto: Pluto,
        mediationHandler: MediationHandler,
    ) {
        this.mercury = mercury
        this.castor = castor
        this.pluto = pluto
        this.mediationHandler = mediationHandler
    }

    fun startMediator() {
        mediationHandler.bootRegisteredMediator()
    }

    fun registerMediator(host: DID): Promise<Mediator> {
        return mediationHandler.achieveMediation(host)
    }

    fun sendMessage(message: Message): Promise<Message?> {
        if (mediationHandler.mediator == null) {
            throw PrismAgentError.noMediatorAvailableError()
        }
        pluto.storeMessage(message)
        return mercury.sendMessageParseMessage(message)
    }

    fun awaitMessages(): Promise<Array<Message>> {
        return mediationHandler.pickupUnreadMessages(NUMBER_OF_MESSAGES)
            .then {
                val messagesIds = it.map { it.first }.toTypedArray()
                mediationHandler.registerMessagesAsRead(messagesIds)
                it.map { it.second }.toTypedArray()
            }
            .then {
                pluto.storeMessages(it)
                it
            }
    }

    companion object {
        const val NUMBER_OF_MESSAGES = 10
    }
}
