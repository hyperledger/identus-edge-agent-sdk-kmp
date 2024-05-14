package org.hyperledger.identus.walletsdk.domain.models

/**
 * The [DIDResolver] protocol defines the interface for resolving DID document using a specific DID method.
 * Implementations of this interface provide a [resolve] method that can be used to retrieve the DID document for a given DID.
 */
interface DIDResolver {
    val method: String
    suspend fun resolve(didString: String): DIDDocument
}
