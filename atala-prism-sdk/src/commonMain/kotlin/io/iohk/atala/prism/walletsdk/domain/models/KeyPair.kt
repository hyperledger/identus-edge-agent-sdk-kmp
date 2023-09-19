package io.iohk.atala.prism.walletsdk.domain.models

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmOverloads

/**
 * Data class representing a pair of private and public keys for a specific key curve.
 */
@Serializable
data class KeyPair(
    val keyCurve: KeyCurve = KeyCurve(Curve.SECP256K1),
    val privateKey: PrivateKey,
    val publicKey: PublicKey
)

/**
 * Data class representing supported key curves for key generation.
 */
@Serializable
data class KeyCurve @JvmOverloads constructor(
    val curve: Curve,
    val index: Int? = 0
)

@Serializable
enum class Curve(val value: String) {
    X25519("X25519"),
    ED25519("Ed25519"),
    SECP256K1("secp256k1")
}

fun getKeyCurveByNameAndIndex(name: String, index: Int?): KeyCurve {
    return when (name) {
        Curve.X25519.value ->
            KeyCurve(Curve.X25519)

        Curve.ED25519.value ->
            KeyCurve(Curve.ED25519)

        Curve.SECP256K1.value ->
            KeyCurve(Curve.SECP256K1, index)

        else ->
            KeyCurve(Curve.SECP256K1, index)
    }
}
