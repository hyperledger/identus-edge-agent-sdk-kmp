package org.hyperledger.identus.walletsdk.domain.models

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.encodeStructure
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

interface JWTPayload {
    val iss: String
    val sub: String?
    val nbf: Long?
    val exp: Long?
    val jti: String?
    val aud: Array<String>?
    val originalJWTString: String?
    val verifiablePresentation: JWTVerifiablePresentation?
    val verifiableCredential: JWTVerifiableCredential?
}

object MapStringAnyToStringSerializer : KSerializer<Map<String, String>> {
    override val descriptor = MapSerializer(String.serializer(), String.serializer()).descriptor

    override fun serialize(encoder: Encoder, value: Map<String, String>) {
        // Use the default MapSerializer for serialization
        encoder.encodeSerializableValue(
            MapSerializer(String.serializer(), String.serializer()),
            value
        )
    }

    override fun deserialize(decoder: Decoder): Map<String, String> {
        // Decode as a JsonObject
        val jsonObject = decoder.decodeSerializableValue(JsonObject.serializer())

        // Transform each value in the JsonObject to String
        return jsonObject.mapValues { (_, value) ->
            when (value) {
                is JsonPrimitive -> value.content
                is JsonElement -> value.jsonPrimitive.content
                else -> value.toString() // Default toString to handle non-primitive cases
            }
        }
    }
}

/**
 * A struct representing the verifiable credential in a JWT credential payload.
 */
@Serializable
data class JWTVerifiableCredential @JvmOverloads constructor(
    @SerialName("@context")
    val context: Array<String> = arrayOf(),
    val type: Array<String> = arrayOf(),
    val credentialSchema: VerifiableCredentialTypeContainer? = null,
    @Serializable(with = MapStringAnyToStringSerializer::class)
    val credentialSubject: Map<String, String>,
    val credentialStatus: CredentialStatus? = null,
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

    @Serializable(with = CredentialStatusSerializer::class)
    data class CredentialStatus(
        val id: String,
        val type: CredentialStatusListType,
        val statusPurpose: CredentialStatusPurpose,
        val statusListIndex: Int,
        val statusListCredential: String
    )

    enum class CredentialStatusListType(val type: String) {
        // The naming does not follow the proper kotlin format, but it is required to work around a
        // situation when serializing an object containing an instance of this enum from nimbus library
        // using java serialization instead of kotlin x.
        StatusList2021Entry("StatusList2021Entry");

        companion object {
            fun fromString(type: String): CredentialStatusListType {
                return entries.first { it.type.lowercase() == type.lowercase() }
            }
        }
    }

    enum class CredentialStatusPurpose(val purpose: String) {
        REVOCATION("revocation"),
        SUSPENSION("suspension");

        companion object {
            fun fromString(purpose: String): CredentialStatusPurpose {
                return entries.first { it.purpose.lowercase() == purpose.lowercase() }
            }
        }
    }

    object CredentialStatusSerializer : KSerializer<CredentialStatus> {
        override val descriptor: SerialDescriptor = buildClassSerialDescriptor("credentialStatus") {
            element<String>("id")
            element<String>("type")
            element<String>("statusPurpose")
            element<Int>("statusListIndex")
            element<String>("statusListCredential")
        }

        override fun serialize(encoder: Encoder, value: CredentialStatus) {
            encoder.encodeStructure(descriptor) {
                encodeStringElement(descriptor, 0, value.id)
                encodeStringElement(descriptor, 1, value.type.type)
                encodeStringElement(descriptor, 2, value.statusPurpose.purpose)
                encodeIntElement(descriptor, 3, value.statusListIndex)
                encodeStringElement(descriptor, 4, value.statusListCredential)
            }
        }

        override fun deserialize(decoder: Decoder): CredentialStatus {
            require(decoder is JsonDecoder)
            val jsonObject = decoder.decodeJsonElement().jsonObject

            val id = jsonObject["id"]?.jsonPrimitive?.content ?: throw SerializationException("Missing 'id'")
            val type = CredentialStatusListType.fromString(
                jsonObject["type"]?.jsonPrimitive?.content ?: throw SerializationException("Missing 'type'")
            )

            val statusPurpose = CredentialStatusPurpose.fromString(
                jsonObject["statusPurpose"]?.jsonPrimitive?.content
                    ?: throw SerializationException("Missing 'statusPurpose'")
            )
            val statusListIndex = jsonObject["statusListIndex"]?.jsonPrimitive?.int ?: throw SerializationException(
                "Missing 'statusListIndex'"
            )
            val statusListCredential = jsonObject["statusListCredential"]?.jsonPrimitive?.content
                ?: throw SerializationException("Missing 'statusListCredential'")

            return CredentialStatus(
                id = id,
                type = type,
                statusPurpose = statusPurpose,
                statusListIndex = statusListIndex,
                statusListCredential = statusListCredential
            )
        }
    }
}

@Serializable
data class JWTVerifiablePresentation(
    @SerialName("@context")
    val context: Array<String>,
    val type: Array<String>,
    val verifiableCredential: Array<String>
) {
    /**
     * This method is used to check if the current object is equal to the given object.
     *
     * @param other The object to compare with.
     * @return True if the two objects are equal, false otherwise.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as JWTVerifiablePresentation

        if (!context.contentEquals(other.context)) return false
        if (!type.contentEquals(other.type)) return false
        if (!verifiableCredential.contentEquals(other.verifiableCredential)) return false

        return true
    }

    /**
     * Computes the hash code value of this object.
     *
     * The hash code is generated by applying the 31 * result + contentHashCode() formula to each property,
     * where result is initialized with the contentHashCode() of the context property.
     *
     * @return the computed hash code value of this object.
     */
    override fun hashCode(): Int {
        var result = context.contentHashCode()
        result = 31 * result + type.contentHashCode()
        result = 31 * result + verifiableCredential.contentHashCode()
        return result
    }
}
