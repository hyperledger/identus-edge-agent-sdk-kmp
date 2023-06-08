package io.iohk.atala.prism.walletsdk.prismagent.protocols.proofOfPresentation

import io.iohk.atala.prism.apollo.uuid.UUID
import io.iohk.atala.prism.walletsdk.domain.models.AttachmentDescriptor
import io.iohk.atala.prism.walletsdk.domain.models.DID
import io.iohk.atala.prism.walletsdk.domain.models.Message
import io.iohk.atala.prism.walletsdk.prismagent.PrismAgentError
import io.iohk.atala.prism.walletsdk.prismagent.protocols.ProtocolType
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class ProofTypes(
    val schema: String,
    val requiredFields: Array<String>?,
    val trustIssuers: Array<String>?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as ProofTypes

        if (schema != other.schema) return false
        if (requiredFields != null) {
            if (other.requiredFields == null) return false
            if (!requiredFields.contentEquals(other.requiredFields)) return false
        } else if (other.requiredFields != null) return false
        if (trustIssuers != null) {
            if (other.trustIssuers == null) return false
            if (!trustIssuers.contentEquals(other.trustIssuers)) return false
        } else if (other.trustIssuers != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = schema.hashCode()
        result = 31 * result + (requiredFields?.contentHashCode() ?: 0)
        result = 31 * result + (trustIssuers?.contentHashCode() ?: 0)
        return result
    }
}

class Presentation {
    val type = ProtocolType.DidcommPresentation.value
    lateinit var id: String
    lateinit var body: Body
    lateinit var attachments: Array<AttachmentDescriptor>
    var thid: String? = null
    lateinit var from: DID
    lateinit var to: DID

    @JvmOverloads
    constructor(
        id: String? = null,
        body: Body,
        attachments: Array<AttachmentDescriptor>,
        thid: String? = null,
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
        if (
            fromMessage.piuri == ProtocolType.DidcommPresentation.value &&
            fromMessage.from != null &&
            fromMessage.to != null
        ) {
            val body = Json.decodeFromString<Body>(fromMessage.body)
            Presentation(
                fromMessage.id,
                body,
                fromMessage.attachments,
                fromMessage.thid,
                fromMessage.from,
                fromMessage.to
            )
        } else {
            throw PrismAgentError.InvalidMessageType(
                type = fromMessage.piuri,
                shouldBe = ProtocolType.DidcommPresentation.value
            )
        }
    }

    fun makeMessage(): Message {
        return Message(
            id = id,
            piuri = type,
            from = from,
            to = to,
            body = Json.encodeToString(body),
            attachments = attachments,
            thid = thid
        )
    }

    @Throws(PrismAgentError.InvalidMessageType::class)
    fun makePresentationFromRequest(msg: Message): Presentation {
        val requestPresentation = RequestPresentation.fromMessage(msg)
        return Presentation(
            body = Body(
                goalCode = requestPresentation.body.goalCode,
                comment = requestPresentation.body.comment
            ),
            attachments = requestPresentation.attachments,
            thid = requestPresentation.id,
            from = requestPresentation.to,
            to = requestPresentation.from
        )
    }

    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (other::class != this::class) return false
        val otherPresentation = other as Presentation
        return otherPresentation.type == this.type &&
            otherPresentation.id == this.id &&
            otherPresentation.body == this.body &&
            otherPresentation.attachments.contentEquals(this.attachments) &&
            otherPresentation.thid == this.thid &&
            otherPresentation.from == this.from &&
            otherPresentation.to == this.to
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + body.hashCode()
        result = 31 * result + attachments.contentHashCode()
        result = 31 * result + (thid?.hashCode() ?: 0)
        result = 31 * result + from.hashCode()
        result = 31 * result + to.hashCode()
        return result
    }

    @Serializable
    data class Body @JvmOverloads constructor(
        val goalCode: String? = null,
        val comment: String? = null
    )
}
