package org.hyperledger.identus.walletsdk.pollux.models

import anoncreds_uniffi.CredentialDefinition
import anoncreds_uniffi.PresentationRequest
import anoncreds_uniffi.Prover
import anoncreds_uniffi.Schema
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.didcommx.didcomm.common.Typ
import org.hyperledger.identus.walletsdk.domain.models.Api
import org.hyperledger.identus.walletsdk.domain.models.Claim
import org.hyperledger.identus.walletsdk.domain.models.ClaimType
import org.hyperledger.identus.walletsdk.domain.models.Credential
import org.hyperledger.identus.walletsdk.domain.models.CredentialOperationsOptions
import org.hyperledger.identus.walletsdk.domain.models.KeyValue
import org.hyperledger.identus.walletsdk.domain.models.PolluxError
import org.hyperledger.identus.walletsdk.domain.models.ProvableCredential
import org.hyperledger.identus.walletsdk.domain.models.StorableCredential
import org.hyperledger.identus.walletsdk.domain.models.UnknownError
import org.hyperledger.identus.walletsdk.pluto.PlutoRestoreTask
import org.hyperledger.identus.walletsdk.pluto.PlutoRestoreTask.AnonCredentialBackUp.RevocationRegistry

/**
 * Represents an anonymous verifiable credential that contains information about an entity or identity.
 *
 * This class implements the [Credential] interface and provides methods for converting the credential
 * to a storable format and restoring it from a storable format.
 *
 * @param schemaID The ID of the credential's schema.
 * @param credentialDefinitionID The ID of the credential's definition.
 * @param values The values of the attributes in the credential.
 * @param signatureJson The JSON representation of the credential's signature.
 * @param signatureCorrectnessProofJson The JSON representation of the credential's signature correctness proof.
 * @param revocationRegistryId The ID of the credential's revocation registry (optional).
 * @param revocationRegistryJson The JSON representation of the credential's revocation registry (optional).
 * @param witnessJson The JSON representation of the credential's witness.
 * @param json The JSON representation of the credential.
 */
