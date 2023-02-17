package io.iohk.atala.prism.walletsdk.domain.buildingBlocks

import io.iohk.atala.prism.walletsdk.domain.models.VerifiableCredential

interface Pollux {
    val castor: Castor
    @Throws // TODO: Add throw classes
    fun parseVerifiableCredential(jwtString: String): VerifiableCredential
}
