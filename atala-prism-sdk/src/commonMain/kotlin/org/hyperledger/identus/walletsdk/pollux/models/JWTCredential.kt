package org.hyperledger.identus.walletsdk.pollux.models

import org.hyperledger.identus.walletsdk.domain.models.Claim
import org.hyperledger.identus.walletsdk.domain.models.ClaimType
import org.hyperledger.identus.walletsdk.domain.models.Credential
import org.hyperledger.identus.walletsdk.domain.models.JWTPayload
import org.hyperledger.identus.walletsdk.domain.models.StorableCredential
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.util.Base64

/**
 * Represents a JSON Web Token (JWT) credential.
 *
 * This class provides a way to parse and extract information from a JWT string.
 * It implements the Credential interface and provides implementations for all its properties and functions.
 *
 * @property data The original JWT string representation of the credential.
 * @property jwtString The JWT string representation of the credential.
 * @property jwtPayload The parsed JWT payload containing the credential information.
 */
data class JWTCredential(val data: String) : Credential {
    private var jwtString: String = data
    var jwtPayload: JWTPayload

    init {
        val jwtParts = jwtString.split(".")
        require(jwtParts.size == 3) { "Invalid JWT string" }
        val credentialString = jwtParts[1]
        val base64Data = Base64.getUrlDecoder().decode(credentialString)
        val jsonString = base64Data.toString(Charsets.UTF_8)

        val json = Json { ignoreUnknownKeys = true }
        this.jwtPayload = json.decodeFromString(jsonString)
    }

    override val id: String
        get() = jwtString

    override val issuer: String
        get() = jwtPayload.iss

    override val subject: String?
        get() = jwtPayload.sub

    override val claims: Array<Claim>
        get() = jwtPayload.verifiableCredential.credentialSubject.map {
            Claim(key = it.key, value = ClaimType.StringValue(it.value))
        }.toTypedArray()

    override val properties: Map<String, Any?>
        get() {
            val properties = mutableMapOf<String, Any?>()
            properties["nbf"] = jwtPayload.nbf
            properties["jti"] = jwtPayload.jti
            properties["type"] = jwtPayload.verifiableCredential.type
            properties["aud"] = jwtPayload.aud
            properties["id"] = jwtString

            jwtPayload.exp?.let { properties["exp"] = it }
            jwtPayload.verifiableCredential.credentialSchema?.let {
                properties["schema"] = it.id
            }
            jwtPayload.verifiableCredential.credentialStatus?.let {
                properties["credentialStatus"] = it.type
            }
            jwtPayload.verifiableCredential.refreshService?.let {
                properties["refreshService"] = it.type
            }
            jwtPayload.verifiableCredential.evidence?.let {
                properties["evidence"] = it.type
            }
            jwtPayload.verifiableCredential.termsOfUse?.let {
                properties["termsOfUse"] = it.type
            }

            return properties.toMap()
        }

    override var revoked: Boolean? = null

    /**
     * Converts the current instance of [JWTCredential] to a [StorableCredential].
     *
     * @return The converted [StorableCredential].
     */
    fun toStorableCredential(): StorableCredential {
        val c = this
        return object : StorableCredential {
            override val id: String
                get() = jwtString
            override val recoveryId: String
                get() = "jwt+credential"
            override val credentialData: ByteArray
                get() = c.data.toByteArray()

            override val issuer: String
                get() = c.issuer

            override val subject: String?
                get() = c.subject
            override val credentialCreated: String?
                get() = null
            override val credentialUpdated: String?
                get() = null
            override val credentialSchema: String?
                get() = c.jwtPayload.verifiableCredential.credentialSchema?.type
            override val validUntil: String?
                get() = null
            override var revoked: Boolean? = c.revoked
            override val availableClaims: Array<String>
                get() = c.claims.map { it.key }.toTypedArray()

            override val claims: Array<Claim>
                get() = jwtPayload.verifiableCredential.credentialSubject.map {
                    Claim(key = it.key, value = ClaimType.StringValue(it.value))
                }.toTypedArray()

            override val properties: Map<String, Any?>
                get() {
                    val properties = mutableMapOf<String, Any?>()
                    properties["nbf"] = jwtPayload.nbf
                    properties["jti"] = jwtPayload.jti
                    properties["type"] = jwtPayload.verifiableCredential.type
                    properties["aud"] = jwtPayload.aud
                    properties["id"] = jwtString

                    jwtPayload.exp?.let { properties["exp"] = it }
                    jwtPayload.verifiableCredential.credentialSchema?.let {
                        properties["schema"] = it.id
                    }
                    jwtPayload.verifiableCredential.credentialStatus?.let {
                        properties["credentialStatus"] = it.type
                    }
                    jwtPayload.verifiableCredential.refreshService?.let {
                        properties["refreshService"] = it.type
                    }
                    jwtPayload.verifiableCredential.evidence?.let {
                        properties["evidence"] = it.type
                    }
                    jwtPayload.verifiableCredential.termsOfUse?.let {
                        properties["termsOfUse"] = it.type
                    }

                    return properties.toMap()
                }

            /**
             * Converts the current instance of [JWTCredential] to a [Credential].
             *
             * @return The converted [Credential].
             */
            override fun fromStorableCredential(): Credential {
                return c
            }
        }
    }
}
