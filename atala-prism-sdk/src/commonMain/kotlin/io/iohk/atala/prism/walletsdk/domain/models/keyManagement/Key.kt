package io.iohk.atala.prism.walletsdk.domain.models.keyManagement

import io.iohk.atala.prism.apollo.base64.base64UrlEncoded
import io.iohk.atala.prism.walletsdk.domain.models.ApolloError
import io.iohk.atala.prism.walletsdk.domain.models.Curve
import io.iohk.atala.prism.walletsdk.domain.models.KeyCurve

abstract class Key {
    abstract val type: KeyTypes
    abstract val keySpecification: MutableMap<KeyProperties, String>
    abstract val size: Int
    abstract val raw: ByteArray

    fun getEncoded(): ByteArray {
        return raw.base64UrlEncoded.encodeToByteArray()
    }

    fun isExportable(): Boolean {
        return this is StorableKey
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
        val property = getKeyPropertyByName(name)
        if (!keySpecification.containsKey(property)) {
            throw Exception("KeySpecification do not contain $name")
        }
        return this.keySpecification[property].toString()
    }

    fun isCurve(curve: String): Boolean {
        val keyCurve = keySpecification[CurveKey()]
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
            throw ApolloError.InvalidKeyCurve()
        }
    }
}
