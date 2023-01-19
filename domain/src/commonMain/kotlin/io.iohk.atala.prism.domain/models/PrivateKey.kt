package io.iohk.atala.prism.domain.models

data class PrivateKey(
    val curve: KeyCurve,
    val value: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as PrivateKey

        if (curve != other.curve) return false
        if (!value.contentEquals(other.value)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = curve.hashCode()
        result = 31 * result + value.contentHashCode()
        return result
    }
}
