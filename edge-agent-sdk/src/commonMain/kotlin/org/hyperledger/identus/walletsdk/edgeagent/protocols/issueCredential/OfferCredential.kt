package org.hyperledger.identus.walletsdk.edgeagent.protocols.issueCredential

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.hyperledger.identus.walletsdk.domain.models.AttachmentDescriptor
import org.hyperledger.identus.walletsdk.domain.models.DID
import org.hyperledger.identus.walletsdk.domain.models.Message
import org.hyperledger.identus.walletsdk.edgeagent.CREDENTIAL_PREVIEW
import org.hyperledger.identus.walletsdk.edgeagent.EdgeAgentError
import org.hyperledger.identus.walletsdk.edgeagent.GOAL_CODE
import org.hyperledger.identus.walletsdk.edgeagent.MULTIPLE_AVAILABLE
import org.hyperledger.identus.walletsdk.edgeagent.REPLACEMENT_ID
import org.hyperledger.identus.walletsdk.edgeagent.helpers.build
import org.hyperledger.identus.walletsdk.edgeagent.protocols.ProtocolType
import java.util.UUID

/**
 * ALL parameters are DIDCOMMV2 format and naming conventions and follows the protocol
 * https://github.com/hyperledger/aries-rfcs/tree/main/features/0453-issue-credential-v2
 */
@Serializable
data class OfferCredential @JvmOverloads constructor(
    val id: String = UUID.randomUUID().toString(),
    val body: Body,
    val attachments: Array<AttachmentDescriptor>,
    val thid: String?,
    val from: DID,
    val to: DID
) {
    public val type: String = ProtocolType.DidcommOfferCredential.value

    /**
     * Creates a [Message] object with the provided data and returns it.
     * The [Message] object includes information about the sender, recipient, message body,
     * and other metadata. The [Message] object is typically used for secure, decentralized communication
     * in the Atala PRISM architecture.
     *
     * @return The [Message] object with the provided data.
     */
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

        /**
         * Creates an [OfferCredential] object from the provided [ProposeCredential] data.
         *
         * @param proposed The [ProposeCredential] object containing the proposed credential data.
         * @return The [OfferCredential] object created with the provided data.
         */
        @JvmStatic
        fun makeOfferFromProposedCredential(proposed: ProposeCredential): OfferCredential {
            return OfferCredential(
                body = Body(
                    goalCode = proposed.body.goalCode,
                    comment = proposed.body.comment,
                    credentialPreview = proposed.body.credentialPreview
                ),
                attachments = proposed.attachments,
                thid = proposed.thid,
                from = proposed.from,
                to = proposed.to
            )
        }

        /**
         * Converts a Message object to an OfferCredential object.
         *
         * @param fromMessage The Message object to convert.
         * @return The converted OfferCredential object.
         * @throws EdgeAgentError.InvalidMessageType if the message type is invalid or the "from" and "to" fields are not present.
         */
        @JvmStatic
        @Throws(EdgeAgentError.InvalidMessageType::class)
        fun fromMessage(fromMessage: Message): OfferCredential {
            require(
                fromMessage.piuri == ProtocolType.DidcommOfferCredential.value &&
                    fromMessage.from != null &&
                    fromMessage.to != null
            ) {
                throw EdgeAgentError.InvalidMessageType(
                    type = fromMessage.piuri,
                    shouldBe = ProtocolType.DidcommOfferCredential.value
                )
            }

            val fromDID = fromMessage.from
            val toDID = fromMessage.to
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

    /**
     * Checks if the current instance of `OfferCredential` is equal to the given object.
     *
     * @param other The object to compare with the current instance of `OfferCredential`.
     * @return `true` if the objects are equal, `false` otherwise.
     */
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

    /**
     * Calculates the hash code for the current instance of the OfferCredential class.
     *
     * The hash code is computed by combining the hash codes of the following properties:
     * - id
     * - body
     * - attachments
     * - thid (if not null)
     * - from
     * - to
     * - type
     *
     * @return The computed hash code for the current instance of the OfferCredential class.
     */
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

    /**
     * Represents the body of an OfferCredential message.
     *
     * @property goalCode The goal code associated with the credential.
     * @property comment Additional comments related to the credential.
     * @property replacementId The ID of the credential being replaced, if applicable.
     * @property multipleAvailable Indicates if multiple credentials of the same type are available for selection.
     * @property credentialPreview The preview of the credential to be offered.
     */
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
        val credentialPreview: CredentialPreview
    ) {
        /**
         * Compares this [Body] object to the specified [other] object for equality.
         *
         * @param other The object to compare for equality.
         * @return `true` if the objects are equal, `false` otherwise.
         */
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as Body

            if (goalCode != other.goalCode) return false
            if (comment != other.comment) return false
            if (replacementId != other.replacementId) return false
            if (multipleAvailable != other.multipleAvailable) return false
            if (credentialPreview != other.credentialPreview) return false

            return true
        }

        /**
         * Computes the hash code value for this object.
         *
         * The hash code is computed by combining the hash codes of the following properties:
         * - goalCode
         * - comment
         * - replacementId
         * - multipleAvailable
         * - credentialPreview
         *
         * @return The hash code value for this object.
         */
        override fun hashCode(): Int {
            var result = goalCode?.hashCode() ?: 0
            result = 31 * result + (comment?.hashCode() ?: 0)
            result = 31 * result + (replacementId?.hashCode() ?: 0)
            result = 31 * result + (multipleAvailable?.hashCode() ?: 0)
            result = 31 * result + credentialPreview.hashCode()
            return result
        }
    }
}

/**
 * Builds an OfferCredential object with the provided data.
 *
 * @param fromDID The DID of the sender.
 * @param toDID The DID of the recipient.
 * @param thid The thread ID.
 * @param credentialPreview The preview of the credential.
 * @param credentials The map of credentials. Default value is an empty map.
 * @return The constructed OfferCredential object.
 */
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
            credentialPreview = credentialPreview
        ),
        attachments = aux.map { it.second }.toTypedArray(),
        thid = thid,
        from = fromDID,
        to = toDID
    )
}
