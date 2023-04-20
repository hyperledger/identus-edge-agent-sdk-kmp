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

@Serializable
data class ProposeCredential(
    val id: String? = UUID.randomUUID4().toString(),
    val body: Body,
    val attachments: Array<AttachmentDescriptor>,
    val thid: String?,
    val from: DID,
    val to: DID
) {
    public val type: String = ProtocolType.DidcommProposeCredential.value

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
        fun fromMessage(fromMessage: Message): ProposeCredential {
            require(
                fromMessage.piuri == ProtocolType.DidcommProposeCredential.value &&
                    fromMessage.from != null &&
                    fromMessage.to != null
            ) {
                throw PrismAgentError.InvalidProposedCredentialMessageError()
            }

            val fromDID = fromMessage.from!!
            val toDID = fromMessage.to!!
            val body = Json.decodeFromString<Body>(fromMessage.body)

            return ProposeCredential(
                id = fromMessage.id,
                body = body,
                attachments = fromMessage.attachments,
                thid = fromMessage.thid,
                from = fromDID,
                to = toDID
            )
        }
    }

    @Serializable
    data class Body(
        val goalCode: String? = null,
        val comment: String? = null,
        val credentialPreview: CredentialPreview,
        val formats: Array<CredentialFormat>
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as Body

            if (goalCode != other.goalCode) return false
            if (comment != other.comment) return false
            if (credentialPreview != other.credentialPreview) return false
            if (!formats.contentEquals(other.formats)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = goalCode?.hashCode() ?: 0
            result = 31 * result + (comment?.hashCode() ?: 0)
            result = 31 * result + credentialPreview.hashCode()
            result = 31 * result + formats.contentHashCode()
            return result
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as ProposeCredential

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

inline fun <reified T : Serializable> ProposeCredential.Companion.build(
    fromDID: DID,
    toDID: DID,
    thid: String?,
    credentialPreview: CredentialPreview,
    credentials: Map<String, T> = mapOf()
): ProposeCredential {
    val aux = credentials.map { (key, value) ->
        val attachment = AttachmentDescriptor.build(
            payload = value
        )
        val format = CredentialFormat(attachId = attachment.id, format = key)
        format to attachment
    }
    return ProposeCredential(
        body = ProposeCredential.Body(
            credentialPreview = credentialPreview,
            formats = aux.map { it.first }.toTypedArray()
        ),
        attachments = aux.map { it.second }.toTypedArray(),
        thid = thid,
        from = fromDID,
        to = toDID
    )
}
