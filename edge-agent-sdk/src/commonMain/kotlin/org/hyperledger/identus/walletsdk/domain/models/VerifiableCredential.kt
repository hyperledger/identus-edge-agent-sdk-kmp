package org.hyperledger.identus.walletsdk.domain.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * A data class representing a container for verifiable credential types.
 * This data class is used to encode and decode verifiable credential types for use with JSON.
 * The VerifiableCredentialTypeContainer contains properties for the ID and type of the verifiable credential.
 * ::: info
 * The VerifiableCredentialTypeContainer is used to encode and decode verifiable credential types for use with JSON.
 * :::
 */
@Serializable
data class VerifiableCredentialTypeContainer(
    val id: String,
    val type: String
)

/**
 * Enum class representing different types of verifiable credentials.
 * The CredentialType is used to indicate the type of verifiable credential.
 * The possible values of the enum are jwt, w3c, and unknown.
 *
 * ::: info
 * The CredentialType enum is used to indicate the type of verifiable credential.
 * :::
 */
@Serializable
enum class CredentialType(val type: String) {
    JWT("prism/jwt"),
    W3C("w3c"),
    ANONCREDS_OFFER("anoncreds/credential-offer@v1.0"),
    ANONCREDS_REQUEST("anoncreds/credential-request@v1.0"),
    ANONCREDS_ISSUE("anoncreds/credential@v1.0"),
    ANONCREDS_PROOF_REQUEST("anoncreds/proof-request@v1.0"),
    PRESENTATION_EXCHANGE("dif/presentation-exchange/definitions@v1.0"),
    Unknown("Unknown")
}

/**
 * Interface for objects representing verifiable credentials.
 */
@Serializable
sealed interface VerifiableCredential {
    val id: String
    val credentialType: CredentialType
    val context: Array<String>
    val type: Array<String>
    val credentialSchema: VerifiableCredentialTypeContainer?
    val credentialSubject: String
    val credentialStatus: VerifiableCredentialTypeContainer?
    val refreshService: VerifiableCredentialTypeContainer?
    val evidence: VerifiableCredentialTypeContainer?
    val termsOfUse: VerifiableCredentialTypeContainer?
    val issuer: DID?
    val issuanceDate: String // TODO(Date)
    val expirationDate: String? // TODO(Date)
    val validFrom: VerifiableCredentialTypeContainer?
    val validUntil: VerifiableCredentialTypeContainer?
    val proof: JsonString?
    val aud: Array<String>

    /**
     * Converts the object to a JSON string representation.
     *
     * @return The JSON string representation of the object.
     */
    fun toJsonString(): String {
        return Json.encodeToString(this)
    }
}
