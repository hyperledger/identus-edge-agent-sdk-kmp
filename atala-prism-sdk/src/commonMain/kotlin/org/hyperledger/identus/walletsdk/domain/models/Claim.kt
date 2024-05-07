package org.hyperledger.identus.walletsdk.domain.models

import kotlinx.serialization.Serializable

/**
 * Represents a claim in a verifiable credential.
 *
 * @property key The key of the claim.
 * @property value The value of the claim.
 */
@Serializable
class Claim(
    val key: String,
    val value: ClaimType
)

/**
 * A sealed class representing different types of claims.
 */
@Serializable
sealed class ClaimType : Comparable<ClaimType> {
    /**
     * A data class representing a string value.
     *
     * @property value The string value.
     */
    data class StringValue(val value: String) : ClaimType()

    /**
     * Represents a boolean value as a claim type.
     *
     * @property value The boolean value.
     */
    data class BoolValue(val value: Boolean) : ClaimType()

    /**
     * Represents a data value with a byte array.
     *
     * @property value the byte array value of the data
     * @constructor Creates a [DataValue] instance with the specified byte array value.
     */
    data class DataValue(val value: ByteArray) : ClaimType() {

        /**
         * Compares this [DataValue] object with the specified object for equality. Returns `true` if the objects are the same,
         * have the same runtime class, and their values are equal.
         *
         * @param other the object to compare for equality
         * @return `true` if the objects are the same, have the same runtime class, and their values are equal, `false` otherwise
         */
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as DataValue

            return value.contentEquals(other.value)
        }

        /**
         * Generates a hash code value for the [DataValue] object.
         *
         * The hash code value is calculated by invoking the [contentHashCode] method on the [value] property.
         *
         * @return the hash code value for the [DataValue] object
         */
        override fun hashCode(): Int {
            return value.contentHashCode()
        }
    }

    /**
     * Represents a numeric value for a claim.
     *
     * @property value The numeric value.
     * @constructor Creates a new NumberValue instance with the specified value.
     */
    data class NumberValue(val value: Double) : ClaimType()

    /**
     * Compares this [ClaimType] object with the specified [other] object for order.
     *
     * @param other the object to compare with
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the
     *         specified object
     *
     * @throws IllegalArgumentException if [other] is not of the same type as this object
     * @throws IllegalArgumentException if [this] and [other] are not comparable
     */
    override fun compareTo(other: ClaimType): Int {
        return when (this) {
            is StringValue -> {
                if (other is StringValue) {
                    value.compareTo(other.value)
                } else {
                    throw IllegalArgumentException("Cannot compare different types")
                }
            }
            is NumberValue -> {
                if (other is NumberValue) {
                    value.compareTo(other.value)
                } else {
                    throw IllegalArgumentException("Cannot compare different types")
                }
            }
            else -> throw IllegalArgumentException("Cannot compare non-comparable types")
        }
    }
}
