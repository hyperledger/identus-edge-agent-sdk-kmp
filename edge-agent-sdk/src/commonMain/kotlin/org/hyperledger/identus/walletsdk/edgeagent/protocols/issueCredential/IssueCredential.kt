package org.hyperledger.identus.walletsdk.edgeagent.protocols.issueCredential

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.hyperledger.identus.apollo.base64.base64UrlEncoded
import org.hyperledger.identus.walletsdk.domain.models.AttachmentData.AttachmentBase64
import org.hyperledger.identus.walletsdk.domain.models.AttachmentDescriptor
import org.hyperledger.identus.walletsdk.domain.models.DID
import org.hyperledger.identus.walletsdk.domain.models.Message
import org.hyperledger.identus.walletsdk.edgeagent.EdgeAgentError
import org.hyperledger.identus.walletsdk.edgeagent.GOAL_CODE
import org.hyperledger.identus.walletsdk.edgeagent.MORE_AVAILABLE
import org.hyperledger.identus.walletsdk.edgeagent.REPLACEMENT_ID
import org.hyperledger.identus.walletsdk.edgeagent.helpers.build
import org.hyperledger.identus.walletsdk.edgeagent.protocols.ProtocolType
import java.util.UUID

/**
 * The IssueCredential class represents a credential issuance in the context of DIDComm messaging.
 */
