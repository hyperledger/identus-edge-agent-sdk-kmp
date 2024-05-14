package org.hyperledger.identus.walletsdk.domain.models

/**
 * Represents a storable credential that can be stored and retrieved from a storage system.
 */
interface StorableCredential : Credential {
    val recoveryId: String
    val credentialData: ByteArray
    val credentialCreated: String?
    val credentialUpdated: String?
    val credentialSchema: String?
    val validUntil: String?
    val availableClaims: Array<String>

    /**
     * Converts a storable credential to a regular credential.
     *
     * @return The converted Credential object.
     */
    fun fromStorableCredential(): Credential
}
