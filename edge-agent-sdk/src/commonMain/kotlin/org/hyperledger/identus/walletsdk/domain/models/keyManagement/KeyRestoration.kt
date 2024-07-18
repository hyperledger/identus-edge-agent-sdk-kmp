package org.hyperledger.identus.walletsdk.domain.models.keyManagement

import org.hyperledger.identus.walletsdk.pluto.StorablePrivateKey

/**
* This interface defines the functionality to verify and restore cryptographic keys
*/
interface KeyRestoration {

    /**
     * Determines if the input data corresponds to a private key
     * @param identifier a string that identifies the key
     * @param data a ByteArray that represents the raw data
     * @return a boolean value that tells if the identifier represents the private key
     */
    fun isPrivateKeyData(identifier: String, data: ByteArray): Boolean

    /**
     * Determines if the input data corresponds to a public key
     * @param identifier a string that identifies the key
     * @param data a ByteArray that represents the raw data
     * @return a boolean value that tells if the identifier represents the public key
     */
    fun isPublicKeyData(identifier: String, data: ByteArray): Boolean

    /**
     * A method to restore a private key from a StorableKey
     * @param key a StorableKey instance
     * @return a PrivateKey
     */
    fun restorePrivateKey(key: StorableKey): PrivateKey

    /**
     * A method to restore a public key from a StorableKey
     * @param key a StorableKey instance
     * @return a PublicKey
     */
    fun restorePublicKey(key: StorableKey): PublicKey

    /**
     * Restores a key from a JWK (JSON Web Key).
     *
     * @param key The JWK to restore the key from.
     * @param index The index of the key to restore, if it is a key with multiple sub-keys. Default is null.
     * @return The restored Key object.
     */
    fun restoreKey(key: JWK, index: Int? = null): Key

    /**
     * Restores a private key from StorablePrivateKey.
     *
     * @param storablePrivateKey The StorablePrivateKey to restore the key from.
     * @return The restored Key object.
     */
    fun restorePrivateKey(storablePrivateKey: StorablePrivateKey): PrivateKey
}
