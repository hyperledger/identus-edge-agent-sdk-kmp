package io.iohk.atala.prism.domain.models

data class DIDPair(
    val holder: DID,
    val other: DID,
    val name: String?
)
