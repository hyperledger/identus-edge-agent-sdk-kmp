package io.iohk.atala.prism.domain.models

data class DIDPair(
    val host: DID,
    val receiver: DID,
    val name: String?
)
