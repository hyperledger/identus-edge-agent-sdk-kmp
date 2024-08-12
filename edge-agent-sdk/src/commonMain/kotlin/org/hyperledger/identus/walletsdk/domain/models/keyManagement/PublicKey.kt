package org.hyperledger.identus.walletsdk.domain.models.keyManagement

import org.hyperledger.identus.walletsdk.domain.models.Curve

/**
 * Abstraction of what a PublicKey is and the functionality it provides.
 */
abstract class PublicKey : Key() {

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
     * Returns the value of this private key
     */
    fun getValue(): ByteArray {
        return this.raw
    }

    @Throws
    abstract fun jca(): java.security.PublicKey
}
