package org.hyperledger.identus.walletsdk.pollux.models

import eu.europa.ec.eudi.sdjwt.JsonPointer
import eu.europa.ec.eudi.sdjwt.JwtAndClaims
import eu.europa.ec.eudi.sdjwt.JwtSignatureVerifier
import eu.europa.ec.eudi.sdjwt.NoSignatureValidation
import eu.europa.ec.eudi.sdjwt.SdJwt
import eu.europa.ec.eudi.sdjwt.SdJwtVerifier
import eu.europa.ec.eudi.sdjwt.present
import eu.europa.ec.eudi.sdjwt.serialize
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Transient
import kotlinx.serialization.builtins.ArraySerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonTransformingSerializer
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonPrimitive
import org.hyperledger.identus.walletsdk.domain.models.Claim
import org.hyperledger.identus.walletsdk.domain.models.ClaimType
import org.hyperledger.identus.walletsdk.domain.models.Credential
import org.hyperledger.identus.walletsdk.domain.models.CredentialOperationsOptions
import org.hyperledger.identus.walletsdk.domain.models.PolluxError
import org.hyperledger.identus.walletsdk.domain.models.ProvableCredential
import org.hyperledger.identus.walletsdk.domain.models.StorableCredential
import java.util.*

@OptIn(ExperimentalSerializationApi::class)
data class SDJWTCredential(
    val sdjwtString: String,
    val sdjwt: SdJwt.Issuance<JwtAndClaims>
) : Credential, ProvableCredential {
    override val id: String
        get() = sdjwtString

    @Transient
    override val issuer: String = sdjwt.jwt.second["iss"]?.jsonPrimitive?.content
        ?: throw PolluxError.InvalidJWTCredential("SD-JWT must contain issuer")

    override val subject: String?
        get() = sdjwt.jwt.second["sub"]?.jsonPrimitive?.content

    override val claims: Array<Claim>
        get() {
            return sdjwt.jwt.second.map {
                Claim(key = it.key, value = ClaimType.StringValue(it.value.toString()))
            }?.toTypedArray()
                ?: emptyArray<Claim>()
        }

    override val properties: Map<String, Any?>
        get() {
            val properties = mutableMapOf<String, Any?>()
            properties["nbf"] = sdjwt.jwt.second["nbf"]?.jsonPrimitive?.content
            properties["jti"] = sdjwt.jwt.second["sub"]?.jsonPrimitive?.content
            properties["aud"] = sdjwt.jwt.second["aud"]?.jsonPrimitive?.content
            properties["id"] = id

            sdjwt.jwt.second["exp"]?.jsonPrimitive?.content.let { properties["exp"] = it }
            return properties.toMap()
        }

    override var revoked: Boolean? = null

    override suspend fun presentation(
        attachmentFormat: String,
        request: ByteArray,
        options: List<CredentialOperationsOptions>
    ): String {
        val jwtPresentationDefinitionRequest =
            Json.decodeFromString<SDJWTPresentationDefinitionRequest>(String(request, Charsets.UTF_8))
        val descriptorItems =
            jwtPresentationDefinitionRequest.presentationDefinition.inputDescriptors.map { inputDescriptor ->
                if (inputDescriptor.format != null && (inputDescriptor.format.sdjwt == null || inputDescriptor.format.sdjwt.alg.isEmpty())) {
                    throw PolluxError.InvalidCredentialDefinitionError()
                }
                PresentationSubmission.Submission.DescriptorItem(
                    id = inputDescriptor.id,
                    format = DescriptorItemFormat.SD_JWT_VP.value,
                    path = "$.verifiablePresentation[0]"
                )
            }.toTypedArray()

        var disclosingClaims: List<String>? = null

        for (option in options) {
            when (option) {
                is CredentialOperationsOptions.DisclosingClaims -> disclosingClaims = option.claims
                else -> {}
            }
        }

        val included = disclosingClaims
            ?.mapNotNull { JsonPointer.parse(it) }
            ?.toSet()

        val presentation = sdjwt.present(included!!)
        val sdjwtString = presentation!!.serialize { (jwt, _) -> jwt }

        return Json.encodeToString(
            PresentationSubmission(
                presentationSubmission = PresentationSubmission.Submission(
                    definitionId = jwtPresentationDefinitionRequest.presentationDefinition.id
                        ?: UUID.randomUUID().toString(),
                    descriptorMap = descriptorItems
                ),
                verifiablePresentation = arrayOf(sdjwtString)
            )
        )
    }

    /**
     * Converts the current instance of [SDJWTCredential] to a [StorableCredential].
     *
     * @return The converted [StorableCredential].
     */
    fun toStorableCredential(): StorableCredential {
        val c = this
        return object : StorableCredential {
            override val id: String
                get() = c.id
            override val recoveryId: String
                get() = "sd-jwt+credential"
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
                get() = null
            override val validUntil: String?
                get() = c.sdjwt.jwt.second["exp"]?.jsonPrimitive?.content
            override var revoked: Boolean? = c.revoked
            override val availableClaims: Array<String>
                get() = c.claims.map { it.key }.toTypedArray()

            override val claims: Array<Claim>
                get() = sdjwt.jwt.second.map {
                    Claim(key = it.key, value = ClaimType.StringValue(it.value.toString()))
                }?.toTypedArray() ?: emptyArray()

            override val properties: Map<String, Any?>
                get() {
                    val properties = mutableMapOf<String, Any?>()
                    properties["nbf"] = sdjwt.jwt.second["nbf"]?.jsonPrimitive?.content
                    properties["jti"] = sdjwt.jwt.second["jti"]?.jsonPrimitive?.content
                    properties["aud"] = sdjwt.jwt.second["aud"]?.jsonPrimitive?.content
                    properties["id"] = id

                    sdjwt.jwt.second["exp"]?.jsonPrimitive?.content.let { properties["exp"] = it }
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

    @OptIn(ExperimentalSerializationApi::class)
    object AudSerializer : JsonTransformingSerializer<Array<String>>(ArraySerializer(String.serializer())) {
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
        @JvmStatic
        fun fromSDJwtString(sdjwtString: String): SDJWTCredential {
            var credential: SDJWTCredential
            runBlocking {
                val sdjwt =
                    SdJwtVerifier.verifyIssuance(JwtSignatureVerifier.NoSignatureValidation, sdjwtString).getOrThrow()
                credential = SDJWTCredential(sdjwtString, sdjwt)
            }
            return credential
        }
    }
}
