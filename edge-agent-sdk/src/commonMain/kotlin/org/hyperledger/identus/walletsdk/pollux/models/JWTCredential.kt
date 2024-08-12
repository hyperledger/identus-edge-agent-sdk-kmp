package org.hyperledger.identus.walletsdk.pollux.models

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.ECDSASigner
import com.nimbusds.jose.crypto.bc.BouncyCastleProviderSingleton
import com.nimbusds.jose.jwk.Curve
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import io.iohk.atala.prism.didcomm.didpeer.core.toJsonElement
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.builtins.ArraySerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonTransformingSerializer
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.hyperledger.identus.apollo.base64.base64UrlDecoded
import org.hyperledger.identus.walletsdk.domain.VC
import org.hyperledger.identus.walletsdk.domain.models.Claim
import org.hyperledger.identus.walletsdk.domain.models.ClaimType
import org.hyperledger.identus.walletsdk.domain.models.Credential
import org.hyperledger.identus.walletsdk.domain.models.CredentialOperationsOptions
import org.hyperledger.identus.walletsdk.domain.models.DID
import org.hyperledger.identus.walletsdk.domain.models.JWTPayload
import org.hyperledger.identus.walletsdk.domain.models.JWTVerifiableCredential
import org.hyperledger.identus.walletsdk.domain.models.JWTVerifiablePresentation
import org.hyperledger.identus.walletsdk.domain.models.PolluxError
import org.hyperledger.identus.walletsdk.domain.models.ProvableCredential
import org.hyperledger.identus.walletsdk.domain.models.StorableCredential
import org.hyperledger.identus.walletsdk.pollux.CHALLENGE
import org.hyperledger.identus.walletsdk.pollux.CONTEXT
import org.hyperledger.identus.walletsdk.pollux.CONTEXT_URL
import org.hyperledger.identus.walletsdk.pollux.DOMAIN
import org.hyperledger.identus.walletsdk.pollux.NONCE
import org.hyperledger.identus.walletsdk.pollux.OPTIONS
import org.hyperledger.identus.walletsdk.pollux.TYPE
import org.hyperledger.identus.walletsdk.pollux.VERIFIABLE_CREDENTIAL
import org.hyperledger.identus.walletsdk.pollux.VERIFIABLE_PRESENTATION
import org.hyperledger.identus.walletsdk.pollux.VP
import java.security.PrivateKey
import java.security.interfaces.ECPrivateKey

