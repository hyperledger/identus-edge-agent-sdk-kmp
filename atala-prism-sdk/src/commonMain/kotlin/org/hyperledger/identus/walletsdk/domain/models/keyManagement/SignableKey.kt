package org.hyperledger.identus.walletsdk.domain.models.keyManagement

/**
 * This interface defines the functionality of a signable key.
 */
interface SignableKey {

    /**
     * Method to sign a message using a key.
     * @param message the ByteArray to be signed
     * @return the signed message as a ByteArray
     */
    fun sign(message: ByteArray): ByteArray
}
