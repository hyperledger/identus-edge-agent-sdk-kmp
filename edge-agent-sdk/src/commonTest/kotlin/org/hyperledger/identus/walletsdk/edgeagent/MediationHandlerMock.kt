package org.hyperledger.identus.walletsdk.edgeagent

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.hyperledger.identus.walletsdk.domain.models.DID
import org.hyperledger.identus.walletsdk.domain.models.Mediator
import org.hyperledger.identus.walletsdk.domain.models.Message
import org.hyperledger.identus.walletsdk.edgeagent.mediation.MediationHandler
import org.hyperledger.identus.walletsdk.edgeagent.mediation.OnMessageCallback
import java.util.UUID

class MediationHandlerMock(
    override var mediator: Mediator? = null,
    override var mediatorDID: DID = DID.testable()
) : MediationHandler {

    var bootMediatorResponse: Mediator? = null
    var achieveMediationResponse: Mediator? = null
    var pickupUnreadMessagesResponse: Array<Pair<String, Message>> = emptyArray()

    @Throws()
    override suspend fun bootRegisteredMediator(): Mediator? {
        val hostDID = DID("did", "test", "123")
        val routingDID = DID("did", "test", "123")
        bootMediatorResponse = Mediator(UUID.randomUUID().toString(), mediatorDID, hostDID, routingDID)
        mediator = bootMediatorResponse
        return bootMediatorResponse
    }

    @Throws()
    override fun achieveMediation(host: DID): Flow<Mediator> {
        mediator = achieveMediationResponse
        return flow { achieveMediationResponse?.let { emit(it) } ?: throw EdgeAgentError.NoMediatorAvailableError() }
    }

    @Throws()
    override suspend fun updateKeyListWithDIDs(dids: Array<DID>) {
    }

    @Throws()
    override fun pickupUnreadMessages(limit: Int): Flow<Array<Pair<String, Message>>> {
        return flow { emit(pickupUnreadMessagesResponse) }
    }

    @Throws()
    override suspend fun registerMessagesAsRead(ids: Array<String>) {
    }

    override suspend fun listenUnreadMessages(
        serviceEndpointUri: String,
        onMessageCallback: OnMessageCallback
    ) {
        TODO("Not yet implemented")
    }
}
