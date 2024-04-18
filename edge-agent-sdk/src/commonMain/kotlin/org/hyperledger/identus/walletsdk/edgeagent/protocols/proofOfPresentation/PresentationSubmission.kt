@file:Suppress("ktlint:standard:import-ordering")

package org.hyperledger.identus.walletsdk.edgeagent.protocols.proofOfPresentation

import java.util.UUID
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class DescriptorItemFormat(val value: String) {
    JWT_VC("jwt_vc"),
    JWT_VP("jwt_vp")
}

@Serializable
data class PresentationSubmission(
    @SerialName("presentation_submission")
    val presentationSubmission: Submission,
    val verifiablePresentation: Array<String>,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PresentationSubmission

        if (presentationSubmission != other.presentationSubmission) return false
        return verifiablePresentation.contentEquals(other.verifiablePresentation)
    }

    override fun hashCode(): Int {
        var result = presentationSubmission.hashCode()
        result = 31 * result + verifiablePresentation.contentHashCode()
        return result
    }

    @Serializable
    data class Submission(
        val id: String? = UUID.randomUUID().toString(),
        @SerialName("definition_id")
        val definitionId: String,
        @SerialName("descriptor_map")
        val descriptorMap: Array<DescriptorItem>
    ) {
        @Serializable
        data class DescriptorItem(
            val id: String,
            val format: String,
            val path: String,
            @SerialName("path_nested")
            val pathNested: DescriptorItem? = null
        )

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as Submission

            if (id != other.id) return false
            if (definitionId != other.definitionId) return false
            return descriptorMap.contentEquals(other.descriptorMap)
        }

        override fun hashCode(): Int {
            var result = id.hashCode()
            result = 31 * result + definitionId.hashCode()
            result = 31 * result + descriptorMap.contentHashCode()
            return result
        }
    }
}
