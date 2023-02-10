package io.iohk.atala.prism.walletsdk.domain.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.js.JsExport

@Serializable
@JsExport
data class VerifiableCredentialTypeContainer(
    val id: String,
    val type: String
)

@JsExport
enum class CredentialType(val type: String) {
    JWT("jwt"),
    W3C("w3c"),
    Unknown("Unknown")
}

@JsExport
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
