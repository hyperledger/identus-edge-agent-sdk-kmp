package io.iohk.atala.prism.walletsdk.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class DIDPair(
    val host: DID,
    val receiver: DID,
    val name: String?
)
