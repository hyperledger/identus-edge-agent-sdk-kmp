package io.iohk.atala.prism.walletsdk.prismagent.protocols.issueCredential

import io.iohk.atala.prism.apollo.uuid.UUID
import io.iohk.atala.prism.walletsdk.domain.models.AttachmentDescriptor
import io.iohk.atala.prism.walletsdk.domain.models.DID
import io.iohk.atala.prism.walletsdk.domain.models.Message
import io.iohk.atala.prism.walletsdk.prismagent.CREDENTIAL_PREVIEW
import io.iohk.atala.prism.walletsdk.prismagent.GOAL_CODE
import io.iohk.atala.prism.walletsdk.prismagent.MULTIPLE_AVAILABLE
import io.iohk.atala.prism.walletsdk.prismagent.PrismAgentError
import io.iohk.atala.prism.walletsdk.prismagent.REPLACEMENT_ID
import io.iohk.atala.prism.walletsdk.prismagent.helpers.build
import io.iohk.atala.prism.walletsdk.prismagent.protocols.ProtocolType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * ALL parameters are DIDCOMMV2 format and naming conventions and follows the protocol
 * https://github.com/hyperledger/aries-rfcs/tree/main/features/0453-issue-credential-v2
 */
@Serializable
data class OfferCredential @JvmOverloads constructor(
    val id: String = UUID.randomUUID4().toString(),
    val body: Body,
    val attachments: Array<AttachmentDescriptor>,
    val thid: String?,
    val from: DID,
    val to: DID
) {
    public val type: String = ProtocolType.DidcommOfferCredential.value

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

    companion object {

        @JvmStatic
        fun makeOfferFromProposedCredential(proposed: ProposeCredential): OfferCredential {
            return OfferCredential(
                body = Body(
                    goalCode = proposed.body.goalCode,
                    comment = proposed.body.comment,
                    credentialPreview = proposed.body.credentialPreview,
                    formats = proposed.body.formats
                ),
                attachments = proposed.attachments,
                thid = proposed.thid,
                from = proposed.from,
                to = proposed.to
            )
        }

        @JvmStatic
        @Throws(PrismAgentError.InvalidMessageType::class)
        fun fromMessage(fromMessage: Message): OfferCredential {
            require(
                fromMessage.piuri == ProtocolType.DidcommOfferCredential.value &&
                    fromMessage.from != null &&
                    fromMessage.to != null
            ) {
                throw PrismAgentError.InvalidMessageType(
                    type = fromMessage.piuri,
                    shouldBe = ProtocolType.DidcommOfferCredential.value
                )
            }

            val fromDID = fromMessage.from
            val toDID = fromMessage.to
            val json = Json {
                ignoreUnknownKeys = true
            }
            val body = Json.decodeFromString<Body>(fromMessage.body)

            return OfferCredential(
                id = fromMessage.id,
                body = body,
                attachments = fromMessage.attachments,
                thid = fromMessage.thid,
                from = fromDID,
                to = toDID
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as OfferCredential

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

    @Serializable
    data class Body @JvmOverloads constructor(
        @SerialName(GOAL_CODE)
        val goalCode: String? = null,
        val comment: String? = null,
        @SerialName(REPLACEMENT_ID)
        val replacementId: String? = null,
        @SerialName(MULTIPLE_AVAILABLE)
        val multipleAvailable: String? = null,
        @SerialName(CREDENTIAL_PREVIEW)
        val credentialPreview: CredentialPreview,
        val formats: Array<CredentialFormat>
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as Body

            if (goalCode != other.goalCode) return false
            if (comment != other.comment) return false
            if (replacementId != other.replacementId) return false
            if (multipleAvailable != other.multipleAvailable) return false
            if (credentialPreview != other.credentialPreview) return false
            if (!formats.contentEquals(other.formats)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = goalCode?.hashCode() ?: 0
            result = 31 * result + (comment?.hashCode() ?: 0)
            result = 31 * result + (replacementId?.hashCode() ?: 0)
            result = 31 * result + (multipleAvailable?.hashCode() ?: 0)
            result = 31 * result + credentialPreview.hashCode()
            result = 31 * result + formats.contentHashCode()
            return result
        }
    }
}

@JvmOverloads
inline fun <reified T : Serializable> OfferCredential.Companion.build(
    fromDID: DID,
    toDID: DID,
    thid: String?,
    credentialPreview: CredentialPreview,
    credentials: Map<String, T> = mapOf()
): OfferCredential {
    val aux = credentials.map { (key, value) ->
        val attachment = AttachmentDescriptor.build(
            payload = value
        )
        val format = CredentialFormat(attachId = attachment.id, format = key)
        format to attachment
    }
    return OfferCredential(
        body = OfferCredential.Body(
            credentialPreview = credentialPreview,
            formats = aux.map { it.first }.toTypedArray()
        ),
        attachments = aux.map { it.second }.toTypedArray(),
        thid = thid,
        from = fromDID,
        to = toDID
    )
}
