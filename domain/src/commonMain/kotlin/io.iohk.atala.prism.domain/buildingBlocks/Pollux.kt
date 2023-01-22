package io.iohk.atala.prism.domain.buildingBlocks

import io.iohk.atala.prism.domain.models.VerifiableCredential

interface Pollux {

    @Throws // TODO: Add throw classes
    fun parseVerifiableCredential(jsonString: String): VerifiableCredential
}
