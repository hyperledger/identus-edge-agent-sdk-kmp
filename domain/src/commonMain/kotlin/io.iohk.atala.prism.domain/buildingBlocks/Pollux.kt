package io.iohk.atala.prism.domain.buildingBlocks

import io.iohk.atala.prism.domain.models.VerifiableCredential

interface Pollux {

    @Throws
    fun parseVerifiableCredential(jsonString: String): VerifiableCredential
}