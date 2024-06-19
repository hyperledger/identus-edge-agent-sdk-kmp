package org.hyperledger.identus.walletsdk.domain.models

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

/**
 * Represents a pair of DIDs, typically used for secure communication or delegation of capabilities or services.
 */
@Serializable
@OptIn(ExperimentalSerializationApi::class)
data class DIDPair(
    @JsonNames("host")
    val holder: DID,
    val receiver: DID,
    val name: String?
)
