package io.iohk.atala.prism.domain.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class VerifiableCredentialTypeContainer(
    val id: String,
    val type: String
)

enum class CredentialType(val type: String) {
    JWT("jwt"),
    W3C("w3c"),
    Unknown("Unknown")
}

interface VerifiableCredential {
    val credentialType: CredentialType
    val id: String
    val context: Set<String>
    val type: Set<String>
    val issuer: DID
    val issuanceDate: String // Date
    val expirationDate: String? // Date
    val credentialSchema: VerifiableCredentialTypeContainer?
    val credentialSubject: String
    val credentialStatus: VerifiableCredentialTypeContainer?
    val refreshService: VerifiableCredentialTypeContainer?
    val evidence: VerifiableCredentialTypeContainer?
    val termsOfUse: VerifiableCredentialTypeContainer?
    val validFrom: VerifiableCredentialTypeContainer?
    val validUntil: VerifiableCredentialTypeContainer?
    val proof: String?
    val aud: Set<String>

    fun toJsonString(): String {
        return Json.encodeToString(this)
    }
}
