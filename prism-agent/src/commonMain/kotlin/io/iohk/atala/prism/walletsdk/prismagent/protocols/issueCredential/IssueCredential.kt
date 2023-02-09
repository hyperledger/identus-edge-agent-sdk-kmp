package io.iohk.atala.prism.walletsdk.prismagent.protocols.issueCredential

import io.iohk.atala.prism.apollo.base64.base64UrlEncoded
import io.iohk.atala.prism.apollo.uuid.UUID
import io.iohk.atala.prism.domain.models.AttachmentBase64
import io.iohk.atala.prism.domain.models.AttachmentDescriptor
import io.iohk.atala.prism.domain.models.DID
import io.iohk.atala.prism.domain.models.Message
import io.iohk.atala.prism.domain.models.PrismAgentError
import io.iohk.atala.prism.walletsdk.prismagent.helpers.build
import io.iohk.atala.prism.walletsdk.prismagent.protocols.ProtocolType
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.js.JsExport

@Serializable
@JsExport
data class IssueCredential(
    val id: String? = UUID.randomUUID4().toString(),
    val body: Body,
    val attachments: Array<AttachmentDescriptor>,
    val thid: String?,
    val from: DID,
    val to: DID
) {
    val type: String = ProtocolType.DidcommIssueCredential.value

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

    fun getCredentialStrings(): Array<String> {
        return attachments.mapNotNull {
            when (it.data) {
                is AttachmentBase64 -> {
                    (it.data as AttachmentBase64).base64.base64UrlEncoded
                }
                else -> null
            }
        }.toTypedArray()
    }

    companion object {
        fun fromMessage(fromMessage: Message): IssueCredential {
            require(
                fromMessage.piuri == ProtocolType.DidcommIssueCredential.value &&
                    fromMessage.from != null &&
                    fromMessage.to != null
            ) {
                throw PrismAgentError.invalidIssueCredentialMessageError()
            }

            val fromDID = fromMessage.from!!
            val toDID = fromMessage.to!!
            val body = Json.decodeFromString<IssueCredential.Body>(fromMessage.body)

            return IssueCredential(
                id = fromMessage.id,
                body = body,
                attachments = fromMessage.attachments,
                thid = fromMessage.thid,
                from = fromDID,
                to = toDID
            )
        }

        fun makeIssueFromRequestCedential(msg: Message): IssueCredential {
            val request = RequestCredential.fromMessage(msg)
            return IssueCredential(
                body = Body(
                    goalCode = request.body.goalCode,
                    comment = request.body.comment,
                    formats = request.body.formats
                ),
                attachments = request.attachments,
                thid = request.thid,
                from = request.from,
                to = request.to
            )
        }
    }

    @Serializable
    data class Body(
        val goalCode: String? = null,
        val comment: String? = null,
        val replacementId: String? = null,
        val moreAvailable: String? = null,
        val formats: Array<CredentialFormat>
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as Body

            if (goalCode != other.goalCode) return false
            if (comment != other.comment) return false
            if (replacementId != other.replacementId) return false
            if (moreAvailable != other.moreAvailable) return false
            if (!formats.contentEquals(other.formats)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = goalCode?.hashCode() ?: 0
            result = 31 * result + (comment?.hashCode() ?: 0)
            result = 31 * result + (replacementId?.hashCode() ?: 0)
            result = 31 * result + (moreAvailable?.hashCode() ?: 0)
            result = 31 * result + formats.contentHashCode()
            return result
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as IssueCredential

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

inline fun <reified T : Serializable> IssueCredential.Companion.build(
    fromDID: DID,
    toDID: DID,
    thid: String?,
    credentials: Map<String, T> = mapOf()
): IssueCredential {
    val aux = credentials.map { (key, value) ->
        val attachment = AttachmentDescriptor.build(
            payload = value
        )
        val format = CredentialFormat(attachId = attachment.id, format = key)
        format to attachment
    }
    return IssueCredential(
        body = IssueCredential.Body(
            formats = aux.map { it.first }.toTypedArray()
        ),
        attachments = aux.map { it.second }.toTypedArray(),
        thid = thid,
        from = fromDID,
        to = toDID
    )
}
