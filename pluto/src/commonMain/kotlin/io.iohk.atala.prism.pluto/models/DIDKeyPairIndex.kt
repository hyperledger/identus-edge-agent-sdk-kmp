package io.iohk.atala.prism.pluto.models

data class DIDKeyPairIndex(
    val did: DID,
    val keyPairIndex: Int,
    val alias: String? = null
)
