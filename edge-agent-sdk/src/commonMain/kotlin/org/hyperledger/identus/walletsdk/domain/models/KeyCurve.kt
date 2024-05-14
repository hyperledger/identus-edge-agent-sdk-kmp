package org.hyperledger.identus.walletsdk.domain.models

import kotlinx.serialization.Serializable

/**
 * Data class representing supported key curves for key generation.
 */
@Serializable
data class KeyCurve @JvmOverloads constructor(
    val curve: Curve,
    val index: Int? = 0
)

/**
 * Enumeration representing supported key curves.
 *
 * @property value The string value of the curve.
 *
 * @constructor Creates a Curve object with the given value.
 */
@Serializable
enum class Curve(val value: String) {
    X25519("X25519"),
    ED25519("Ed25519"),
    SECP256K1("secp256k1")
}
