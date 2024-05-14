package org.hyperledger.identus.walletsdk.domain.models.keyManagement

import kotlinx.serialization.Serializable

/**
 * Data class representing a pair of private and public keys for a specific key curve.
 */
@Serializable
abstract class KeyPair {
    abstract var privateKey: PrivateKey
    abstract var publicKey: PublicKey

    /**
     * Returns the value of the key curve for this key pair.
     *
     * @return The value of the key curve.
     */
    fun getCurve(): String {
        return this.privateKey.getProperty(CurveKey().property)
    }
}
