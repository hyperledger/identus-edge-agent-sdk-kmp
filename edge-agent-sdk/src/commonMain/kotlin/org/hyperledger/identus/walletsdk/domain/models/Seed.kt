package org.hyperledger.identus.walletsdk.domain.models

import kotlinx.serialization.Serializable

/**
 * Represents a seed used for key generation.
 */
@Serializable
data class Seed(
    val value: ByteArray
) {
    /**
     * Compares this Seed object to the specified [other] object for equality.
     *
     * Two Seed objects are considered equal if they have the same [value] property.
     *
     * @param other The object to compare for equality.
     * @return `true` if the objects are equal, `false` otherwise.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Seed

        return value.contentEquals(other.value)
    }

    /**
     *
     */
    override fun hashCode(): Int {
        return value.contentHashCode()
    }
}
