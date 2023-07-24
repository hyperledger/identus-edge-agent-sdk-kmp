package io.iohk.atala.prism.walletsdk.domain.models

import kotlinx.serialization.Serializable

@Serializable
class Claim(
    val key: String,
    val value: ClaimType
)

@Serializable
sealed class ClaimType : Comparable<ClaimType> {
    data class StringValue(val value: String) : ClaimType()
    data class BoolValue(val value: Boolean) : ClaimType()
    data class DataValue(val value: ByteArray) : ClaimType()
    data class NumberValue(val value: Double) : ClaimType()

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
