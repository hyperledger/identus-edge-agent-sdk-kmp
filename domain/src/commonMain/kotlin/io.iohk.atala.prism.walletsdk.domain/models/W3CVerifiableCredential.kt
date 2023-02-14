package io.iohk.atala.prism.walletsdk.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class W3CVerifiableCredential(
    override val credentialType: CredentialType,
    override val context: Array<String>,
    override val type: Array<String>,
    override val issuer: DID,
    override val issuanceDate: String,
    override val expirationDate: String? = null,
    override val credentialSchema: VerifiableCredentialTypeContainer?,
    override val credentialSubject: String,
    override val credentialStatus: VerifiableCredentialTypeContainer?,
    override val refreshService: VerifiableCredentialTypeContainer?,
    override val evidence: VerifiableCredentialTypeContainer?,
    override val termsOfUse: VerifiableCredentialTypeContainer?,
    override val proof: String,
) : VerifiableCredential {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as W3CVerifiableCredential

        if (credentialType != other.credentialType) return false
        if (!context.contentEquals(other.context)) return false
        if (!type.contentEquals(other.type)) return false
        if (issuer != other.issuer) return false
        if (issuanceDate != other.issuanceDate) return false
        if (expirationDate != other.expirationDate) return false
        if (credentialSchema != other.credentialSchema) return false
        if (credentialSubject != other.credentialSubject) return false
        if (credentialStatus != other.credentialStatus) return false
        if (refreshService != other.refreshService) return false
        if (evidence != other.evidence) return false
        if (termsOfUse != other.termsOfUse) return false
        if (proof != other.proof) return false

        return true
    }

    override fun hashCode(): Int {
        var result = credentialType.hashCode()
        result = 31 * result + context.contentHashCode()
        result = 31 * result + type.contentHashCode()
        result = 31 * result + issuer.hashCode()
        result = 31 * result + issuanceDate.hashCode()
        result = 31 * result + (expirationDate?.hashCode() ?: 0)
        result = 31 * result + (credentialSchema?.hashCode() ?: 0)
        result = 31 * result + credentialSubject.hashCode()
        result = 31 * result + (credentialStatus?.hashCode() ?: 0)
        result = 31 * result + (refreshService?.hashCode() ?: 0)
        result = 31 * result + (evidence?.hashCode() ?: 0)
        result = 31 * result + (termsOfUse?.hashCode() ?: 0)
        result = 31 * result + proof.hashCode()
        return result
    }

}
