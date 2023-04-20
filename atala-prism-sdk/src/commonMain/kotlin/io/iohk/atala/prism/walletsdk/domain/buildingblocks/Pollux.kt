package io.iohk.atala.prism.walletsdk.domain.buildingblocks

import io.iohk.atala.prism.walletsdk.domain.models.VerifiableCredential

/**
 * TODO(Clarify what Castor methods stand for)
 * TODO(Add method documentations)
 */
interface Pollux {
    val castor: Castor

    fun parseVerifiableCredential(jwtString: String): VerifiableCredential
}
