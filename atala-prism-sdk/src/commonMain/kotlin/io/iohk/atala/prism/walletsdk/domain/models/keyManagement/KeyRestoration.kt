package io.iohk.atala.prism.walletsdk.domain.models.keyManagement

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
}
