package io.iohk.atala.prism.walletsdk.prismagent.protocols.proofOfPresentation

import io.iohk.atala.prism.apollo.uuid.UUID
import io.iohk.atala.prism.walletsdk.domain.models.AttachmentDescriptor
import io.iohk.atala.prism.walletsdk.domain.models.DID
import io.iohk.atala.prism.walletsdk.domain.models.Message
import io.iohk.atala.prism.walletsdk.prismagent.GOAL_CODE
import io.iohk.atala.prism.walletsdk.prismagent.PROOF_TYPES
import io.iohk.atala.prism.walletsdk.prismagent.PrismAgentError
import io.iohk.atala.prism.walletsdk.prismagent.protocols.ProtocolType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ProposePresentation {

    lateinit var id: String
    val type = ProtocolType.DidcommProposePresentation.value
    lateinit var body: Body
    lateinit var attachments: Array<AttachmentDescriptor>
    var thid: String? = null
    lateinit var from: DID
    lateinit var to: DID

    @JvmOverloads
    constructor(
        id: String? = UUID.randomUUID4().toString(),
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

    @Throws(PrismAgentError.InvalidMessageType::class)
    constructor(fromMessage: Message) {
        if (fromMessage.piuri == ProtocolType.DidcommProposePresentation.value &&
            fromMessage.from != null &&
            fromMessage.to != null
        ) {
            ProposePresentation(
                id = fromMessage.id,
                body = Json.decodeFromString(fromMessage.body),
                attachments = fromMessage.attachments,
                thid = fromMessage.thid,
                from = fromMessage.from,
                to = fromMessage.to
            )
        } else {
            throw PrismAgentError.InvalidMessageType(
                type = fromMessage.piuri,
                shouldBe = ProtocolType.DidcommProposePresentation.value
            )
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

    @Throws(PrismAgentError.InvalidMessageType::class)
    fun makeProposalFromRequest(msg: Message): ProposePresentation {
        val request = RequestPresentation.fromMessage(msg)

        return ProposePresentation(
            body = Body(
                goalCode = request.body.goalCode,
                comment = request.body.comment,
                proofTypes = request.body.proofTypes
            ),
            attachments = request.attachments,
            thid = msg.id,
            from = request.to,
            to = request.from
        )
    }

    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (other::class != this::class) return false
        val otherPresentation = other as ProposePresentation
        return otherPresentation.type == this.type &&
            otherPresentation.id == this.id &&
            otherPresentation.body == this.body &&
            otherPresentation.attachments.contentEquals(this.attachments) &&
            otherPresentation.thid == this.thid &&
            otherPresentation.from == this.from &&
            otherPresentation.to == this.to
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + body.hashCode()
        result = 31 * result + attachments.contentHashCode()
        result = 31 * result + (thid?.hashCode() ?: 0)
        result = 31 * result + from.hashCode()
        result = 31 * result + to.hashCode()
        return result
    }

    @Serializable
    data class Body @JvmOverloads constructor(
        @SerialName(GOAL_CODE)
        val goalCode: String? = null,
        val comment: String? = null,
        @SerialName(PROOF_TYPES)
        val proofTypes: Array<ProofTypes>
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as Body

            if (goalCode != other.goalCode) return false
            if (comment != other.comment) return false
            if (!proofTypes.contentEquals(other.proofTypes)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = goalCode?.hashCode() ?: 0
            result = 31 * result + (comment?.hashCode() ?: 0)
            result = 31 * result + proofTypes.contentHashCode()
            return result
        }
    }
}
