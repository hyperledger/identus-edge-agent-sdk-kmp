package org.hyperledger.identus.walletsdk.domain.models

import kotlinx.serialization.Serializable

/**
 * Represents a public key with a specific key curve and value.
 */
@Serializable
data class PublicKey(
    val curve: KeyCurve,
    val value: ByteArray
) {
    /**
     * Checks whether the current [PublicKey] object is equal to the specified [other] object.
     *
     * Two [PublicKey] objects are considered equal if they have the same key curve and value.
     *
     * @param other The object to compare for equality.
     * @return `true` if the objects are equal, `false` otherwise.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as PublicKey

        if (curve != other.curve) return false
        if (!value.contentEquals(other.value)) return false

        return true
    }

    /**
     * Computes the hash code value for this [PublicKey] object.
     *
     * The hash code is computed based on the key curve and value of the [PublicKey] object.
     * Two [PublicKey] objects that are equal according to the [equals] method will have the same hash code.
     *
     * @return The hash code value for this [PublicKey] object.
     */
    override fun hashCode(): Int {
        var result = curve.hashCode()
        result = 31 * result + value.contentHashCode()
        return result
    }
}

/**
 * Represents a compressed public key and its uncompressed version.
 */
@Serializable
data class CompressedPublicKey(
    val uncompressed: PublicKey,
    val value: ByteArray
) {
    /**
     * Checks whether the current [CompressedPublicKey] object is equal to the specified [other] object.
     *
     * Two [CompressedPublicKey] objects are considered equal if they have the same uncompressed key and value.
     *
     * @param other The object to compare for equality.
     * @return `true` if the objects are equal, `false` otherwise.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as CompressedPublicKey

        if (uncompressed != other.uncompressed) return false
        if (!value.contentEquals(other.value)) return false

        return true
    }

    /**
     * Computes the hash code value for this [CompressedPublicKey] object.
     *
     * The hash code is computed based on the uncompressed key and value of the [CompressedPublicKey] object.
     * Two [CompressedPublicKey] objects that are equal according to the [equals] method will have the same hash code.
     *
     * @return The hash code value for this [CompressedPublicKey] object.
     */
    override fun hashCode(): Int {
        var result = uncompressed.hashCode()
        result = 31 * result + value.contentHashCode()
        return result
    }
}