@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class AnonCredential(
    @SerialName("schema_id")
    @JsonNames("schema_id", "schemaID")
    val schemaID: String,
    @SerialName("cred_def_id")
    @JsonNames("cred_def_id", "credentialDefinitionID")
    val credentialDefinitionID: String,
    val values: Map<String, Attribute>,
    @SerialName("signature")
    @JsonNames("signature", "signatureJson")
    val signatureJson: String,
    @SerialName("signature_correctness_proof")
    @JsonNames("signature_correctness_proof", "signatureCorrectnessProofJson")
    val signatureCorrectnessProofJson: String,
    @SerialName("rev_reg_id")
    @JsonNames("revocation_registry_id", "revocationRegistryId", "rev_reg_id")
    val revocationRegistryId: String?,
    @SerialName("rev_reg")
    @JsonNames("revocation_registry", "revocationRegistryJson", "rev_reg")
    val revocationRegistryJson: String?,
    @SerialName("witness")
    @JsonNames("witness", "witnessJson")
    val witnessJson: String?,
    private val json: String
) : Credential, ProvableCredential {

    /**
     * Converts the current object to a [PlutoRestoreTask.AnonCredentialBackUp] object.
     *
     * @return The converted [PlutoRestoreTask.AnonCredentialBackUp] object.
     */
    fun toAnonCredentialBackUp(): PlutoRestoreTask.AnonCredentialBackUp {
        return PlutoRestoreTask.AnonCredentialBackUp(
            schemaID = this.schemaID,
            credentialDefinitionID = this.credentialDefinitionID,
            values = this.values,
            signature = Json.decodeFromString(this.signatureJson),
            signatureCorrectnessProof = Json.decodeFromString(this.signatureCorrectnessProofJson),
            revocationRegistryId = this.revocationRegistryId,
            revocationRegistry = if (this.revocationRegistryJson != null) Json.decodeFromString(this.revocationRegistryJson) else RevocationRegistry(),
            witnessJson = if (this.witnessJson != null) Json.decodeFromString(this.witnessJson) else PlutoRestoreTask.AnonCredentialBackUp.Witness(),
            revoked = this.revoked
        )
    }

    /**
     * Represents an attribute in a verifiable credential.
     *
     * @property raw The raw value of the attribute as a string.
     * @property encoded The encoded value of the attribute as a string.
     */
    @Serializable
    data class Attribute(
        val raw: String,
        val encoded: String
    )

    override val id: String
        get() = json

    override val issuer: String
        get() = ""

    override val subject: String?
        get() = null

    override val claims: Array<Claim>
        get() = values.map {
            Claim(key = it.key, value = ClaimType.StringValue(it.value.raw))
        }.toTypedArray()

    override val properties: Map<String, Any>
        get() {
            val properties = mutableMapOf<String, Any>()
            properties["schemaID"] = this.schemaID
            properties["credentialDefinitionID"] = this.credentialDefinitionID
            properties["signatureJson"] = this.signatureJson
            properties["signatureCorrectnessProofJson"] = this.signatureCorrectnessProofJson

            witnessJson?.let {
                properties["witnessJson"] = it
            }
            revocationRegistryId?.let {
                properties["revocationRegistryId"] = it
            }
            revocationRegistryJson?.let {
                properties["revocationRegistryJson"] = it
            }

            return properties.toMap()
        }

    override var revoked: Boolean? = null

    override suspend fun presentation(request: ByteArray, options: List<CredentialOperationsOptions>): String {
        var schemaDownloader: Api? = null
        var definitionDownloader: Api? = null
        var linkSecret: String? = null

        for (option in options) {
            when (option) {
                is CredentialOperationsOptions.SchemaDownloader -> schemaDownloader = option.api
                is CredentialOperationsOptions.CredentialDefinitionDownloader -> definitionDownloader = option.api
                is CredentialOperationsOptions.LinkSecret -> linkSecret = option.secret
                else -> {}
            }
        }
        if (linkSecret == null) {
            throw UnknownError.SomethingWentWrongError()
        }
        if (schemaDownloader == null) {
            throw UnknownError.SomethingWentWrongError()
        }
        if (definitionDownloader == null) {
            throw UnknownError.SomethingWentWrongError()
        }

        val decodedRequest = request.decodeToString()
        val presentationRequest = PresentationRequest(decodedRequest)
        val cred = anoncreds_uniffi.Credential(this.id)

        val requestedAttributes = extractRequestedAttributes(decodedRequest)
        val requestedPredicates = extractRequestedPredicatesKeys(decodedRequest)

        val credentialRequests = anoncreds_uniffi.RequestedCredential(
            cred = cred,
            timestamp = null,
            revState = null,
            requestedAttributes = requestedAttributes,
            requestedPredicates = requestedPredicates
        )
        val schemaId = this.schemaID
        // When testing using a local instance of an agent, we need to replace the host.docker.internal with the local IP
        // .replace("host.docker.internal", "192.168.68.114")
        val schema = getSchema(schemaId, schemaDownloader)

        val schemaMap: Map<String, Schema> = mapOf(Pair(this.schemaID, schema))

        val credDefId = this.credentialDefinitionID
        // When testing using a local instance of an agent, we need to replace the host.docker.internal with the local IP
        // .replace("host.docker.internal", "192.168.68.114")
        val credentialDefinition = getCredentialDefinition(credDefId, definitionDownloader)
        val credDefinition: Map<String, CredentialDefinition> = mapOf(
            Pair(this.credentialDefinitionID, credentialDefinition)
        )

        return Prover().createPresentation(
            presReq = presentationRequest,
            requestedCredentials = listOf(credentialRequests),
            selfAttestedAttributes = null,
            linkSecret = linkSecret,
            schemas = schemaMap,
            credDefs = credDefinition
        ).toJson()
    }

    /**
     * Converts the current credential object into a storable credential object.
     *
     * @return The converted storable credential object.
     */
    fun toStorableCredential(): StorableCredential {
        val c = this
        return object : StorableCredential {
            override val id: String
                get() = c.id

            override val issuer: String
                get() = c.issuer

            override val subject: String?
                get() = c.subject

            override val claims: Array<Claim>
                get() = c.claims

            override val properties: Map<String, Any?>
                get() = c.properties

            override val recoveryId: String
                get() = "anon+credential"

            override val credentialData: ByteArray
                get() = Json.encodeToString(c).toByteArray()

            override val credentialCreated: String?
                get() = null

            override val credentialUpdated: String?
                get() = null

            override val credentialSchema: String?
                get() = c.schemaID

            override val validUntil: String?
                get() = null

            override var revoked: Boolean? = c.revoked

            override val availableClaims: Array<String>
                get() = c.claims.map { it.key }.toTypedArray()

            /**
             * Converts a storable credential to a regular credential.
             *
             * @return The converted Credential object.
             */
            override fun fromStorableCredential(): Credential {
                return c
            }
        }
    }

    /**
     * Converts the map of [anoncreds_uniffi.AttributeInfoValue] values to a list of [RequestedAttribute].
     *
     * @return The list of [RequestedAttribute].
     */
    private fun extractRequestedAttributes(jsonString: String): Map<String, Boolean> {
        val jsonElement: JsonElement = Json.parseToJsonElement(jsonString)
        val jsonObject: JsonObject = jsonElement.jsonObject
        val requestedAttributes = jsonObject["requested_attributes"]?.jsonObject ?: JsonObject(mapOf())
        val resultMap = requestedAttributes.keys.associateWith { true }

        return resultMap
    }

    /**
     * Converts the map of [anoncreds_uniffi.PredicateInfoValue] values to a list of [RequestedPredicate].
     *
     * @receiver The map of [anoncreds_uniffi.PredicateInfoValue] values.
     * @return The list of [RequestedPredicate].
     */
    private fun extractRequestedPredicatesKeys(jsonString: String): List<String> {
        val jsonElement: JsonElement = Json.parseToJsonElement(jsonString)
        val jsonObject: JsonObject = jsonElement.jsonObject
        val requestedPredicates = jsonObject["requested_predicates"]?.jsonObject ?: JsonObject(mapOf())
        val keysList = requestedPredicates.keys.toList()

        return keysList
    }

    /**
     * Retrieves the credential definition for the specified ID.
     *
     * @param id The ID of the credential definition.
     * @return The credential definition.
     */
    private suspend fun getCredentialDefinition(id: String, api: Api): CredentialDefinition {
        val result = api.request(
            HttpMethod.Get.value,
            id,
            emptyArray(),
            arrayOf(KeyValue(HttpHeaders.ContentType, Typ.Encrypted.typ)),
            null
        )
        if (result.status == 200) {
            return CredentialDefinition(result.jsonString)
        }
        throw PolluxError.InvalidCredentialDefinitionError()
    }

    private suspend fun getSchema(schemaId: String, api: Api): Schema {
        val result = api.request(
            HttpMethod.Get.value,
            schemaId,
            emptyArray(),
            arrayOf(KeyValue(HttpHeaders.ContentType, Typ.Encrypted.typ)),
            null
        )

        if (result.status == 200) {
            val schema = (Json.parseToJsonElement(result.jsonString) as JsonObject)
            if (schema.containsKey("attrNames") && schema.containsKey("issuerId")) {
                val name = schema["name"]?.jsonPrimitive?.content
                val version = schema["version"]?.jsonPrimitive?.content
                val attrs = schema["attrNames"]
                val attrNames = attrs?.jsonArray?.map { value -> value.jsonPrimitive.content }
                val issuerId =
                    schema["issuerId"]?.jsonPrimitive?.content
                if (name == null || version == null || attrNames == null || issuerId == null) {
                    throw PolluxError.InvalidCredentialError()
                }
                return Schema(result.jsonString)
            }
        }
        throw PolluxError.InvalidCredentialDefinitionError()
    }

    companion object {
        /**
         * Converts a byte array of storable credential data to an AnonCredential object.
         *
         * @param data The byte array containing the storable credential data.
         * @return The converted AnonCredential object.
         */
        @JvmStatic
        fun fromStorableData(data: ByteArray): AnonCredential {
            val dataString = data.decodeToString()
            val cred = Json.decodeFromString<AnonCredential>(dataString)
            return cred
        }
    }
}
