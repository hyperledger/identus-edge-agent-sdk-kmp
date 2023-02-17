package io.iohk.atala.prism.walletsdk.prismagent

import io.iohk.atala.prism.walletsdk.domain.buildingBlocks.Castor
import io.iohk.atala.prism.walletsdk.domain.buildingBlocks.Mercury
import io.iohk.atala.prism.walletsdk.domain.buildingBlocks.Pluto
import io.iohk.atala.prism.walletsdk.prismagent.mediation.MediationHandler

expect class ConnectionManager(
    mercury: Mercury,
    castor: Castor,
    pluto: Pluto,
    mediationHandler: MediationHandler,
)
