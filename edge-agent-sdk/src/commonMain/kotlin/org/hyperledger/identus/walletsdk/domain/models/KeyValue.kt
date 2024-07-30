package org.hyperledger.identus.walletsdk.domain.models

import kotlinx.serialization.Serializable

/**
 * Represents a key-value pair.
 *
 * @param key The key.
 * @param value The value.
 */
@Serializable
data class KeyValue(
    val key: String,
    val value: String
)
