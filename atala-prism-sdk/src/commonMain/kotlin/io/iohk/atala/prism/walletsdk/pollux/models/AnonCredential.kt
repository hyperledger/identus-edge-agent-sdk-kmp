package io.iohk.atala.prism.walletsdk.pollux.models

import io.iohk.atala.prism.walletsdk.domain.models.Claim
import io.iohk.atala.prism.walletsdk.domain.models.ClaimType
import io.iohk.atala.prism.walletsdk.domain.models.Credential
import io.iohk.atala.prism.walletsdk.domain.models.StorableCredential
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

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
@Serializable
data class AnonCredential(
    val schemaID: String,
    val credentialDefinitionID: String,
    val values: Map<String, Attribute>,
    val signatureJson: String,
    val signatureCorrectnessProofJson: String,
    val revocationRegistryId: String?,
    val revocationRegistryJson: String?,
    val witnessJson: String,
    private val json: String
) : Credential {

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
            properties["witnessJson"] = this.witnessJson

            revocationRegistryId?.map { properties["revocationRegistryId"] = it }
            revocationRegistryJson?.map { properties["revocationRegistryJson"] = it }

            return properties.toMap()
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

            override val revoked: Boolean?
                get() = null

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
