package org.hyperledger.identus.walletsdk.pollux.models

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.hyperledger.identus.walletsdk.domain.models.InputFieldFilter
import org.hyperledger.identus.walletsdk.domain.models.RequestedAttributes
import org.hyperledger.identus.walletsdk.domain.models.RequestedPredicates
import java.util.UUID

@Serializable
sealed interface PresentationDefinitionRequest

@Serializable
data class AnoncredsPresentationDefinitionRequest(
    val nonce: String,
    val name: String,
    val version: String,
    @SerialName("requested_predicates")
    val requestedPredicates: Map<String, RequestedPredicates>,
    @SerialName("requested_attributes")
    val requestedAttributes: Map<String, RequestedAttributes>
) : PresentationDefinitionRequest

@Serializable
data class JWTPresentationDefinitionRequest(
    @SerialName("presentation_definition")
    val presentationDefinition: PresentationDefinition,
    val options: PresentationDefinitionOptions
) : PresentationDefinitionRequest {

    @Serializable
    data class PresentationDefinitionOptions(
        val domain: String,
        val challenge: String
    )
}

@Serializable
data class SDJWTPresentationDefinitionRequest(
    @SerialName("presentation_definition")
    val presentationDefinition: PresentationDefinition
) : PresentationDefinitionRequest

@Serializable
data class PresentationDefinition(
    val id: String? = UUID.randomUUID().toString(),
    @SerialName("input_descriptors")
    val inputDescriptors: Array<InputDescriptor>,
    val format: InputDescriptor.PresentationFormat
) {
    @Serializable
    data class InputDescriptor(
        val id: String = UUID.randomUUID().toString(),
        val name: String? = null,
        val purpose: String? = null,
        val format: PresentationFormat? = null,
        val constraints: Constraints
    ) {

        @Serializable
        data class Constraints @JvmOverloads constructor(
            val fields: Array<Field>? = null,
            @SerialName("limit_disclosure")
            val limitDisclosure: LimitDisclosure? = null
        ) {
            @Serializable
            data class Field @JvmOverloads constructor(
                val path: Array<String>,
                val id: String? = null,
                val purpose: String? = null,
                val name: String? = null,
                val filter: InputFieldFilter? = null,
                val optional: Boolean = false
            ) {
                @Serializable
                data class Filter(
                    val type: String,
                    val pattern: String
                )

                override fun equals(other: Any?): Boolean {
                    if (this === other) {
                        return true
                    }
                    if (javaClass != other?.javaClass) {
                        return false
                    }

                    other as Field

                    if (!path.contentEquals(other.path)) {
                        return false
                    }
                    if (id != other.id) {
                        return false
                    }
                    if (purpose != other.purpose) {
                        return false
                    }
                    if (name != other.name) {
                        return false
                    }
                    if (filter != other.filter) {
                        return false
                    }
                    if (optional != other.optional) {
                        return false
                    }

                    return true
                }

                override fun hashCode(): Int {
                    var result = path.contentHashCode()
                    result = 31 * result + (id?.hashCode() ?: 0)
                    result = 31 * result + (purpose?.hashCode() ?: 0)
                    result = 31 * result + (name?.hashCode() ?: 0)
                    result = 31 * result + (filter?.hashCode() ?: 0)
                    result = 31 * result + optional.hashCode()
                    return result
                }
            }

            @Serializable(with = LimitDisclosure.LimitDisclosureSerializer::class)
            enum class LimitDisclosure(val value: String) {
                REQUIRED("required"),
                PREFERRED("preferred");

                // Custom serializer for the enum
                object LimitDisclosureSerializer : KSerializer<LimitDisclosure> {
                    override val descriptor: SerialDescriptor =
                        PrimitiveSerialDescriptor("LimitDisclosure", PrimitiveKind.STRING)

                    override fun serialize(encoder: Encoder, value: LimitDisclosure) {
                        encoder.encodeString(value.value)
                    }

                    override fun deserialize(decoder: Decoder): LimitDisclosure {
                        val stringValue = decoder.decodeString()
                        return entries.firstOrNull { it.value == stringValue }
                            ?: throw SerializationException("Unknown value: $stringValue")
                    }
                }
            }

            override fun equals(other: Any?): Boolean {
                if (this === other) {
                    return true
                }
                if (javaClass != other?.javaClass) {
                    return false
                }

                other as Constraints

                if (fields != null) {
                    if (other.fields == null) {
                        return false
                    }
                    if (!fields.contentEquals(other.fields)) {
                        return false
                    }
                } else if (other.fields != null) {
                    return false
                }
                if (limitDisclosure != other.limitDisclosure) {
                    return false
                }

                return true
            }

            override fun hashCode(): Int {
                var result = fields?.contentHashCode() ?: 0
                result = 31 * result + (limitDisclosure?.hashCode() ?: 0)
                return result
            }
        }

        @Serializable
        data class PresentationFormat(
            @SerialName("jwt")
            val jwt: JwtFormat? = null,
            @SerialName("sdJwt")
            val sdjwt: JwtFormat? = null,
        )

        @Serializable
        data class JwtFormat(
            val alg: List<String>
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || this::class != other::class) {
            return false
        }

        other as PresentationDefinition

        if (id != other.id) {
            return false
        }
        if (!inputDescriptors.contentEquals(other.inputDescriptors)) {
            return false
        }
        return format == other.format
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + inputDescriptors.contentHashCode()
        result = 31 * result + format.hashCode()
        return result
    }
}
