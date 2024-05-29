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
data class RequestCredential @JvmOverloads constructor(
    val id: String = UUID.randomUUID().toString(),
    val body: Body,
    val attachments: Array<AttachmentDescriptor>,
    val thid: String?,
    val from: DID,
    val to: DID
) {
    val type: String = ProtocolType.DidcommRequestCredential.value

    /**
     * This method is used to create a [Message] object with the specified properties.
     *
     * @return The created [Message] object.
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
         * Converts a Message object to a RequestCredential object.
         *
         * @param fromMessage The Message object to convert.
         * @return The converted RequestCredential object.
         * @throws EdgeAgentError.InvalidMessageType If the Message object does not represent the expected protocol or is missing the "from" and "to" fields.
         */
        @JvmStatic
        @Throws(EdgeAgentError.InvalidMessageType::class)
        fun fromMessage(fromMessage: Message): RequestCredential {
            require(
                fromMessage.piuri == ProtocolType.DidcommRequestCredential.value &&
                    fromMessage.from != null &&
                    fromMessage.to != null
            ) {
                throw EdgeAgentError.InvalidMessageType(
                    type = fromMessage.piuri,
                    shouldBe = ProtocolType.DidcommRequestCredential.value
                )
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

        /**
         * Creates a [RequestCredential] object based on the provided [OfferCredential].
         *
         * @param offer The [OfferCredential] object containing the offer credential data.
         * @return The [RequestCredential] object created with the data from the offer credential.
         */
        @JvmStatic
        fun makeRequestFromOfferCredential(offer: OfferCredential): RequestCredential {
            return RequestCredential(
                body = Body(
                    goalCode = offer.body.goalCode,
                    comment = offer.body.comment
                ),
                attachments = offer.attachments,
                thid = offer.thid,
                from = offer.to,
                to = offer.from
            )
        }
    }

    /**
     * Represents the body of a request credential.
     *
     * @property goalCode The goal code.
     * @property comment The comment.
     */
    @Serializable
    data class Body @JvmOverloads constructor(
        val goalCode: String? = null,
        val comment: String? = null
    ) {
        /**
         * Checks if this [Body] object is equal to the specified [other] object.
         *
         * Two [Body] objects are considered equal if they have the same values for the following properties:
         * - [goalCode] (the goal code)
         * - [comment] (the comment)
         *
         * @param other The object to compare with this [Body] object.
         * @return true if the specified [other] object is equal to this [Body] object, false otherwise.
         */
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as Body

            if (goalCode != other.goalCode) return false
            if (comment != other.comment) return false

            return true
        }

        /**
         * Calculates the hash code for the object.
         *
         * The hash code is calculated based on the values of the `goalCode` and `comment` properties.
         * If `goalCode` is not null, its hash code is used as part of the calculation.
         * If `comment` is not null, its hash code is used as part of the calculation.
         *
         * @return The hash code value for the object.
         */
        override fun hashCode(): Int {
            var result = goalCode?.hashCode() ?: 0
            result = 31 * result + (comment?.hashCode() ?: 0)
            return result
        }
    }

    /**
     * Checks if this [RequestCredential] object is equal to the specified [other] object.
     *
     * Two [RequestCredential] objects are considered equal if they have the same values for the following properties:
     * - [id] (the ID)
     * - [body] (the body)
     * - [attachments] (the attachments)
     * - [thid] (the THID)
     * - [from] (the sender)
     * - [to] (the receiver)
     * - [type] (the type)
     *
     * @param other The object to compare with this [RequestCredential] object.
     * @return true if the specified [other] object is equal to this [RequestCredential] object, false otherwise.
     */
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

    /**
     * Calculates the hash code for the [RequestCredential] object.
     *
     * The hash code is calculated based on the values of the following properties:
     * - [id] (the ID)
     * - [body] (the body)
     * - [attachments] (the attachments)
     * - [thid] (the THID)
     * - [from] (the sender)
     * - [to] (the receiver)
     * - [type] (the type)
     *
     * @return The hash code value for the [RequestCredential] object.
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
 * Builds a [RequestCredential] object using the provided parameters.
 *
 * @param fromDID The DID of the sender.
 * @param toDID The DID of the receiver.
 * @param thid The THID (thread ID).
 * @param credentials The map of credential formats and corresponding payloads.
 * @return The created [RequestCredential] object.
 */
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
        body = RequestCredential.Body(),
        attachments = aux.map { it.second }.toTypedArray(),
        thid = thid,
        from = fromDID,
        to = toDID
    )
}
