package io.iohk.atala.prism.walletsdk.domain.models

import io.iohk.atala.prism.walletsdk.domain.AUD
import io.iohk.atala.prism.walletsdk.domain.CONTEXT
import io.iohk.atala.prism.walletsdk.domain.CREDENTIAL_SCHEMA
import io.iohk.atala.prism.walletsdk.domain.CREDENTIAL_STATUS
import io.iohk.atala.prism.walletsdk.domain.CREDENTIAL_SUBJECT
import io.iohk.atala.prism.walletsdk.domain.CREDENTIAL_TYPE
import io.iohk.atala.prism.walletsdk.domain.EVIDENCE
import io.iohk.atala.prism.walletsdk.domain.EXP
import io.iohk.atala.prism.walletsdk.domain.EXPIRATION_DATE
import io.iohk.atala.prism.walletsdk.domain.ID
import io.iohk.atala.prism.walletsdk.domain.ISS
import io.iohk.atala.prism.walletsdk.domain.ISSUANCE_DATE
import io.iohk.atala.prism.walletsdk.domain.ISSUER
import io.iohk.atala.prism.walletsdk.domain.JTI
import io.iohk.atala.prism.walletsdk.domain.NBF
import io.iohk.atala.prism.walletsdk.domain.PROOF
import io.iohk.atala.prism.walletsdk.domain.REFRESH_SERVICE
import io.iohk.atala.prism.walletsdk.domain.SUB
import io.iohk.atala.prism.walletsdk.domain.TERMS_OF_USE
import io.iohk.atala.prism.walletsdk.domain.TYPE
import io.iohk.atala.prism.walletsdk.domain.VALID_FROM
import io.iohk.atala.prism.walletsdk.domain.VALID_UNTIL
import io.iohk.atala.prism.walletsdk.domain.VC
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive
import kotlin.jvm.JvmOverloads

