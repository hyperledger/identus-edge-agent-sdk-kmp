package io.iohk.atala.prism.walletsdk.domain.models.keyManagement

import io.iohk.atala.prism.apollo.base64.base64UrlEncoded
import io.iohk.atala.prism.walletsdk.domain.models.ApolloError
import io.iohk.atala.prism.walletsdk.domain.models.Curve
import io.iohk.atala.prism.walletsdk.domain.models.KeyCurve

abstract class Key {
    abstract val type: KeyTypes
    abstract val keySpecification: MutableMap<String, String>
    abstract val size: Int
    abstract val raw: ByteArray

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Key

        if (!raw.contentEquals(other.raw)) {
            return false
        }
        if (!keySpecification.containsKey(CurveKey().property) ||
            keySpecification[CurveKey().property] != other.getProperty(CurveKey().property)
        ) {
            return false
        }
        return true
    }

    override fun hashCode(): Int {
        var result = keySpecification[CurveKey().property].hashCode()
        result = 31 * result + raw.contentHashCode()
        return result
    }

    fun getEncoded(): ByteArray {
        return raw.base64UrlEncoded.encodeToByteArray()
    }

    fun isExportable(): Boolean {
        return this is ExportableKey
    }

    fun isImportable(): Boolean {
        return this is ImportableKey
    }

    fun isSignable(): Boolean {
        return this is SignableKey
    }

    fun isDerivable(): Boolean {
        return this is DerivableKey
    }

    fun canVerify(): Boolean {
        return this is VerifiableKey
    }

    fun getProperty(name: String): String {
        if (!keySpecification.containsKey(name)) {
            throw Exception("KeySpecification do not contain $name")
        }
        return this.keySpecification[name].toString()
    }

    fun isCurve(curve: String): Boolean {
        val keyCurve = keySpecification[CurveKey().property]
        return keyCurve == curve
    }
}

fun getKeyCurveByNameAndIndex(name: String, index: Int?): KeyCurve {
    return when (name) {
        Curve.X25519.value -> {
            KeyCurve(Curve.X25519)
        }

        Curve.ED25519.value -> {
            KeyCurve(Curve.ED25519)
        }

        Curve.SECP256K1.value -> {
            KeyCurve(Curve.SECP256K1, index)
        }

        else -> {
            throw ApolloError.InvalidKeyCurve(name, Curve.values().map { it.value }.toTypedArray())
        }
    }
}
