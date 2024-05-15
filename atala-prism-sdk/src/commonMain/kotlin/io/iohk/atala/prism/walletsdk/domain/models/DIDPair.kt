package io.iohk.atala.prism.walletsdk.domain.models

import kotlinx.serialization.Serializable

/**
 * Represents a pair of DIDs, typically used for secure communication or delegation of capabilities or services.
 */
@Serializable
data class DIDPair(
    val host: DID,
    val receiver: DID,
    val name: String?
)
