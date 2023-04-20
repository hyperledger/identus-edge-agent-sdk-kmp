package io.iohk.atala.prism.walletsdk.domain.buildingblocks

import io.iohk.atala.prism.walletsdk.domain.models.VerifiableCredential

interface Pollux {
    val castor: Castor

    fun parseVerifiableCredential(jwtString: String): VerifiableCredential
}
