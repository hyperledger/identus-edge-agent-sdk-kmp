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

class RequestPresentation {

    lateinit var id: String
    val type = ProtocolType.DidcommRequestPresentation.value
    lateinit var body: Body
    lateinit var attachments: Array<AttachmentDescriptor>
    var thid: String? = null
    lateinit var from: DID
    lateinit var to: DID

    constructor(
        id: String?,
        body: Body,
        attachments: Array<AttachmentDescriptor>,
        thid: String?,
        from: DID,
        to: DID
    ) {
        this.id = id ?: UUID.randomUUID4().toString()
        this.body = body
        this.attachments = attachments
        this.thid = thid
        this.from = from
        this.to = to
    }

    constructor(fromMessage: Message) {
        if (fromMessage.piuri == ProtocolType.DidcommRequestPresentation.value &&
            fromMessage.from != null &&
            fromMessage.to != null
        ) {
            RequestPresentation(
                id = fromMessage.id,
                body = Json.decodeFromString(fromMessage.body),
                attachments = fromMessage.attachments,
                thid = fromMessage.thid,
                from = fromMessage.from!!,
                to = fromMessage.to!!
            )
        } else {
            throw PrismAgentError.invalidMessageError()
        }
    }

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

    fun makeRequestFromProposal(msg: Message): RequestPresentation {
        TODO("Do when ProposePresentation is implemented")
    }

    @Serializable
    data class Body(
        @SerialName("goal_code")
        val goalCode: String? = null,
        val comment: String? = null,
        @SerialName("will_confirm")
        val willConfirm: Boolean? = false,
        @SerialName("proff_types")
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
