package io.iohk.atala.prism.walletsdk.domain.models

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.double
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.float
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.long
import kotlinx.serialization.json.longOrNull

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

object AnySerializer : KSerializer<Any> {

    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Any") {
        element("value", PolymorphicSerializer(Any::class).descriptor)
    }

    override fun deserialize(decoder: Decoder): Any {
        val dec: CompositeDecoder = decoder.beginStructure(descriptor)
        var value: Any? = null

        val jsonElement = dec.decodeSerializableElement(descriptor, 0, JsonElement.serializer())
        value = when (jsonElement) {
            is JsonPrimitive -> {
                when {
                    jsonElement.isString -> jsonElement.content
                    jsonElement.floatOrNull != null -> jsonElement.float
                    jsonElement.intOrNull != null -> jsonElement.int
                    jsonElement.doubleOrNull != null -> jsonElement.double
                    jsonElement.longOrNull != null -> jsonElement.long
                    else -> jsonElement.content
                }
            }

            else -> ""
        }
        dec.endStructure(descriptor)
        return value
    }

    override fun serialize(encoder: Encoder, value: Any) {
        val output = encoder.beginStructure(descriptor)
        if (value is String) {
            output.encodeStringElement(descriptor, 0, value)
        } else if (value is Int) {
            output.encodeIntElement(descriptor, 0, value)
        } else if (value is Float) {
            output.encodeFloatElement(descriptor, 0, value)
        } else if (value is Boolean) {
            output.encodeBooleanElement(descriptor, 0, value)
        } else {
            throw SerializationException("Unsupported type")
        }
        output.endStructure(descriptor)
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
    val credentialSubject: Map<String, @Serializable(with = AnySerializer::class) Any>,
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

@Serializable
data class JWTVerifiablePresentation(
    @SerialName("@context")
    val context: Array<String>,
    val type: Array<String>,
    val verifiableCredential: Array<String>
)
