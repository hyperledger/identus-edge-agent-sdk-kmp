package org.hyperledger.identus.walletsdk.domain.models.keyManagement

import org.hyperledger.identus.walletsdk.domain.models.Curve

/**
 * Abstraction of what a PrivateKey is and what functionality provides.
 */
abstract class PrivateKey : Key() {

    /**
     * Returns the value of the key curve for this private key
     */
    fun getCurve(): String {
        return this.getProperty(CurveKey().property)
    }

    /**
     * Returns an instance of the key curve for this private key
     */
    fun getCurveInstance(): Curve? {
        return try {
            Curve.valueOf(this.getProperty(CurveKey().property))
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Returns the index for this private key
     */
    fun getIndex(): String {
        return this.getProperty(IndexKey().property)
    }

    /**
     * Returns the value of this private key
     */
    fun getValue(): ByteArray {
        return this.raw
    }

    /**
     * Defines a method to fetch the public key of this private key
     */
    abstract fun publicKey(): PublicKey

    @Throws
    abstract fun jca(): java.security.PrivateKey
}
