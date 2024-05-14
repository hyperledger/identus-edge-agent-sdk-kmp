package org.hyperledger.identus.walletsdk.domain.models

/**
 * Represents a verifiable credential that contains information about an entity or identity.
 *
 * Implementing classes are expected to provide implementation for all properties and functions defined in this interface.
 */
interface Credential {
    val id: String
    val issuer: String
    val subject: String?
    val claims: Array<Claim>
    val properties: Map<String, Any?>
    var revoked: Boolean?
}
