package io.iohk.atala.prism.domain.buildingBlocks

interface Pollux {

    @Throws
    fun parseVerifiableCredential(jsonString: String): VerifiableCredential
}