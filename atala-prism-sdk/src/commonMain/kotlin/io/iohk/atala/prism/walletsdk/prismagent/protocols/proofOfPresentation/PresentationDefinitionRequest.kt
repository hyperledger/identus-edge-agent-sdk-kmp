@file:Suppress("ktlint:standard:import-ordering")

package io.iohk.atala.prism.walletsdk.prismagent.protocols.proofOfPresentation

import java.util.UUID
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PresentationDefinitionRequest(
    @SerialName("presentation_definition")
    val presentationDefinitionBody: PresentationDefinitionBody
) {

    @Serializable
    data class PresentationDefinitionBody(
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
            val format: String? = null,
            val constraints: Constraints
        ) {

            @Serializable
            data class Constraints constructor(
                val fields: Array<Fields>? = null,
                @SerialName("limit_disclosure")
                val limitDisclosure: LimitDisclosure? = null
            ) {
                @Serializable
                data class Fields constructor(
                    val path: Array<String>,
                    val id: String? = null,
                    val purpose: String? = null,
                    val name: String? = null,
                    val filter: Filter? = null,
                    val optional: Boolean? = false
                ) {
                    @Serializable
                    data class Filter(
                        val type: String,
                        val pattern: String
                    )
                }

                enum class LimitDisclosure(value: String) {
                    REQUIRED("required"),
                    PREFERRED("preferred")
                }
            }

            @Serializable
            data class PresentationFormat constructor(
                @SerialName("jwt_vc")
                val jwtVc: JwtVcFormat? = null,
                @SerialName("jwt_vp")
                val jwtVp: JwtVpFormat? = null
            )

            @Serializable
            data class JwtVcFormat(
                val alg: List<String>
            )

            @Serializable
            data class JwtVpFormat(
                val alg: List<String>
            )
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as PresentationDefinitionBody

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
