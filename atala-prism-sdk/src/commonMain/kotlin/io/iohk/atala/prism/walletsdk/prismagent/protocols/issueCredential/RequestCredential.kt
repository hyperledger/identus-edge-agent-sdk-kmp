package io.iohk.atala.prism.walletsdk.prismagent.protocols.issueCredential

import io.iohk.atala.prism.apollo.uuid.UUID
import io.iohk.atala.prism.walletsdk.domain.models.AttachmentDescriptor
import io.iohk.atala.prism.walletsdk.domain.models.DID
import io.iohk.atala.prism.walletsdk.domain.models.Message
import io.iohk.atala.prism.walletsdk.domain.models.PrismAgentError
import io.iohk.atala.prism.walletsdk.prismagent.helpers.build
import io.iohk.atala.prism.walletsdk.prismagent.protocols.ProtocolType
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.jvm.Throws

/**
 * ALL parameters are DIDCOMMV2 format and naming conventions and follows the protocol
 * https://github.com/hyperledger/aries-rfcs/tree/main/features/0453-issue-credential-v2
 */
@Serializable
data class RequestCredential @JvmOverloads constructor(
    val id: String? = UUID.randomUUID4().toString(),
    val body: Body,
    val attachments: Array<AttachmentDescriptor>,
    val thid: String?,
    val from: DID,
    val to: DID
) {
    val type: String = ProtocolType.DidcommRequestCredential.value

    fun makeMessage(): Message {
        return Message(
            id = id ?: UUID.randomUUID4().toString(),
            piuri = type,
            from = from,
            to = to,
            body = Json.encodeToString(body),
            attachments = attachments,
            thid = thid
        )
    }

    companion object {
        @JvmStatic
        @Throws(PrismAgentError.InvalidRequestCredentialMessageError::class)
        fun fromMessage(fromMessage: Message): RequestCredential {
            require(
                fromMessage.piuri == ProtocolType.DidcommRequestCredential.value &&
                    fromMessage.from != null &&
                    fromMessage.to != null
            ) {
                throw PrismAgentError.InvalidRequestCredentialMessageError()
            }

            val fromDID = fromMessage.from
            val toDID = fromMessage.to
            val body = Json.decodeFromString<Body>(fromMessage.body)

            return RequestCredential(
                id = fromMessage.id,
                body = body,
                attachments = fromMessage.attachments,
                thid = fromMessage.thid,
                from = fromDID,
                to = toDID
            )
        }

        @JvmStatic
        fun makeRequestFromOfferCredential(offer: OfferCredential): RequestCredential {
            return RequestCredential(
                body = Body(
                    goalCode = offer.body.goalCode,
                    comment = offer.body.comment,
                    formats = offer.body.formats
                ),
                attachments = offer.attachments,
                thid = offer.thid,
                from = offer.to,
                to = offer.from
            )
        }
    }

    @Serializable
    data class Body @JvmOverloads constructor(
        val goalCode: String? = null,
        val comment: String? = null,
        val formats: Array<CredentialFormat>
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as Body

            if (goalCode != other.goalCode) return false
            if (comment != other.comment) return false
            if (!formats.contentEquals(other.formats)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = goalCode?.hashCode() ?: 0
            result = 31 * result + (comment?.hashCode() ?: 0)
            result = 31 * result + formats.contentHashCode()
            return result
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as RequestCredential

        if (id != other.id) return false
        if (body != other.body) return false
        if (!attachments.contentEquals(other.attachments)) return false
        if (thid != other.thid) return false
        if (from != other.from) return false
        if (to != other.to) return false
        if (type != other.type) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + body.hashCode()
        result = 31 * result + attachments.contentHashCode()
        result = 31 * result + (thid?.hashCode() ?: 0)
        result = 31 * result + from.hashCode()
        result = 31 * result + to.hashCode()
        result = 31 * result + type.hashCode()
        return result
    }
}

@JvmOverloads
inline fun <reified T : Serializable> RequestCredential.Companion.build(
    fromDID: DID,
    toDID: DID,
    thid: String?,
    credentials: Map<String, T> = mapOf()
): RequestCredential {
    val aux = credentials.map { (key, value) ->
        val attachment = AttachmentDescriptor.build(
            payload = value
        )
        val format = CredentialFormat(attachId = attachment.id, format = key)
        format to attachment
    }
    return RequestCredential(
        body = RequestCredential.Body(
            formats = aux.map { it.first }.toTypedArray()
        ),
        attachments = aux.map { it.second }.toTypedArray(),
        thid = thid,
        from = fromDID,
        to = toDID
    )
}
