package io.iohk.atala.prism.pluto.models

data class DIDPair(
    val holder: DID,
    val other: DID,
    val name: String? = null
)
