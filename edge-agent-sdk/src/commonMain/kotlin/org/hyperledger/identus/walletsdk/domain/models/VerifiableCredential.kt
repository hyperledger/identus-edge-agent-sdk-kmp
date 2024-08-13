package org.hyperledger.identus.walletsdk.domain.models

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encodeToString
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.double
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.float
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.serializer

/**
 * A data class representing a container for verifiable credential types.
 * This data class is used to encode and decode verifiable credential types for use with JSON.
 * The VerifiableCredentialTypeContainer contains properties for the ID and type of the verifiable credential.
 * ::: info
 * The VerifiableCredentialTypeContainer is used to encode and decode verifiable credential types for use with JSON.
 * :::
 */
@Serializable
data class VerifiableCredentialTypeContainer(
    val id: String,
    val type: String
)

/**
 * Enum class representing different types of verifiable credentials.
 * The CredentialType is used to indicate the type of verifiable credential.
 * The possible values of the enum are jwt, w3c, and unknown.
 *
 * ::: info
 * The CredentialType enum is used to indicate the type of verifiable credential.
 * :::
 */
@Serializable
enum class CredentialType(val type: String) {
    JWT("prism/jwt"),
    W3C("w3c"),
    SDJWT("vc+sd-jwt"),
    ANONCREDS_OFFER("anoncreds/credential-offer@v1.0"),
    ANONCREDS_REQUEST("anoncreds/credential-request@v1.0"),
    ANONCREDS_ISSUE("anoncreds/credential@v1.0"),
    ANONCREDS_PROOF_REQUEST("anoncreds/proof-request@v1.0"),
    PRESENTATION_EXCHANGE_DEFINITIONS("dif/presentation-exchange/definitions@v1.0"),
    PRESENTATION_EXCHANGE_SUBMISSION("dif/presentation-exchange/submission@v1.0"),
    Unknown("Unknown")
}

/**
 * Interface for objects representing verifiable credentials.
 */
@Serializable
sealed interface VerifiableCredential {
    val id: String
    val credentialType: CredentialType
    val context: Array<String>
    val type: Array<String>
    val credentialSchema: VerifiableCredentialTypeContainer?
    val credentialSubject: String
    val credentialStatus: VerifiableCredentialTypeContainer?
    val refreshService: VerifiableCredentialTypeContainer?
    val evidence: VerifiableCredentialTypeContainer?
    val termsOfUse: VerifiableCredentialTypeContainer?
    val issuer: DID?
    val issuanceDate: String // TODO(Date)
    val expirationDate: String? // TODO(Date)
    val validFrom: VerifiableCredentialTypeContainer?
    val validUntil: VerifiableCredentialTypeContainer?
    val proof: JsonString?
    val aud: Array<String>

    /**
     * Converts the object to a JSON string representation.
     *
     * @return The JSON string representation of the object.
     */
    fun toJsonString(): String {
        return Json.encodeToString(this)
    }
}

sealed interface PresentationClaims

data class JWTPresentationClaims(
    val schema: String? = null,
    val issuer: String? = null,
    val claims: Map<String, InputFieldFilter>
) : PresentationClaims

data class AnoncredsPresentationClaims(
    val predicates: Map<String, AnoncredsInputFieldFilter>,
    val attributes: Map<String, RequestedAttributes>
) : PresentationClaims

@OptIn(ExperimentalSerializationApi::class)
@Serializable(with = InputFieldFilterSerializer::class)
data class InputFieldFilter @JvmOverloads constructor(
    val type: String,
    @EncodeDefault
    val pattern: String? = null,
    @EncodeDefault
    val enum: List<Any>? = null,
    @EncodeDefault
    val const: List<Any>? = null,
    @EncodeDefault
    val value: Any? = null
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other != null && this::class != other::class) return false

        other as InputFieldFilter

        if (type != other.type) return false
        if (pattern != other.pattern) return false
        // if (!enum.contentEquals(other.enum)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + (pattern?.hashCode() ?: 0)
        // result = 31 * result + enum.contentHashCode()
        return result
    }
}

data class AnoncredsInputFieldFilter(
    val type: String,
    val name: String,
    val gt: Any? = null,
    val gte: Any? = null,
    val lt: Any? = null,
    val lte: Any? = null
)

/**
 * Custom serializer for InputFieldFilter. Used to serialized List<Any> contained by InputFieldFilter.
 */
