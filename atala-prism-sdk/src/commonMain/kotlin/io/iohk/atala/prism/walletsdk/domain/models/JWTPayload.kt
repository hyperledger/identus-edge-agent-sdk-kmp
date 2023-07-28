package io.iohk.atala.prism.walletsdk.domain.models

import io.iohk.atala.prism.walletsdk.domain.VC
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive

/**
 * A data class representing a JWT credential payload.
 * This payload includes the issuer (`iss`), subject (`sub`), and the verifiable credential (`verifiableCredential`).
 *
 *Note: This data class conforms to the JSON Web Token (JWT) format. For more information, see https://jwt.io/introduction/.
 */
@Serializable
data class JWTPayload @JvmOverloads constructor(
    val iss: String,
    val sub: String?,
    @SerialName(VC)
    val verifiableCredential: JWTVerifiableCredential,
    val nbf: Long?,
    val exp: Long?,
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
        val termsOfUse: VerifiableCredentialTypeContainer? = null,
    ) {
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

    companion object {

//        @JvmStatic
//        fun fromJson(json: JsonString): JWTPayload {
//            val jsonObject = Json.decodeFromString<JsonObject>(json)
//
//            val jsonVc = jsonObject.getCredentialField<JsonObject>(VC)
//
//            val iss = DID(jsonObject.getCredentialField(name = ISS))
//            val sub: String? = jsonObject.getCredentialField(name = SUB, isOptional = true)
//            val nbf: String = jsonObject.getCredentialField(name = NBF)
//            val exp: String? = jsonObject.getCredentialField(name = EXP, isOptional = true)
//            val jti: String = jsonObject.getCredentialField(name = JTI)
//
//            if (jsonObject.getCredentialField<String>(CREDENTIAL_TYPE) != CredentialType.JWT.type) {
//                throw PolluxError.InvalidCredentialError()
//            }
//
//            val vcCredentialSubject: JsonObject = jsonVc.getCredentialField(name = CREDENTIAL_SUBJECT)
//            val vcProof: JsonObject? = jsonVc.getCredentialField(name = PROOF, isOptional = true)
//            val credentialSubject = Json.encodeToString(vcCredentialSubject)
//            val proof = vcProof?.let { Json.encodeToString(it) }
//
//            val verifiableCredential = JWTVerifiableCredential(
//                id = jsonVc.getCredentialField(name = ID),
//                credentialType = CredentialType.JWT,
//                context = jsonVc.getCredentialField(name = CONTEXT),
//                type = jsonVc.getCredentialField(name = TYPE),
//                credentialSchema = jsonVc.getCredentialField(name = CREDENTIAL_SCHEMA, isOptional = true),
//                credentialSubject = credentialSubject,
//                credentialStatus = jsonVc.getCredentialField(name = CREDENTIAL_STATUS, isOptional = true),
//                refreshService = jsonVc.getCredentialField(name = REFRESH_SERVICE, isOptional = true),
//                evidence = jsonVc.getCredentialField(name = EVIDENCE, isOptional = true),
//                termsOfUse = jsonVc.getCredentialField(name = TERMS_OF_USE, isOptional = true),
//                issuer = jsonVc.getCredentialField(name = ISSUER),
//                issuanceDate = jsonVc.getCredentialField(name = ISSUANCE_DATE),
//                expirationDate = jsonVc.getCredentialField(name = EXPIRATION_DATE, isOptional = true),
//                validFrom = jsonVc.getCredentialField(name = VALID_FROM, isOptional = true),
//                validUntil = jsonVc.getCredentialField(name = VALID_UNTIL, isOptional = true),
//                proof = proof,
//                aud = jsonVc.getCredentialField(name = AUD),
//            )
//
//            return JWTPayload(
//                iss = iss,
//                verifiableCredential = verifiableCredential,
//                nbf = nbf,
//                exp = exp,
//                jti = jti,
//                sub = sub
//            )
//        }
    }
}

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
