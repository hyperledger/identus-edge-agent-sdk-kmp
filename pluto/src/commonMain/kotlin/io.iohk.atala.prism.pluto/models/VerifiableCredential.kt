package io.iohk.atala.prism.pluto.models

data class VerifiableCredential(
val credentialType: CredentialType,
val id: String,
val context: Set<String>,
val type: Set<String>,
val issuer: DID,
val issuanceDate: String, // Date
val expirationDate: String?, // Date
val credentialSchema: VerifiableCredentialTypeContainer?,
val credentialSubject: String,
val credentialStatus: VerifiableCredentialTypeContainer?,
val refreshService: VerifiableCredentialTypeContainer?,
val evidence: VerifiableCredentialTypeContainer?,
val termsOfUse: VerifiableCredentialTypeContainer?,
val validFrom: VerifiableCredentialTypeContainer?,
val validUntil: VerifiableCredentialTypeContainer?,
)
