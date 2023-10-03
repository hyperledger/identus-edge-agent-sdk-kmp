package io.iohk.atala.prism.walletsdk.domain.models.keyManagement

import kotlinx.serialization.Serializable

/**
 * Data class representing a pair of private and public keys for a specific key curve.
 */
@Serializable
abstract class KeyPair {
    abstract var privateKey: PrivateKey
    abstract var publicKey: PublicKey

    fun getCurve(): String {
        return this.privateKey.getProperty(CurveKey().property)
    }
}
