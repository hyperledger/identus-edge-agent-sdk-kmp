package org.hyperledger.identus.walletsdk.domain.models.keyManagement

/**
 * This interface defines the functionality of a verifiable key.
 */
interface VerifiableKey {
    /**
     * Method to verify a message with a signature.
     * @param message in ByteArray
     * @param signature in byteArray
     * @return a boolean which tell us if message and signature match
     */
    fun verify(message: ByteArray, signature: ByteArray): Boolean
}
