package io.iohk.atala.prism.walletsdk.prismagent.mediation

import io.iohk.atala.prism.walletsdk.domain.models.Mediator

interface MediatorRepository {
    fun storeMediator(mediator: Mediator)
    fun getAllMediators(): Array<Mediator>
}
