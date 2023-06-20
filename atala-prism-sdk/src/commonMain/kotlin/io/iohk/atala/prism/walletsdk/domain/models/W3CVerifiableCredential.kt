package io.iohk.atala.prism.walletsdk.domain.models

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmOverloads

/**
 * A data class representing a W3C Verifiable Credential.
 * This data class conforms to the VerifiableCredential interface, which defines the properties and methods required for
 * a verifiable credential.
 * The W3CVerifiableCredential contains properties for the credential's context, type, ID, issuer, issuance date,
 * expiration date, credential schema, credential subject, credential status, refresh service, evidence, terms of use,
 * valid from date, valid until date, proof, and audience.
 *
 * Note: The W3CVerifiableCredential is designed to work with W3C-compliant verifiable credentials.
 */
@Serializable
data class W3CVerifiableCredential @JvmOverloads constructor(
    override val id: String,
    override val credentialType: CredentialType = CredentialType.W3C,
    override val context: Array<String>,
    override val type: Array<String>,
    override val issuer: DID,
    override val issuanceDate: String,
    override val expirationDate: String? = null,
    override val credentialSchema: VerifiableCredentialTypeContainer? = null,
    override val credentialSubject: String,
    override val credentialStatus: VerifiableCredentialTypeContainer? = null,
    override val refreshService: VerifiableCredentialTypeContainer? = null,
    override val evidence: VerifiableCredentialTypeContainer? = null,
    override val termsOfUse: VerifiableCredentialTypeContainer? = null,
    override val validFrom: VerifiableCredentialTypeContainer? = null,
    override val validUntil: VerifiableCredentialTypeContainer? = null,
    override val proof: JsonString?,
    override val aud: Array<String> = arrayOf()
) : VerifiableCredential {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as W3CVerifiableCredential

        if (id != other.id) return false
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
        if (validFrom != other.validFrom) return false
        if (validUntil != other.validUntil) return false
        if (proof != other.proof) return false
        if (!aud.contentEquals(other.aud)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + credentialType.hashCode()
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
        result = 31 * result + (validFrom?.hashCode() ?: 0)
        result = 31 * result + (validUntil?.hashCode() ?: 0)
        result = 31 * result + proof.hashCode()
        result = 31 * result + aud.contentHashCode()
        return result
    }
}
