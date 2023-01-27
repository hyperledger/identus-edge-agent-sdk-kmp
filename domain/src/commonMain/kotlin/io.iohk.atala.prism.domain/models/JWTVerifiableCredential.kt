package io.iohk.atala.prism.domain.models

data class JWTVerifiableCredential(
    override val credentialType: CredentialType,
    override val id: String,
    override val context: Set<String>,
    override val type: Set<String>,
    override val issuer: DID,
    override val issuanceDate: String,
    override val expirationDate: String?,
    override val credentialSchema: VerifiableCredentialTypeContainer?,
    override val credentialSubject: String,
    override val credentialStatus: VerifiableCredentialTypeContainer?,
    override val refreshService: VerifiableCredentialTypeContainer?,
    override val evidence: VerifiableCredentialTypeContainer?,
    override val termsOfUse: VerifiableCredentialTypeContainer?,
    override val validFrom: VerifiableCredentialTypeContainer?,
    override val validUntil: VerifiableCredentialTypeContainer?,
    override val proof: String?,
    override val aud: Set<String>
) : VerifiableCredential

data class JWTCredentialPayload(
    val iss: DID,
    val sub: String? = null,
    val verifiableCredential: JWTVerifiableCredential,
    val nbf: String, // TODO: Change to Date type
    val exp: String? = null, // TODO: Change to Date type
    val jti: String,
)
