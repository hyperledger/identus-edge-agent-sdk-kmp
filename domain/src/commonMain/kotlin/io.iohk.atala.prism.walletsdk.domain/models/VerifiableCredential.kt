package io.iohk.atala.prism.walletsdk.domain.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@OptIn(ExperimentalJsExport::class)
@Serializable
@JsExport
data class VerifiableCredentialTypeContainer(
    val id: String,
    val type: String,
)

@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
enum class CredentialType(val type: String) {
    JWT("jwt"),
    W3C("w3c"),
    Unknown("Unknown"),
}

@OptIn(ExperimentalJsExport::class)
@JsExport
interface VerifiableCredential {
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
    val issuer: DID
    val issuanceDate: String // Date
    val expirationDate: String? // Date
    val validFrom: VerifiableCredentialTypeContainer?
    val validUntil: VerifiableCredentialTypeContainer?
    val proof: JsonString?
    val aud: Array<String>
    fun toJsonString(): String {
        return Json.encodeToString(this)
    }
}
