package io.iohk.atala.prism.walletsdk.prismagent.mediation

import io.iohk.atala.prism.walletsdk.domain.models.DID
import io.iohk.atala.prism.walletsdk.domain.models.Mediator
import io.iohk.atala.prism.walletsdk.domain.models.Message
import kotlinx.coroutines.flow.Flow

interface MediationHandler {
    val mediator: Mediator?
    val mediatorDID: DID

    @Throws()
    suspend fun bootRegisteredMediator(): Mediator?

    @Throws()
    fun achieveMediation(host: DID): Flow<Mediator>

    @Throws()
    suspend fun updateKeyListWithDIDs(dids: Array<DID>)

    @Throws()
    fun pickupUnreadMessages(limit: Int): Flow<Array<Pair<String, Message>>>

    @Throws()
    suspend fun registerMessagesAsRead(ids: Array<String>)
}
