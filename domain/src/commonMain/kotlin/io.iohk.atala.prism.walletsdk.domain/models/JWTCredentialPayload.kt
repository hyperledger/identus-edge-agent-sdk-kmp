package io.iohk.atala.prism.walletsdk.domain.models

import kotlinx.serialization.Serializable
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class JWTCredentialPayload(
    val iss: DID,
    val sub: String? = null,
    val verifiableCredential: JWTVerifiableCredential,
    val nbf: String,
    val exp: String? = null,
    val jti: String,
    val aud: Array<String>,
) {

    @Serializable
    data class JWTVerifiableCredential(
        override val credentialType: CredentialType,
        override val context: Array<String>,
        override val type: Array<String>,
        override val issuer: DID,
        override val issuanceDate: String,
        override val expirationDate: String?,
        override val credentialSchema: VerifiableCredentialTypeContainer? = null,
        override val credentialSubject: String,
        override val credentialStatus: VerifiableCredentialTypeContainer? = null,
        override val refreshService: VerifiableCredentialTypeContainer? = null,
        override val evidence: VerifiableCredentialTypeContainer? = null,
        override val termsOfUse: VerifiableCredentialTypeContainer? = null,
        override val proof: String,
    ) : VerifiableCredential {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as JWTVerifiableCredential

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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as JWTCredentialPayload

        if (iss != other.iss) return false
        if (sub != other.sub) return false
        if (verifiableCredential != other.verifiableCredential) return false
        if (nbf != other.nbf) return false
        if (exp != other.exp) return false
        if (jti != other.jti) return false
        if (!aud.contentEquals(other.aud)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = iss.hashCode()
        result = 31 * result + (sub?.hashCode() ?: 0)
        result = 31 * result + verifiableCredential.hashCode()
        result = 31 * result + nbf.hashCode()
        result = 31 * result + (exp?.hashCode() ?: 0)
        result = 31 * result + jti.hashCode()
        result = 31 * result + aud.contentHashCode()
        return result
    }
}
