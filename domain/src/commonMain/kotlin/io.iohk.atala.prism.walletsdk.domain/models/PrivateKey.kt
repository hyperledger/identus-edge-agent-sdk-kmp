package io.iohk.atala.prism.walletsdk.domain.models

import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@Serializable
@JsExport
data class PrivateKey(
    val keyCurve: KeyCurve,
    val value: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as PrivateKey

        if (keyCurve != other.keyCurve) return false
        if (!value.contentEquals(other.value)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = keyCurve.hashCode()
        result = 31 * result + value.contentHashCode()
        return result
    }
}
