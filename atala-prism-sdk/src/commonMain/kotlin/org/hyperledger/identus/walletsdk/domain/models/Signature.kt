package org.hyperledger.identus.walletsdk.domain.models

import kotlinx.serialization.Serializable

/**
 * Represents a digital signature.
 */
@Serializable
data class Signature(
    val value: ByteArray
) {
    /**
     * Check whether this Signature is equal to the specified object.
     *
     * @param other the object to compare with
     * @return true if the given object is a Signature and has the same content as this Signature, false otherwise
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Signature

        return value.contentEquals(other.value)
    }

    /**
     * Calculates the hash code value of the Signature object.
     *
     * @return the hash code value of the Signature object
     */
    override fun hashCode(): Int {
        return value.contentHashCode()
    }
}
