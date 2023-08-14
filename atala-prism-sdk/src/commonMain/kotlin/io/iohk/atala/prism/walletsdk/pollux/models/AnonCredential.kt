package io.iohk.atala.prism.walletsdk.pollux.models

import io.iohk.atala.prism.walletsdk.domain.models.Claim
import io.iohk.atala.prism.walletsdk.domain.models.ClaimType
import io.iohk.atala.prism.walletsdk.domain.models.Credential
import io.iohk.atala.prism.walletsdk.domain.models.StorableCredential

data class AnonCredential @JvmOverloads constructor(
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

    data class Attribute(
        val raw: String,
        val encoded: String
    )

    override val id: String
        get() = json

    override
    val issuer: String
        get() = ""

    override
    val subject: String?
        get() = null

    override
    val claims: Array<Claim>
        get() = values.map {
            Claim(key = it.key, value = ClaimType.StringValue(it.value.raw))
        }.toTypedArray()

    override
    val properties: Map<String, Any>
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
                get() = c.json.toByteArray()

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
        }
    }
}
