package io.iohk.atala.prism.walletsdk.domain.buildingBlocks

import io.iohk.atala.prism.walletsdk.domain.models.VerifiableCredential

interface Pollux {

    @Throws // TODO: Add throw classes
    fun parseVerifiableCredential(jsonString: String): VerifiableCredential
}
