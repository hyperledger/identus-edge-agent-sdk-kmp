package io.iohk.atala.prism.walletsdk.prismagent

import io.iohk.atala.prism.walletsdk.domain.buildingBlocks.Apollo
import io.iohk.atala.prism.walletsdk.domain.buildingBlocks.Castor
import io.iohk.atala.prism.walletsdk.domain.buildingBlocks.Pluto
import io.iohk.atala.prism.walletsdk.domain.models.Seed
import io.iohk.atala.prism.walletsdk.prismagent.helpers.Api

expect class PrismAgent(
    apollo: Apollo,
    castor: Castor,
    pluto: Pluto,
    seed: Seed? = null,
    api: Api? = null
) {
    enum class State {
        STOPED, STARTING, RUNNING, STOPING
    }

    val seed: Seed
    var state: State
    val apollo: Apollo
    val castor: Castor
    val pluto: Pluto
}