object InputFieldFilterSerializer : KSerializer<InputFieldFilter> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("InputFieldFilter") {
        element<String>("type")
        element<String?>("pattern")
        element("enum", PolymorphicSerializer(Any::class).descriptor, isOptional = true)
        element("const", PolymorphicSerializer(Any::class).descriptor, isOptional = true)
        element("value", PolymorphicSerializer(Any::class).descriptor, isOptional = true)
    }

    /**
     * Deserializes the input data from the provided decoder into an instance of the InputFieldFilter class.
     *
     * @param decoder The decoder used to decode the input data.
     * @return An instance of the InputFieldFilter class.
     * @throws SerializationException if an unknown index is encountered during deserialization or if the "type" property is missing.
     */
    @OptIn(ExperimentalSerializationApi::class)
    override fun deserialize(decoder: Decoder): InputFieldFilter {
        val dec: CompositeDecoder = decoder.beginStructure(descriptor)
        var type: String? = null
        var pattern: String? = null
        var enum: List<Any>? = null
        var const: List<Any>? = null
        var value: Any? = null

        loop@ while (true) {
            when (val index = dec.decodeElementIndex(descriptor)) {
                CompositeDecoder.DECODE_DONE -> break@loop
                0 ->
                    type = dec.decodeStringElement(descriptor, index)

                1 ->
                    pattern =
                        dec.decodeNullableSerializableElement(
                            descriptor,
                            index,
                            serializer<String?>()
                        )

                2 -> {
                    val jsonElement =
                        dec.decodeNullableSerializableElement(
                            descriptor,
                            index,
                            JsonElement.serializer()
                        )
                    enum = when (jsonElement) {
                        is JsonArray -> jsonElement.jsonArray.map { element ->
                            val jsonPrimitive = element.jsonPrimitive
                            when {
                                jsonPrimitive.isString -> jsonPrimitive.content
                                jsonPrimitive.floatOrNull != null -> jsonPrimitive.float
                                jsonPrimitive.intOrNull != null -> jsonPrimitive.int
                                jsonPrimitive.doubleOrNull != null -> jsonPrimitive.double
                                jsonPrimitive.longOrNull != null -> jsonPrimitive.long
                                else -> jsonPrimitive.content
                            }
                        }

                        is JsonPrimitive -> listOf(jsonElement.content)
                        else -> null
                    }
                }

                3 -> {
                    val jsonElement =
                        dec.decodeNullableSerializableElement(
                            descriptor,
                            index,
                            JsonElement.serializer()
                        )
                    const = when (jsonElement) {
                        is JsonArray -> jsonElement.jsonArray.map { element ->
                            val jsonPrimitive = element.jsonPrimitive
                            when {
                                jsonPrimitive.isString -> jsonPrimitive.content
                                jsonPrimitive.intOrNull != null -> jsonPrimitive.int
                                jsonPrimitive.floatOrNull != null -> jsonPrimitive.float
                                jsonPrimitive.doubleOrNull != null -> jsonPrimitive.double
                                jsonPrimitive.longOrNull != null -> jsonPrimitive.long
                                else -> jsonPrimitive.content
                            }
                        }

                        is JsonPrimitive -> listOf(jsonElement.content)
                        else -> null
                    }
                }

                4 -> {
                    val jsonElement =
                        dec.decodeNullableSerializableElement(
                            descriptor,
                            index,
                            JsonElement.serializer()
                        )
                    value = when (jsonElement) {
                        is JsonPrimitive -> when {
                            jsonElement.isString -> jsonElement.content
                            jsonElement.booleanOrNull != null -> jsonElement.boolean
                            jsonElement.intOrNull != null -> jsonElement.int
                            jsonElement.doubleOrNull != null -> jsonElement.double
                            else -> jsonElement.content // or some default handling
                        }
                        // Add handling for other types (e.g., JsonArray, JsonObject) if needed
                        else -> jsonElement?.jsonObject // or some default handling
                    }
                }

                else -> throw SerializationException("Unknown index $index")
            }
        }

        dec.endStructure(descriptor)
        return InputFieldFilter(
            type ?: throw Exception("type"),
            pattern,
            enum,
            const,
            value
        )
    }

    /**
     * Serializes an [InputFieldFilter] object using the provided [Encoder].
     *
     * @param encoder The encoder used for serialization.
     * @param value The [InputFieldFilter] object to be serialized.
     */
    @OptIn(ExperimentalSerializationApi::class)
    override fun serialize(encoder: Encoder, value: InputFieldFilter) {
        val compositeOutput = encoder.beginStructure(descriptor)
        compositeOutput.encodeStringElement(descriptor, 0, value.type)
        value.pattern?.let { compositeOutput.encodeStringElement(descriptor, 1, it) }
        compositeOutput.shouldEncodeElementDefault(descriptor, 1)

        value.enum?.let {
            if (it.isNotEmpty()) {
                if (it[0] is String) {
                    compositeOutput.encodeSerializableElement(
                        descriptor,
                        2,
                        ListSerializer(serializer<String>()),
                        it as List<String>
                    )
                } else if (it[0] is Int) {
                    compositeOutput.encodeSerializableElement(
                        descriptor,
                        2,
                        ListSerializer(serializer<Int>()),
                        it as List<Int>
                    )
                } else if (it[0] is Float) {
                    compositeOutput.encodeSerializableElement(
                        descriptor,
                        2,
                        ListSerializer(serializer<Float>()),
                        it as List<Float>
                    )
                } else if (it[0] is Double) {
                    compositeOutput.encodeSerializableElement(
                        descriptor,
                        2,
                        ListSerializer(serializer<Double>()),
                        it as List<Double>
                    )
                } else {
                    throw Exception("Unknown data type of array during seralization of `InputFieldFilter`")
                }
            } else {
                compositeOutput.encodeNullableSerializableElement(
                    descriptor,
                    2,
                    ListSerializer(PolymorphicSerializer(Any::class)),
                    null
                )
            }
        }
        compositeOutput.shouldEncodeElementDefault(descriptor, 2)

        value.const?.let {
            if (it.isNotEmpty()) {
                if (it[0] is String) {
                    compositeOutput.encodeSerializableElement(
                        descriptor,
                        3,
                        ListSerializer(serializer<String>()),
                        it as List<String>
                    )
                } else if (it[0] is Int) {
                    compositeOutput.encodeSerializableElement(
                        descriptor,
                        3,
                        ListSerializer(serializer<Int>()),
                        it as List<Int>
                    )
                } else if (it[0] is Float) {
                    compositeOutput.encodeSerializableElement(
                        descriptor,
                        3,
                        ListSerializer(serializer<Float>()),
                        it as List<Float>
                    )
                } else if (it[0] is Double) {
                    compositeOutput.encodeSerializableElement(
                        descriptor,
                        3,
                        ListSerializer(serializer<Double>()),
                        it as List<Double>
                    )
                } else {
                    throw Exception("Unknown data type of array during seralization of `InputFieldFilter`")
                }
            } else {
                compositeOutput.encodeNullableSerializableElement(
                    descriptor,
                    3,
                    ListSerializer(PolymorphicSerializer(Any::class)),
                    null
                )
            }
        }
        compositeOutput.shouldEncodeElementDefault(descriptor, 3)

        value.value?.let {
            when (it) {
                is String -> {
                    compositeOutput.encodeStringElement(descriptor, 4, it)
                }

                is Float -> {
                    compositeOutput.encodeFloatElement(descriptor, 4, it)
                }

                is Int -> {
                    compositeOutput.encodeIntElement(descriptor, 4, it)
                }

                is Double -> {
                    compositeOutput.encodeDoubleElement(descriptor, 4, it)
                }

                else -> {
                    throw Exception("Unknown data type of value during seralization of `InputFieldFilter`")
                }
            }
        }
        compositeOutput.shouldEncodeElementDefault(descriptor, 4)
        compositeOutput.endStructure(descriptor)
    }
}

@Serializable
data class RequestedAttributes(
    val name: String,
    val names: Set<String>,
    val restrictions: Map<String, String>,
    @SerialName("non_revoked")
    val nonRevoked: NonRevoked?
)

@Serializable
data class RequestedPredicates(
    val name: String,
    @SerialName("p_type")
    val pType: String,
    @SerialName("p_value")
    val pValue: Int,
    val restrictions: Map<String, String>,
    @SerialName("non_revoked")
    val nonRevoked: NonRevoked?
)

@Serializable
data class NonRevoked(
    val from: Long,
    val to: Long
)
