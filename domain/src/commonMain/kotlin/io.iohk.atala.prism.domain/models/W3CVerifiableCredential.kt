package io.iohk.atala.prism.domain.models

data class W3CVerifiableCredential(
    override val credentialType: CredentialType = CredentialType.W3C,
    override val context: Set<String>,
    override val type: Set<String>,
    override val id: String,
    override val issuer: DID,
    override val issuanceDate: String,
    override val expirationDate: String? = null,
    override val credentialSchema: VerifiableCredentialTypeContainer?,
    override val credentialSubject: String,
    override val credentialStatus: VerifiableCredentialTypeContainer?,
    override val refreshService: VerifiableCredentialTypeContainer?,
    override val evidence: VerifiableCredentialTypeContainer?,
    override val termsOfUse: VerifiableCredentialTypeContainer?,
    override val validFrom: VerifiableCredentialTypeContainer?,
    override val validUntil: VerifiableCredentialTypeContainer?,
    override val proof: String?,
    override val aud: Set<String>,
) : VerifiableCredential
