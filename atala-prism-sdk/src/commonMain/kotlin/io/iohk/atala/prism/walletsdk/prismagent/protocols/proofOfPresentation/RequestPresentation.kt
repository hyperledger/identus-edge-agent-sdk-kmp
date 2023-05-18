package io.iohk.atala.prism.walletsdk.prismagent.protocols.proofOfPresentation

import io.iohk.atala.prism.apollo.uuid.UUID
import io.iohk.atala.prism.walletsdk.domain.models.AttachmentDescriptor
import io.iohk.atala.prism.walletsdk.domain.models.DID
import io.iohk.atala.prism.walletsdk.domain.models.Message
import io.iohk.atala.prism.walletsdk.domain.models.PrismAgentError
import io.iohk.atala.prism.walletsdk.prismagent.protocols.ProtocolType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.jvm.Throws

data class RequestPresentation(
    val id: String = UUID.randomUUID4().toString(),
    val body: Body,
    val attachments: Array<AttachmentDescriptor>,
    val thid: String? = null,
    val from: DID,
    val to: DID
) {

    val type = ProtocolType.DidcommRequestPresentation.value

    fun makeMessage(): Message {
        return Message(
            id = this.id,
            piuri = this.type,
            from = this.from,
            to = this.to,
            body = Json.encodeToString(this.body),
            attachments = this.attachments,
            thid = this.thid
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RequestPresentation

        if (id != other.id) return false
        if (body != other.body) return false
        if (!attachments.contentEquals(other.attachments)) return false
        if (thid != other.thid) return false
        if (from != other.from) return false
        if (to != other.to) return false
        return type == other.type
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + body.hashCode()
        result = 31 * result + attachments.contentHashCode()
        result = 31 * result + (thid?.hashCode() ?: 0)
        result = 31 * result + from.hashCode()
        result = 31 * result + to.hashCode()
        result = 31 * result + type.hashCode()
        return result
    }

    companion object {
        @JvmStatic
        @Throws(PrismAgentError.InvalidRequestPresentationMessageError::class)
        fun fromMessage(fromMessage: Message): RequestPresentation {
            if (fromMessage.piuri == ProtocolType.DidcommRequestPresentation.value &&
                fromMessage.from != null &&
                fromMessage.to != null
            ) {
                return RequestPresentation(
                    id = fromMessage.id,
                    body = Json.decodeFromString(fromMessage.body),
                    attachments = fromMessage.attachments,
                    thid = fromMessage.thid,
                    from = fromMessage.from,
                    to = fromMessage.to
                )
            } else {
                throw PrismAgentError.InvalidMessageError()
            }
        }

        @JvmStatic
        @Throws(PrismAgentError.InvalidMessageError::class)
        fun makeRequestFromProposal(msg: Message): RequestPresentation {
            try {
                val request = ProposePresentation(msg)

                return RequestPresentation(
                    body = Body(
                        goalCode = request.body.goalCode,
                        comment = request.body.comment,
                        willConfirm = false,
                        proofTypes = request.body.proofTypes
                    ),
                    attachments = request.attachments,
                    thid = msg.id,
                    from = request.to,
                    to = request.from
                )
            } catch (e: Exception) {
                throw PrismAgentError.InvalidMessageError()
            }
        }
    }

    @Serializable
    data class Body @JvmOverloads constructor(
        @SerialName("goal_code")
        val goalCode: String? = null,
        val comment: String? = null,
        @SerialName("will_confirm")
        val willConfirm: Boolean? = false,
        @SerialName("proof_types")
        val proofTypes: Array<ProofTypes>
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as Body

            if (goalCode != other.goalCode) return false
            if (comment != other.comment) return false
            if (willConfirm != other.willConfirm) return false
            if (!proofTypes.contentEquals(other.proofTypes)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = goalCode?.hashCode() ?: 0
            result = 31 * result + (comment?.hashCode() ?: 0)
            result = 31 * result + (willConfirm?.hashCode() ?: 0)
            result = 31 * result + proofTypes.contentHashCode()
            return result
        }
    }
}
