package io.iohk.atala.prism.walletsdk.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class Signature(
    val value: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Signature

        if (!value.contentEquals(other.value)) return false

        return true
    }

    override fun hashCode(): Int {
        return value.contentHashCode()
    }
}
