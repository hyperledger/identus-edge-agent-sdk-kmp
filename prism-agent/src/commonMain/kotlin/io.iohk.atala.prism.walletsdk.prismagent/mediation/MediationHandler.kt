package io.iohk.atala.prism.walletsdk.prismagent.mediation

import io.iohk.atala.prism.walletsdk.domain.models.DID
import io.iohk.atala.prism.walletsdk.domain.models.Mediator

expect interface MediationHandler {
    val mediator: Mediator?
    val mediatorDID: DID
}
