package org.hyperledger.identus.walletsdk.domain.models

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive
import org.hyperledger.identus.walletsdk.domain.VC

/**
 * A data class representing a JWT credential payload.
 * This payload includes the issuer (`iss`), subject (`sub`), and the verifiable credential (`verifiableCredential`).
 *
 * Note: This data class conforms to the JSON Web Token (JWT) format. For more information, see https://jwt.io/introduction/.
 */
@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class JWTPayload
@JvmOverloads constructor(
    val iss: String,
    val sub: String?,
    @SerialName(VC)
    val verifiableCredential: JWTVerifiableCredential,
    val nbf: Long?,
    @EncodeDefault
    val exp: Long? = null,
    @EncodeDefault
    val jti: String? = null,
    @EncodeDefault
    val aud: Array<String>? = null,
    @EncodeDefault
    val originalJWTString: String? = null
) {

    /**
     * A struct representing the verifiable credential in a JWT credential payload.
     */
    @Serializable
    data class JWTVerifiableCredential @JvmOverloads constructor(
        val context: Array<String> = arrayOf(),
        val type: Array<String> = arrayOf(),
        val credentialSchema: VerifiableCredentialTypeContainer? = null,
        val credentialSubject: Map<String, String>,
        val credentialStatus: VerifiableCredentialTypeContainer? = null,
        val refreshService: VerifiableCredentialTypeContainer? = null,
        val evidence: VerifiableCredentialTypeContainer? = null,
        val termsOfUse: VerifiableCredentialTypeContainer? = null
    ) {
        /**
         * Checks if this JWTVerifiableCredential object is equal to the specified object.
         *
         * @param other The object to compare this JWTVerifiableCredential object against.
         * @return true if the objects are equal, false otherwise.
         */
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as JWTVerifiableCredential

            if (!context.contentEquals(other.context)) return false
            if (!type.contentEquals(other.type)) return false
            if (credentialSchema != other.credentialSchema) return false
            if (credentialSubject != other.credentialSubject) return false
            if (credentialStatus != other.credentialStatus) return false
            if (refreshService != other.refreshService) return false
            if (evidence != other.evidence) return false
            if (termsOfUse != other.termsOfUse) return false

            return true
        }

        /**
         * Calculates the hash code value for the current object. The hash code is computed
         * based on the values of the object's properties.
         *
         * @return The hash code value for the object.
         */
        override fun hashCode(): Int {
            var result = context.contentHashCode()
            result = 31 * result + type.contentHashCode()
            result = 31 * result + (credentialSchema?.hashCode() ?: 0)
            result = 31 * result + credentialSubject.hashCode()
            result = 31 * result + (credentialStatus?.hashCode() ?: 0)
            result = 31 * result + (refreshService?.hashCode() ?: 0)
            result = 31 * result + (evidence?.hashCode() ?: 0)
            result = 31 * result + (termsOfUse?.hashCode() ?: 0)
            return result
        }
    }

    /**
     * Checks if this JWTPayload object is equal to the specified object.
     *
     * @param other The object to compare this JWTPayload object against.
     * @return true if the objects are equal, false otherwise.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (javaClass != other?.javaClass) {
            return false
        }

        other as JWTPayload

        if (iss != other.iss) {
            return false
        }
        if (sub != other.sub) {
            return false
        }
        if (verifiableCredential != other.verifiableCredential) {
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

        return true
    }

    /**
     * Calculates the hash code value for the current `JWTPayload` object.
     * The hash code is computed based on the values of the object's properties.
     *
     * @return The hash code value for the object.
     */
    override fun hashCode(): Int {
        var result = iss.hashCode()
        result = 31 * result + (sub?.hashCode() ?: 0)
        result = 31 * result + verifiableCredential.hashCode()
        result = 31 * result + (nbf?.hashCode() ?: 0)
        result = 31 * result + (exp?.hashCode() ?: 0)
        result = 31 * result + (jti?.hashCode() ?: 0)
        result = 31 * result + (aud?.contentHashCode() ?: 0)
        result = 31 * result + (originalJWTString?.hashCode() ?: 0)
        return result
    }
}

/**
 * Retrieves the value of the specified credential field from a JsonObject.
 *
 * @param name The name of the credential field.
 * @param isOptional Indicates whether the field is optional. If set to false and the field is not found, a [PolluxError.InvalidJWTString] error is thrown.
 * @return The value of the credential field. The return type is inferred based on the actual type of the field in the JsonObject.
 * @throws PolluxError.InvalidJWTString if the field is not found and isOptional is set to false.
 */
inline fun <reified T> JsonObject.getCredentialField(name: String, isOptional: Boolean = false): T {
    if (!isOptional && this[name] == null) throw PolluxError.InvalidJWTString()
    return when (val field = this[name]) {
        is JsonPrimitive -> {
            field.content as T
        }

        is JsonObject -> {
            field as T
        }

        is JsonArray -> {
            field.map { it.jsonPrimitive.content } as T
        }

        else -> null as T
    }
}