@Serializable
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
@OptIn(ExperimentalSerializationApi::class)
data class JWTCredential @JvmOverloads constructor(
    override val id: String,
    override val iss: String,
    override val sub: String?,
    override val nbf: Long?,
    override val exp: Long?,
    override val jti: String?,
    @Serializable(with = AudSerializer::class)
    override val aud: Array<String>?,
    override val originalJWTString: String?,
    @SerialName("vp")
    override var verifiablePresentation: JWTVerifiablePresentation? = null,
    @SerialName(VC)
    override var verifiableCredential: JWTVerifiableCredential? = null,
    var nonce: String? = null
) : Credential, JWTPayload, ProvableCredential {

    @Transient
    override val issuer: String = iss

    override val subject: String?
        get() = sub

    override val claims: Array<Claim>
        get() {
            return verifiableCredential?.credentialSubject?.map {
                Claim(key = it.key, value = ClaimType.StringValue(it.value))
            }?.toTypedArray()
                ?: emptyArray<Claim>()
        }

    override val properties: Map<String, Any?>
        get() {
            val properties = mutableMapOf<String, Any?>()
            properties["nbf"] = nbf
            properties["jti"] = jti
            verifiableCredential?.let { verifiableCredential ->
                properties["type"] = verifiableCredential.type
                verifiableCredential.credentialSchema?.let {
                    properties["schema"] = it.id
                }
                verifiableCredential.credentialStatus?.let {
                    properties["credentialStatus"] = it.type
                }
                verifiableCredential.refreshService?.let {
                    properties["refreshService"] = it.type
                }
                verifiableCredential.evidence?.let {
                    properties["evidence"] = it.type
                }
                verifiableCredential.termsOfUse?.let {
                    properties["termsOfUse"] = it.type
                }
            }
            verifiablePresentation?.let { verifiablePresentation ->
                properties["type"] = verifiablePresentation.type
            }
            properties["aud"] = aud
            properties["id"] = id

            exp?.let { properties["exp"] = it }
            return properties.toMap()
        }

    override var revoked: Boolean? = false
    override suspend fun presentation(request: ByteArray, options: List<CredentialOperationsOptions>): String {
        var exportableKeyOption: org.hyperledger.identus.walletsdk.domain.models.keyManagement.PrivateKey? = null
        var subjectDID: DID? = null

        for (option in options) {
            when (option) {
                is CredentialOperationsOptions.SubjectDID -> subjectDID = option.did
                is CredentialOperationsOptions.ExportableKey -> exportableKeyOption = option.key
                else -> {}
            }
        }
        if (subjectDID == null) {
            throw PolluxError.InvalidPrismDID()
        }
        if (exportableKeyOption == null) {
            throw PolluxError.WrongKeyProvided("Secp256k1", actual = "null")
        }
        val jsonString = String(request, Charsets.UTF_8)
        val requestJson = Json.parseToJsonElement(jsonString).jsonObject
        val domain =
            getDomain(requestJson) ?: throw PolluxError.NoDomainOrChallengeFound()
        val challenge =
            getChallenge(requestJson) ?: throw PolluxError.NoDomainOrChallengeFound()
        return signClaimsProofPresentationJWT(
            subjectDID,
            exportableKeyOption.jca() as ECPrivateKey,
            this,
            domain,
            challenge
        )
    }

    /**
     * Converts the current instance of [JWTCredential] to a [StorableCredential].
     *
     * @return The converted [StorableCredential].
     */
    fun toStorableCredential(): StorableCredential {
        val c = this
        return object : StorableCredential {
            override val id: String
                get() = c.id
            override val recoveryId: String
                get() = "jwt+credential"
            override val credentialData: ByteArray
                get() = c.id.toByteArray()

            override val issuer: String
                get() = c.issuer

            override val subject: String?
                get() = c.subject
            override val credentialCreated: String?
                get() = null
            override val credentialUpdated: String?
                get() = null
            override val credentialSchema: String?
                get() = verifiableCredential?.credentialSchema?.type
            override val validUntil: String?
                get() = c.exp?.toString()
            override var revoked: Boolean? = c.revoked
            override val availableClaims: Array<String>
                get() = c.claims.map { it.key }.toTypedArray()

            override val claims: Array<Claim>
                get() = verifiableCredential?.credentialSubject?.map {
                    Claim(key = it.key, value = ClaimType.StringValue(it.value))
                }?.toTypedArray() ?: emptyArray()

            override val properties: Map<String, Any?>
                get() {
                    val properties = mutableMapOf<String, Any?>()
                    properties["nbf"] = nbf
                    properties["jti"] = jti
                    properties["aud"] = aud
                    properties["id"] = id

                    exp?.let { properties["exp"] = it }
                    verifiableCredential?.let { verifiableCredential ->
                        properties["type"] = verifiableCredential.type
                        verifiableCredential.credentialSchema?.let {
                            properties["schema"] = it.id
                        }
                        verifiableCredential.credentialStatus?.let {
                            properties["credentialStatus"] = it.type
                        }
                        verifiableCredential.refreshService?.let {
                            properties["refreshService"] = it.type
                        }
                        verifiableCredential.evidence?.let {
                            properties["evidence"] = it.type
                        }
                        verifiableCredential.termsOfUse?.let {
                            properties["termsOfUse"] = it.type
                        }
                    }
                    verifiablePresentation?.let { verifiablePresentation ->
                        properties["type"] = verifiablePresentation.type
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

    /**
     * Returns the domain from the given JsonObject.
     *
     * @param jsonObject The JsonObject from which to retrieve the domain.
     * @return The domain as a String, or null if not found.
     */
    private fun getDomain(jsonObject: JsonObject): String? {
        return jsonObject[OPTIONS]?.jsonObject?.get(DOMAIN)?.jsonPrimitive?.content
    }

    /**
     * Retrieves the challenge value from the given JsonObject.
     *
     * @param jsonObject The JsonObject from which to retrieve the challenge.
     * @return The challenge value as a String, or null if not found in the JsonObject.
     */
    private fun getChallenge(jsonObject: JsonObject): String? {
        return jsonObject[OPTIONS]?.jsonObject?.get(CHALLENGE)?.jsonPrimitive?.content
    }

    /**
     * Signs the claims for a proof presentation JSON Web Token (JWT).
     *
     * @param subjectDID The DID of the subject for whom the JWT is being created.
     * @param privateKey The private key used to sign the JWT.
     * @param credential The credential to be included in the presentation.
     * @param domain The domain of the JWT.
     * @param challenge The challenge value for the JWT.
     * @return The signed JWT as a string.
     */
    internal fun signClaimsProofPresentationJWT(
        subjectDID: DID,
        privateKey: ECPrivateKey,
        credential: Credential,
        domain: String,
        challenge: String
    ): String {
        return signClaims(subjectDID, privateKey, domain, challenge, credential)
    }

    /**
     * Signs the claims for a JWT.
     *
     * @param subjectDID The DID of the subject for whom the JWT is being created.
     * @param privateKey The private key used to sign the JWT.
     * @param domain The domain of the JWT.
     * @param challenge The challenge value for the JWT.
     * @param credential The optional credential to be included in the JWT.
     * @return The signed JWT as a string.
     */
    private fun signClaims(
        subjectDID: DID,
        privateKey: ECPrivateKey,
        domain: String,
        challenge: String,
        credential: Credential? = null
    ): String {
        val presentation: MutableMap<String, Collection<String>> = mutableMapOf(
            CONTEXT to setOf(CONTEXT_URL),
            TYPE to setOf(VERIFIABLE_PRESENTATION)
        )
        credential?.let {
            presentation[VERIFIABLE_CREDENTIAL] = listOf(it.id)
        }

        val claims = JWTClaimsSet.Builder()
            .issuer(subjectDID.toString())
            .audience(domain)
            .claim(NONCE, challenge)
            .claim(VP, presentation)
            .build()

        // Generate a JWS header with the ES256K algorithm
        val header = JWSHeader.Builder(JWSAlgorithm.ES256K)
            .build()

        // Sign the JWT with the private key
        val jwsObject = SignedJWT(header, claims)
        val signer = ECDSASigner(
            privateKey as PrivateKey,
            Curve.SECP256K1
        )
        val provider = BouncyCastleProviderSingleton.getInstance()
        signer.jcaContext.provider = provider
        jwsObject.sign(signer)

        // Serialize the JWS object to a string
        return jwsObject.serialize()
    }

    /**
     * Compares this JWTCredential with the specified object for equality.
     *
     * @param other The object to compare with this JWTCredential.
     * @return true if the specified object is equal to this JWTCredential, false otherwise.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (javaClass != other?.javaClass) {
            return false
        }

        other as JWTCredential

        if (id != other.id) {
            return false
        }
        if (iss != other.iss) {
            return false
        }
        if (sub != other.sub) {
            return false
        }
        if (nbf != other.nbf) {
            return false
        }
        if (exp != other.exp) {
            return false
        }
        if (jti != other.jti) {
            return false
        }
        if (aud != null) {
            if (other.aud == null) {
                return false
            }
            if (!aud.contentEquals(other.aud)) {
                return false
            }
        } else if (other.aud != null) {
            return false
        }
        if (originalJWTString != other.originalJWTString) {
            return false
        }
        if (verifiablePresentation != other.verifiablePresentation) {
            return false
        }
        if (verifiableCredential != other.verifiableCredential) {
            return false
        }
        if (nonce != other.nonce) {
            return false
        }
        if (issuer != other.issuer) {
            return false
        }
        if (revoked != other.revoked) {
            return false
        }

        return true
    }

    /**
     * Calculates the hash code value for the object.
     *
     * @return The hash code value.
     */
    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + iss.hashCode()
        result = 31 * result + (sub?.hashCode() ?: 0)
        result = 31 * result + (nbf?.hashCode() ?: 0)
        result = 31 * result + (exp?.hashCode() ?: 0)
        result = 31 * result + (jti?.hashCode() ?: 0)
        result = 31 * result + (aud?.contentHashCode() ?: 0)
        result = 31 * result + (originalJWTString?.hashCode() ?: 0)
        result = 31 * result + (verifiablePresentation?.hashCode() ?: 0)
        result = 31 * result + (verifiableCredential?.hashCode() ?: 0)
        result = 31 * result + (nonce?.hashCode() ?: 0)
        result = 31 * result + issuer.hashCode()
        result = 31 * result + (revoked?.hashCode() ?: 0)
        return result
    }

    /**
     * AudSerializer is a custom serializer for serializing and deserializing JSON arrays of strings.
     *
     * This class extends the JsonTransformingSerializer class from the kotlinx.serialization.json package,
     * with a type parameter of Array<String>. It transforms the serialization and deserialization process
     * of JSON elements into arrays of strings.
     *
     * @OptIn annotation indicates that this class makes use of experimental serialization API.
     */
    @OptIn(ExperimentalSerializationApi::class)
    object AudSerializer :
        JsonTransformingSerializer<Array<String>>(ArraySerializer(String.serializer())) {
        /**
         * Transforms a given [element] into a serialized form.
         *
         * @param element the [JsonElement] to be transformed
         * @return the transformed [JsonElement]
         */
        override fun transformDeserialize(element: JsonElement): JsonElement {
            // Check if the element is a JSON array
            if (element is JsonArray) {
                return element
            }
            // If it's a single string, wrap it into an array
            return Json.encodeToJsonElement(arrayOf(element.jsonPrimitive.content))
        }
    }

    companion object {
        /**
         * Converts a JWT string into a JWTCredential object.
         *
         * @param jwtString The JWT string to convert.
         * @return The JWTCredential object extracted from the JWT string.
         * @throws IllegalArgumentException If the JWT string is invalid.
         */
        @JvmStatic
        fun fromJwtString(jwtString: String): JWTCredential {
            val jwtParts = jwtString.split(".")
            require(jwtParts.size == 3) { "Invalid JWT string" }
            val credentialString = jwtParts[1]
            val jsonString = credentialString.base64UrlDecoded

            val json = Json {
                ignoreUnknownKeys = true
                explicitNulls = false
            }

            val jsonObject = Json.decodeFromString<JsonElement>(jsonString).jsonObject
            return json.decodeFromJsonElement(jsonObject.plus("id" to jwtString).toJsonElement())
        }
    }
}
