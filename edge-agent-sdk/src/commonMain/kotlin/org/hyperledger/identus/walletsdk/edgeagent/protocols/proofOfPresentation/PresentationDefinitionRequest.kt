@file:Suppress("ktlint:standard:import-ordering")

package org.hyperledger.identus.walletsdk.edgeagent.protocols.proofOfPresentation

import java.util.UUID
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

@Serializable
data class PresentationDefinitionRequest(
    @SerialName("presentation_definition")
    val presentationDefinition: PresentationDefinition,
    val options: PresentationDefinitionOptions
) {

    @Serializable
    data class PresentationDefinitionOptions(
        val domain: String,
        val challenge: String
    )

    @Serializable
    data class PresentationDefinition(
        val id: String? = UUID.randomUUID().toString(),
        @SerialName("input_descriptors")
        val inputDescriptors: Array<InputDescriptor>,
        val format: InputDescriptor.PresentationFormat
    ) {
        @Serializable
        data class InputDescriptor constructor(
            val id: String = UUID.randomUUID().toString(),
            val name: String? = null,
            val purpose: String? = null,
            val format: PresentationFormat? = null,
            val constraints: Constraints
        ) {

            @Serializable
            data class Constraints constructor(
                val fields: Array<Field>? = null,
                @SerialName("limit_disclosure")
                val limitDisclosure: LimitDisclosure? = null
            ) {
                @Serializable
                data class Field constructor(
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
                }

                @Serializable(with = LimitDisclosure.LimitDisclosureSerializer::class)
                enum class LimitDisclosure(val value: String) {
                    REQUIRED("required"),
                    PREFERRED("preferred");

                    // Custom serializer for the enum
                    object LimitDisclosureSerializer : KSerializer<LimitDisclosure> {
                        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LimitDisclosure", PrimitiveKind.STRING)

                        override fun serialize(encoder: Encoder, value: LimitDisclosure) {
                            encoder.encodeString(value.value)
                        }

                        override fun deserialize(decoder: Decoder): LimitDisclosure {
                            val stringValue = decoder.decodeString()
                            return LimitDisclosure.values().firstOrNull { it.value == stringValue }
                                ?: throw SerializationException("Unknown value: $stringValue")
                        }
                    }
                }
            }

            @Serializable
            data class PresentationFormat constructor(
                @SerialName("jwt")
                val jwt: JwtFormat? = null,
            )

            @Serializable
            data class JwtFormat(
                val alg: List<String>
            )
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as PresentationDefinition

            if (id != other.id) return false
            if (!inputDescriptors.contentEquals(other.inputDescriptors)) return false
            return format == other.format
        }

        override fun hashCode(): Int {
            var result = id?.hashCode() ?: 0
            result = 31 * result + inputDescriptors.contentHashCode()
            result = 31 * result + format.hashCode()
            return result
        }
    }
}
