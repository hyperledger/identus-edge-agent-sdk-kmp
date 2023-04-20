package io.iohk.atala.prism.walletsdk.prismagent.mediation

import io.iohk.atala.prism.walletsdk.domain.models.DID
import io.iohk.atala.prism.walletsdk.domain.models.Mediator
import io.iohk.atala.prism.walletsdk.domain.models.Message
import kotlinx.coroutines.flow.Flow

interface MediationHandler {
    val mediator: Mediator?
    val mediatorDID: DID

    suspend fun bootRegisteredMediator(): Mediator?

    fun achieveMediation(host: DID): Flow<Mediator>

    suspend fun updateKeyListWithDIDs(dids: Array<DID>)

    fun pickupUnreadMessages(limit: Int): Flow<Array<Pair<String, Message>>>

    suspend fun registerMessagesAsRead(ids: Array<String>)
}