@Serializable
data class JWTCredentialPayload(
    val iss: DID,
    val sub: String?,
    @SerialName(VC)
    val verifiableCredential: JWTVerifiableCredential,
    val nbf: String,
    val exp: String?,
    val jti: String
) : VerifiableCredential {

    @Serializable
    data class JWTVerifiableCredential @JvmOverloads constructor(
        override val credentialType: CredentialType,
        override val context: Array<String> = arrayOf(),
        override val type: Array<String> = arrayOf(),
        override val issuer: DID,
        override val credentialSchema: VerifiableCredentialTypeContainer? = null,
        override val credentialSubject: String,
        override val credentialStatus: VerifiableCredentialTypeContainer? = null,
        override val refreshService: VerifiableCredentialTypeContainer? = null,
        override val evidence: VerifiableCredentialTypeContainer? = null,
        override val termsOfUse: VerifiableCredentialTypeContainer? = null,
        override val id: String,
        override val issuanceDate: String,
        override val expirationDate: String?,
        override val validFrom: VerifiableCredentialTypeContainer? = null,
        override val validUntil: VerifiableCredentialTypeContainer? = null,
        override val proof: JsonString?,
        override val aud: Array<String> = arrayOf()
    ) : VerifiableCredential {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as JWTVerifiableCredential

            if (credentialType != other.credentialType) return false
            if (!context.contentEquals(other.context)) return false
            if (!type.contentEquals(other.type)) return false
            if (issuer != other.issuer) return false
            if (credentialSchema != other.credentialSchema) return false
            if (credentialSubject != other.credentialSubject) return false
            if (credentialStatus != other.credentialStatus) return false
            if (refreshService != other.refreshService) return false
            if (evidence != other.evidence) return false
            if (termsOfUse != other.termsOfUse) return false
            if (id != other.id) return false
            if (issuanceDate != other.issuanceDate) return false
            if (expirationDate != other.expirationDate) return false
            if (validFrom != other.validFrom) return false
            if (validUntil != other.validUntil) return false
            if (proof != other.proof) return false
            if (!aud.contentEquals(other.aud)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = credentialType.hashCode()
            result = 31 * result + context.contentHashCode()
            result = 31 * result + type.contentHashCode()
            result = 31 * result + issuer.hashCode()
            result = 31 * result + (credentialSchema?.hashCode() ?: 0)
            result = 31 * result + credentialSubject.hashCode()
            result = 31 * result + (credentialStatus?.hashCode() ?: 0)
            result = 31 * result + (refreshService?.hashCode() ?: 0)
            result = 31 * result + (evidence?.hashCode() ?: 0)
            result = 31 * result + (termsOfUse?.hashCode() ?: 0)
            result = 31 * result + id.hashCode()
            result = 31 * result + issuanceDate.hashCode()
            result = 31 * result + (expirationDate?.hashCode() ?: 0)
            result = 31 * result + (validFrom?.hashCode() ?: 0)
            result = 31 * result + (validUntil?.hashCode() ?: 0)
            result = 31 * result + (proof?.hashCode() ?: 0)
            result = 31 * result + aud.contentHashCode()
            return result
        }
    }

    override val id: String
        get() = jti
    override val credentialType: CredentialType
        get() = CredentialType.JWT
    override val context: Array<String>
        get() = verifiableCredential.context
    override val type: Array<String>
        get() = verifiableCredential.type
    override val credentialSchema: VerifiableCredentialTypeContainer?
        get() = verifiableCredential.credentialSchema
    override val credentialSubject: String
        get() = verifiableCredential.credentialSubject
    override val credentialStatus: VerifiableCredentialTypeContainer?
        get() = verifiableCredential.credentialStatus
    override val refreshService: VerifiableCredentialTypeContainer?
        get() = verifiableCredential.refreshService
    override val evidence: VerifiableCredentialTypeContainer?
        get() = verifiableCredential.evidence
    override val termsOfUse: VerifiableCredentialTypeContainer?
        get() = verifiableCredential.termsOfUse
    override val issuer: DID
        get() = verifiableCredential.issuer
    override val issuanceDate: String
        get() = verifiableCredential.issuanceDate
    override val expirationDate: String?
        get() = verifiableCredential.expirationDate
    override val validFrom: VerifiableCredentialTypeContainer?
        get() = verifiableCredential.validFrom
    override val validUntil: VerifiableCredentialTypeContainer?
        get() = verifiableCredential.validUntil
    override val proof: JsonString?
        get() = verifiableCredential.proof
    override val aud: Array<String>
        get() = verifiableCredential.aud

    fun toJson(): JsonString {
        val vcJson: MutableMap<String, JsonElement> = mutableMapOf()

        vcJson[CREDENTIAL_TYPE] = JsonPrimitive(this.credentialType.type)
        vcJson[ID] = JsonPrimitive(this.id)
        vcJson[CONTEXT] = JsonArray(this.context.map { JsonPrimitive(it) })
        vcJson[TYPE] = JsonArray(this.type.map { JsonPrimitive(it) })
        vcJson[ISSUER] = JsonPrimitive(this.issuer.toString())

        this.credentialSchema?.let {
            vcJson[CREDENTIAL_SCHEMA] = JsonObject(
                mapOf(
                    ID to JsonPrimitive(it.id),
                    TYPE to JsonPrimitive(it.type),
                ),
            )
        }

        vcJson[CREDENTIAL_SUBJECT] = Json.decodeFromString(this.credentialSubject)

        this.credentialStatus?.let {
            vcJson[CREDENTIAL_STATUS] = JsonObject(
                mapOf(
                    ID to JsonPrimitive(it.id),
                    TYPE to JsonPrimitive(it.type),
                ),
            )
        }

        this.refreshService?.let {
            vcJson[REFRESH_SERVICE] = JsonObject(
                mapOf(
                    ID to JsonPrimitive(it.id),
                    TYPE to JsonPrimitive(it.type),
                ),
            )
        }

        this.evidence?.let {
            vcJson[EVIDENCE] = JsonObject(
                mapOf(
                    ID to JsonPrimitive(it.id),
                    TYPE to JsonPrimitive(it.type),
                ),
            )
        }

        this.termsOfUse?.let {
            vcJson[TERMS_OF_USE] = JsonObject(
                mapOf(
                    ID to JsonPrimitive(it.id),
                    TYPE to JsonPrimitive(it.type),
                ),
            )
        }

        vcJson[ISSUANCE_DATE] = JsonPrimitive(this.issuanceDate)

        if (!this.expirationDate.isNullOrEmpty()) {
            vcJson[EXPIRATION_DATE] = JsonPrimitive(this.expirationDate)
        }

        this.validFrom?.let {
            vcJson[VALID_FROM] = JsonObject(
                mapOf(
                    ID to JsonPrimitive(it.id),
                    TYPE to JsonPrimitive(it.type),
                ),
            )
        }

        this.validUntil?.let {
            vcJson[VALID_UNTIL] = JsonObject(
                mapOf(
                    ID to JsonPrimitive(it.id),
                    TYPE to JsonPrimitive(it.type),
                ),
            )
        }

        this.proof?.let {
            vcJson[PROOF] = Json.decodeFromString(it)
        }

        vcJson[AUD] = JsonArray(this.aud.map { JsonPrimitive(it) })

        val jwtJson = mapOf(
            ISS to JsonPrimitive(this.iss.toString()),
            SUB to this.sub?.let { JsonPrimitive(it) },
            NBF to JsonPrimitive(this.nbf),
            EXP to this.exp?.let { JsonPrimitive(it) },
            JTI to JsonPrimitive(this.jti),
            VC to JsonObject(vcJson),
        )

        return Json.encodeToString(jwtJson)
    }

    companion object {

        @JvmStatic
        fun fromJson(json: JsonString): JWTCredentialPayload {
            val jsonObject = Json.decodeFromString<JsonObject>(json)

            val jsonVc = jsonObject.getCredentialField<JsonObject>(VC)

            val iss = DID(jsonObject.getCredentialField(name = ISS))
            val sub: String? = jsonObject.getCredentialField(name = SUB, isOptional = true)
            val nbf: String = jsonObject.getCredentialField(name = NBF)
            val exp: String? = jsonObject.getCredentialField(name = EXP, isOptional = true)
            val jti: String = jsonObject.getCredentialField(name = JTI)

            if (jsonObject.getCredentialField<String>(CREDENTIAL_TYPE) != CredentialType.JWT.type) {
                throw PolluxError.InvalidCredentialError()
            }

            val vcCredentialSubject: JsonObject = jsonVc.getCredentialField(name = CREDENTIAL_SUBJECT)
            val vcProof: JsonObject? = jsonVc.getCredentialField(name = PROOF, isOptional = true)
            val credentialSubject = Json.encodeToString(vcCredentialSubject)
            val proof = vcProof?.let { Json.encodeToString(it) }

            val verifiableCredential = JWTVerifiableCredential(
                id = jsonVc.getCredentialField(name = ID),
                credentialType = CredentialType.JWT,
                context = jsonVc.getCredentialField(name = CONTEXT),
                type = jsonVc.getCredentialField(name = TYPE),
                credentialSchema = jsonVc.getCredentialField(name = CREDENTIAL_SCHEMA, isOptional = true),
                credentialSubject = credentialSubject,
                credentialStatus = jsonVc.getCredentialField(name = CREDENTIAL_STATUS, isOptional = true),
                refreshService = jsonVc.getCredentialField(name = REFRESH_SERVICE, isOptional = true),
                evidence = jsonVc.getCredentialField(name = EVIDENCE, isOptional = true),
                termsOfUse = jsonVc.getCredentialField(name = TERMS_OF_USE, isOptional = true),
                issuer = jsonVc.getCredentialField(name = ISSUER),
                issuanceDate = jsonVc.getCredentialField(name = ISSUANCE_DATE),
                expirationDate = jsonVc.getCredentialField(name = EXPIRATION_DATE, isOptional = true),
                validFrom = jsonVc.getCredentialField(name = VALID_FROM, isOptional = true),
                validUntil = jsonVc.getCredentialField(name = VALID_UNTIL, isOptional = true),
                proof = proof,
                aud = jsonVc.getCredentialField(name = AUD),
            )

            return JWTCredentialPayload(
                iss = iss,
                verifiableCredential = verifiableCredential,
                nbf = nbf,
                exp = exp,
                jti = jti,
                sub = sub
            )
        }
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
