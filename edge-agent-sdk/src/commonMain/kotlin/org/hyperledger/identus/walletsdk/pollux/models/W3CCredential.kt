package org.hyperledger.identus.walletsdk.pollux.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.hyperledger.identus.walletsdk.domain.models.Claim
import org.hyperledger.identus.walletsdk.domain.models.ClaimType
import org.hyperledger.identus.walletsdk.domain.models.Credential
import org.hyperledger.identus.walletsdk.domain.models.CredentialType
import org.hyperledger.identus.walletsdk.domain.models.JsonString
import org.hyperledger.identus.walletsdk.domain.models.StorableCredential
import org.hyperledger.identus.walletsdk.domain.models.VerifiableCredentialTypeContainer

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
data class W3CCredential @JvmOverloads constructor(
    val credentialType: CredentialType = CredentialType.W3C,
    val context: Array<String>,
    val type: Array<String>,
    val _id: String,
    val _issuer: String,
    val _subject: String?,
    val issuanceDate: String,
    val expirationDate: String? = null,
    val credentialSchema: VerifiableCredentialTypeContainer? = null,
    val credentialSubject: Map<String, String>? = null,
    val credentialStatus: VerifiableCredentialTypeContainer? = null,
    val refreshService: VerifiableCredentialTypeContainer? = null,
    val evidence: VerifiableCredentialTypeContainer? = null,
    val termsOfUse: VerifiableCredentialTypeContainer? = null,
    val validFrom: VerifiableCredentialTypeContainer? = null,
    val validUntil: VerifiableCredentialTypeContainer? = null,
    val proof: JsonString?,
    val aud: Array<String> = arrayOf()
) : Credential {

    override val id: String
        get() = _id

    override val issuer: String
        get() = _issuer
    override val subject: String?
        get() = _subject

    override val claims: Array<Claim>
        get() {
            return credentialSubject?.let {
                it.map { entry ->
                    Claim(key = entry.key, value = ClaimType.StringValue(entry.value))
                }.toTypedArray()
            } ?: emptyArray()
        }
    override val properties: Map<String, *>
        get() {
            val properties = mutableMapOf(
                "issuanceDate" to issuanceDate,
                "context" to context,
                "type" to type,
                "id" to this.id,
                "aud" to aud
            )

            this.expirationDate?.let { properties["expirationDate"] = it }
            this.credentialSchema?.let { properties["schema"] = it.type }
            this.credentialStatus?.let { properties["credentialStatus"] = it.type }
            this.refreshService?.let { properties["refreshService"] = it.type }
            this.evidence?.let { properties["evidence"] = it.type }
            this.termsOfUse?.let { properties["termsOfUse"] = it.type }
            this.validFrom?.let { properties["validFrom"] = it.type }
            this.validUntil?.let { properties["validUntil"] = it.type }
            this.proof?.let { properties["proof"] = it }

            return properties.toMap()
        }

    override var revoked: Boolean? = null

    /**
     * Converts the current W3CCredential object to a StorableCredential object that can be stored and retrieved from a storage system.
     *
     * @return The converted StorableCredential object.
     */
    fun toStorableCredential(): StorableCredential {
        val c = this
        return object : StorableCredential {
            override val recoveryId: String
                get() = "w3c+credential"
            override val credentialData: ByteArray
                get() = Json.encodeToString(c).toByteArray()
            override val credentialCreated: String?
                get() = null
            override val credentialUpdated: String?
                get() = null
            override val credentialSchema: String?
                get() = c.credentialSchema?.type
            override val validUntil: String?
                get() = null
            override var revoked: Boolean? = c.revoked
            override val availableClaims: Array<String>
                get() = claims.map { it.key }.toTypedArray()

            override val id: String
                get() = c.id
            override val issuer: String
                get() = c.issuer
            override val subject: String?
                get() = c.subject

            override val claims: Array<Claim>
                get() {
                    return credentialSubject?.let {
                        it.map { entry ->
                            Claim(key = entry.key, value = ClaimType.StringValue(entry.value))
                        }.toTypedArray()
                    } ?: emptyArray()
                }
            override val properties: Map<String, *>
                get() {
                    val properties = mutableMapOf(
                        "issuanceDate" to issuanceDate,
                        "context" to context,
                        "type" to type,
                        "id" to id,
                        "aud" to aud
                    )

                    c.expirationDate?.let { properties["expirationDate"] = it }
                    c.credentialSchema?.let { properties["schema"] = it.type }
                    c.credentialStatus?.let { properties["credentialStatus"] = it.type }
                    c.refreshService?.let { properties["refreshService"] = it.type }
                    c.evidence?.let { properties["evidence"] = it.type }
                    c.termsOfUse?.let { properties["termsOfUse"] = it.type }
                    c.validFrom?.let { properties["validFrom"] = it.type }
                    c.validUntil?.let { properties["validUntil"] = it.type }
                    c.proof?.let { properties["proof"] = it }

                    return properties.toMap()
                }

            /**
             * Converts the current W3CCredential object to a StorableCredential object that can be stored and retrieved from a storage system.
             *
             * @return The converted StorableCredential object.
             */
            override fun fromStorableCredential(): Credential {
                return c
            }
        }
    }

    /**
     * Checks if the current W3CCredential object is equal to the specified object.
     *
     * Two W3CCredential objects are considered equal if they have the same values for the following properties:
     * - id
     * - credentialType
     * - context
     * - type
     * - issuer
     * - issuanceDate
     * - expirationDate
     * - credentialSchema
     * - credentialSubject
     * - credentialStatus
     * - refreshService
     * - evidence
     * - termsOfUse
     * - validFrom
     * - validUntil
     * - proof
     * - aud
     *
     * @param other The object to compare with the current W3CCredential object.
     * @return true if the objects are equal, false otherwise.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as W3CCredential

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

    /**
     * Calculates a hash code value for the current `W3CCredential` object.
     *
     * The hash code is generated by combining the hash codes of the following properties:
     * - `id`
     * - `credentialType`
     * - `context`
     * - `type`
     * - `issuer`
     * - `issuanceDate`
     * - `expirationDate`
     * - `credentialSchema`
     * - `credentialSubject`
     * - `credentialStatus`
     * - `refreshService`
     * - `evidence`
     * - `termsOfUse`
     * - `validFrom`
     * - `validUntil`
     * - `proof`
     * - `aud`
     *
     * @return The calculated hash code value.
     */
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
