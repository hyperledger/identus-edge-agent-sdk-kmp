package io.iohk.atala.prism.walletsdk.domain.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.js.JsExport

@Serializable
@JsExport
data class VerifiableCredentialTypeContainer(
    val id: String,
    val type: String,
)

@JsExport
@Serializable
enum class CredentialType(val type: String) {
    JWT("jwt"),
    W3C("w3c"),
    Unknown("Unknown"),
}

@JsExport
interface VerifiableCredential {
    val credentialType: CredentialType
    val context: Array<String>
    val type: Array<String>
    val credentialSchema: VerifiableCredentialTypeContainer?
    val credentialSubject: String
    val credentialStatus: VerifiableCredentialTypeContainer?
    val refreshService: VerifiableCredentialTypeContainer?
    val evidence: VerifiableCredentialTypeContainer?
    val termsOfUse: VerifiableCredentialTypeContainer?
    val issuer: DID
    val issuanceDate: String // Date
    val expirationDate: String? // Date
    val proof: String
    fun toJsonString(): String {
        return Json.encodeToString(this)
    }
}
