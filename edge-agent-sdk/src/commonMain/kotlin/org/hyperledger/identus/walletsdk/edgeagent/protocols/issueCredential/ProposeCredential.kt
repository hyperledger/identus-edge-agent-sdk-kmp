package org.hyperledger.identus.walletsdk.edgeagent.protocols.issueCredential

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.hyperledger.identus.walletsdk.domain.models.AttachmentDescriptor
import org.hyperledger.identus.walletsdk.domain.models.DID
import org.hyperledger.identus.walletsdk.domain.models.Message
import org.hyperledger.identus.walletsdk.edgeagent.EdgeAgentError
import org.hyperledger.identus.walletsdk.edgeagent.helpers.build
import org.hyperledger.identus.walletsdk.edgeagent.protocols.ProtocolType
import java.util.UUID

/**
 * ALL parameters are DIDCOMMV2 format and naming conventions and follows the protocol
 * https://github.com/hyperledger/aries-rfcs/tree/main/features/0453-issue-credential-v2
 */
@Serializable
data class ProposeCredential @JvmOverloads constructor(
    val id: String = UUID.randomUUID().toString(),
    val body: Body,
    val attachments: Array<AttachmentDescriptor>,
    val thid: String?,
    val from: DID,
    val to: DID
) {
    public val type: String = ProtocolType.DidcommProposeCredential.value

    /**
     * This function creates a new [Message] object based on the provided data. The [Message] object represents a DIDComm message,
     * which is used for secure, decentralized communication in the Atala PRISM architecture. The function sets the id, piuri,
     * from, to, body, attachments, and thid properties of the [Message] object. The id property is generated using a random UUID,
     * and the body property is encoded as a JSON string using the [Json.encodeToString] function. The other properties are set
     * based on the values passed as arguments to the function.
     *
     * @return A new [Message] object.
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
         * Converts a Message object to a ProposeCredential object.
         *
         * @param fromMessage The input Message object to convert.
         * @return A ProposeCredential object created from the input Message.
         * @throws EdgeAgentError.InvalidMessageType If the input message does not represent the expected protocol type or if it does not have "from" and "to" fields.
         */
        @JvmStatic
        @Throws(EdgeAgentError.InvalidMessageType::class)
        fun fromMessage(fromMessage: Message): ProposeCredential {
            require(
                fromMessage.piuri == ProtocolType.DidcommProposeCredential.value &&
                    fromMessage.from != null &&
                    fromMessage.to != null
            ) {
                throw EdgeAgentError.InvalidMessageType(
                    type = fromMessage.piuri,
                    shouldBe = ProtocolType.DidcommProposeCredential.value
                )
            }

            val fromDID = fromMessage.from
            val toDID = fromMessage.to
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

    /**
     * Represents the body of a message for proposing a credential.
     *
     * @property goalCode The goal code.
     * @property comment The comment.
     * @property credentialPreview The credential preview.
     */
    @Serializable
    data class Body @JvmOverloads constructor(
        val goalCode: String? = null,
        val comment: String? = null,
        val credentialPreview: CredentialPreview
    ) {
        /**
         * Checks if this object is equal to the specified [other] object.
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
            if (credentialPreview != other.credentialPreview) return false

            return true
        }

        /**
         * Computes the hash code value for this object.
         *
         * @return The hash code value for this object.
         */
        override fun hashCode(): Int {
            var result = goalCode?.hashCode() ?: 0
            result = 31 * result + (comment?.hashCode() ?: 0)
            result = 31 * result + credentialPreview.hashCode()
            return result
        }
    }

    /**
     * Checks if this object is equal to the specified [other] object.
     *
     * @param other The object to compare for equality.
     * @return `true` if the objects are equal, `false` otherwise.
     */
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

    /**
     * Computes the hash code value for this object.
     *
     * @return The hash code value for this object.
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
}

/**
 * This method builds a ProposeCredential object based on the provided parameters. The ProposeCredential represents a proposal to issue a credential in the Atala PRISM architecture
 *. The method takes in the following parameters:
 *
 * @param fromDID The DID of the sender of the proposal.
 * @param toDID The DID of the recipient of the proposal.
 * @param thid The thread ID of the proposal. Optional.
 * @param credentialPreview The credential preview object that describes the proposed credential.
 * @param credentials A map of credential formats and their corresponding values. The values must be of type T, which must implement the Serializable interface. The default value
 * is an empty map.
 *
 * @return A new ProposeCredential object.
 */
@JvmOverloads
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
            credentialPreview = credentialPreview
        ),
        attachments = aux.map { it.second }.toTypedArray(),
        thid = thid,
        from = fromDID,
        to = toDID
    )
}