@Serializable
data class IssueCredential(
    val id: String = UUID.randomUUID().toString(),
    val body: Body,
    val attachments: Array<AttachmentDescriptor>,
    val thid: String?,
    val from: DID,
    val to: DID
) {
    val type: String = ProtocolType.DidcommIssueCredential.value

    /**
     * Creates a [Message] object representing a DIDComm message.
     * This function is used to generate a [Message] object based on the current state of an [IssueCredential] object.
     *
     * @return A [Message] object with the following properties:
     *   - id: A unique identifier generated using [UUID.randomUUID].
     *   - piuri: The type of the message.
     *   - from: The sender's DID (Decentralized Identifier).
     *   - to: The recipient's DID.
     *   - body: The JSON-encoded body of the message.
     *   - attachments: An array of [AttachmentDescriptor] objects.
     *   - thid: The thread ID.
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

    /**
     * Retrieves an array of credential strings from the attachments.
     *
     * @return An array of credential strings.
     */
    fun getCredentialStrings(): Array<String> {
        return attachments.mapNotNull {
            when (it.data) {
                is AttachmentBase64 -> {
                    it.data.base64.base64UrlEncoded
                }

                else -> null
            }
        }.toTypedArray()
    }

    companion object {
        /**
         * Converts a Message into an IssueCredential object.
         *
         * @param fromMessage The Message object to convert.
         * @return The converted IssueCredential object.
         * @throws EdgeAgentError.InvalidMessageType if the fromMessage doesn't represent the DidcommIssueCredential protocol,
         * or if it doesn't have "from" and "to" fields.
         */
        @JvmStatic
        @Throws(EdgeAgentError.InvalidMessageType::class)
        fun fromMessage(fromMessage: Message): IssueCredential {
            require(
                fromMessage.piuri == ProtocolType.DidcommIssueCredential.value &&
                    fromMessage.from != null &&
                    fromMessage.to != null
            ) {
                throw EdgeAgentError.InvalidMessageType(
                    type = fromMessage.piuri,
                    shouldBe = ProtocolType.DidcommIssueCredential.value
                )
            }

            val fromDID = fromMessage.from
            val toDID = fromMessage.to
            val body = Json.decodeFromString<Body>(fromMessage.body)

            return IssueCredential(
                id = fromMessage.id,
                body = body,
                attachments = fromMessage.attachments,
                thid = fromMessage.thid,
                from = fromDID,
                to = toDID
            )
        }

        /**
         * Creates an [IssueCredential] object from a [Message] object.
         *
         * @param msg The [Message] object containing the request credential information.
         * @return The created [IssueCredential] object.
         */
        @JvmStatic
        fun makeIssueFromRequestCedential(msg: Message): IssueCredential {
            val request = RequestCredential.fromMessage(msg)
            return IssueCredential(
                body = Body(
                    goalCode = request.body.goalCode,
                    comment = request.body.comment
//                    formats = request.body.formats
                ),
                attachments = request.attachments,
                thid = msg.id,
                from = request.to,
                to = request.from
            )
        }
    }

    /**
     * Represents the body of an issue credential message.
     *
     * @property goalCode The goal code associated with the credential (optional).
     * @property comment Additional comment about the credential (optional).
     * @property replacementId The ID of the credential being replaced (optional).
     * @property moreAvailable Additional information about the availability of more credentials (optional).
     */
    @Serializable
    data class Body(
        @SerialName(GOAL_CODE)
        val goalCode: String? = null,
        val comment: String? = null,
        @SerialName(REPLACEMENT_ID)
        val replacementId: String? = null,
        @SerialName(MORE_AVAILABLE)
        val moreAvailable: String? = null
//        val formats: Array<CredentialFormat>
    ) {
        /**
         * Checks if the object is equal to the current `Body` object.
         *
         * Two `Body` objects are considered equal if they meet the following conditions:
         * - They are the same instance (reference equality).
         * - They belong to the same class.
         * - Their `goalCode` fields have the same value.
         * - Their `comment` fields have the same value.
         * - Their `replacementId` fields have the same value.
         *
         * @param other The object to compare against the current `Body` object.
         * @return `true` if the objects are equal, `false` otherwise.
         */
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as Body

            if (goalCode != other.goalCode) return false
            if (comment != other.comment) return false
            if (replacementId != other.replacementId) return false
//            if (moreAvailable != other.moreAvailable) return false
//            if (!formats.contentEquals(other.formats)) return false

            return true
        }

        /**
         * Calculates the hash code for the `hashCode()` method of the `Body` class.
         *
         * The hash code is calculated using the following formula:
         *   - The hash code of the `goalCode` field is calculated using its `hashCode()` method, or zero if it is null.
         *   - The hash code of the `comment` field is calculated using its `hashCode()` method, or zero if it is null.
         *   - The hash code of the `replacementId` field is calculated using its `hashCode()` method, or zero if it is null.
         *   - The final hash code is calculated by multiplying each field's hash code by 31 and summing them up.
         *
         * @return The calculated hash code value for the `Body` object.
         */
        override fun hashCode(): Int {
            var result = goalCode?.hashCode() ?: 0
            result = 31 * result + (comment?.hashCode() ?: 0)
            result = 31 * result + (replacementId?.hashCode() ?: 0)
//            result = 31 * result + (moreAvailable?.hashCode() ?: 0)
//            result = 31 * result + formats.contentHashCode()
            return result
        }
    }

    /**
     * Checks if the object is equal to the current `IssueCredential` object.
     *
     * Two `IssueCredential` objects are considered equal if they meet the following conditions:
     * - They are the same instance (reference equality).
     * - They belong to the same class.
     * - Their `id` fields have the same value.
     * - Their `body` fields have the same value.
     * - Their `attachments` arrays have the same content.
     * - Their `thid` fields have the same value.
     * - Their `from` fields have the same value.
     * - Their `to` fields have the same value.
     * - Their `type` fields have the same value.
     *
     * @param other The object to compare against the current `IssueCredential` object.
     * @return `true` if the objects are equal, `false` otherwise.
     */
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

    /**
     * Calculates the hash code for the `IssueCredential` object.
     *
     * The hash code is calculated using the following formula:
     *   - The hash code of the `id` field is calculated using its `hashCode()` method.
     *   - The hash code of the `body` field is calculated using its `hashCode()` method.
     *   - The hash code of the `attachments` field is calculated using its `contentHashCode()` method.
     *   - The hash code of the `thid` field is calculated using its `hashCode()` method, or zero if it is null.
     *   - The hash code of the `from` field is calculated using its `hashCode()` method.
     *   - The hash code of the `to` field is calculated using its `hashCode()` method.
     *   - The hash code of the `type` field is calculated using its `hashCode()` method.
     *   - The final hash code is calculated by multiplying each field's hash code by 31 and summing them up.
     *
     * @return The calculated hash code value for the `IssueCredential` object.
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
 * Builds an instance of [IssueCredential] with the provided parameters.
 *
 * @param T The type of the credentials.
 * @param fromDID The DID of the sender.
 * @param toDID The DID of the recipient.
 * @param thid The thread ID (optional).
 * @param credentials The map of credential formats and their corresponding values (default is an empty map).
 * @return An instance of [IssueCredential] with the specified parameters.
 */
@JvmOverloads
inline fun <reified T : Serializable> IssueCredential.Companion.build(
    fromDID: DID,
    toDID: DID,
    thid: String?,
    credentials: Map<String, T> = mapOf()
): IssueCredential {
    val aux = credentials.map { (key, value) ->
        val attachment = AttachmentDescriptor.build(payload = value)
        val format = CredentialFormat(attachId = attachment.id, format = key)
        format to attachment
    }
    return IssueCredential(
        body = IssueCredential.Body(
//            formats = aux.map { it.first }.toTypedArray()
        ),
        attachments = aux.map { it.second }.toTypedArray(),
        thid = thid,
        from = fromDID,
        to = toDID
    )
}
