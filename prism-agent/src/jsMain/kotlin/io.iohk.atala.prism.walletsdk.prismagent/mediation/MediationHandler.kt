package io.iohk.atala.prism.walletsdk.prismagent.mediation

import io.iohk.atala.prism.walletsdk.domain.models.DID
import io.iohk.atala.prism.walletsdk.domain.models.Mediator
import io.iohk.atala.prism.walletsdk.domain.models.Message
import io.iohk.atala.prism.walletsdk.prismagent.helpers.KMMPair
import kotlin.js.Promise

@OptIn(ExperimentalJsExport::class)
@JsExport
actual interface MediationHandler {
    actual val mediator: Mediator?
    actual val mediatorDID: DID

    fun bootRegisteredMediator(): Mediator?

    fun achieveMediation(host: DID): Promise<Mediator>

    fun updateKeyListWithDIDs(dids: Array<DID>): Promise<Boolean>

    fun pickupUnreadMessages(limit: Int): Promise<Array<KMMPair<String, Message>>>

    fun registerMessagesAsRead(ids: Array<String>): Promise<Boolean>
}
