package org.hyperledger.identus.walletsdk.edgeagent.protocols.proofOfPresentation

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.hyperledger.identus.walletsdk.domain.models.AttachmentDescriptor
import org.hyperledger.identus.walletsdk.domain.models.DID
import org.hyperledger.identus.walletsdk.domain.models.Message
import org.hyperledger.identus.walletsdk.edgeagent.EdgeAgentError
import org.hyperledger.identus.walletsdk.edgeagent.GOAL_CODE
import org.hyperledger.identus.walletsdk.edgeagent.PROOF_TYPES
import org.hyperledger.identus.walletsdk.edgeagent.WILL_CONFIRM
import org.hyperledger.identus.walletsdk.edgeagent.protocols.ProtocolType
import java.util.UUID

@Serializable
/**
 * The `RequestPresentation` class represents a request for presentation of credentials or proofs in a DIDComm protocol.
 *
 * @property id The unique identifier of the request.
 * @property body The content of the request.
 * @property attachments The array of attachment descriptors associated with the request.
 * @property thid The thread ID of the request message. Default value is `null`.
 * @property from The DID of the sender of the request.
 * @property to The DID of the recipient of the request.
 */
data class RequestPresentation(
    val id: String = UUID.randomUUID().toString(),
    val body: Body,
    val attachments: Array<AttachmentDescriptor>,
    val thid: String? = null,
    val from: DID,
    val to: DID? = null,
    val direction: Message.Direction = Message.Direction.RECEIVED
) {

    val type = ProtocolType.DidcommRequestPresentation.value

    /**
     * Creates a new [Message] object based on the current state of the [RequestPresentation] instance.
     * The [Message] object contains information about the sender, recipient, message body, and other metadata.
     * This method is typically used to convert a [RequestPresentation] instance into a [Message] object for communication purposes.
     *
     * @return The newly created [Message] object.
     */
    fun makeMessage(): Message {
        return Message(
            id = this.id,
            piuri = this.type,
            from = this.from,
            to = this.to,
            body = Json.encodeToString(this.body),
            attachments = this.attachments,
            thid = this.thid,
            direction = direction
        )
    }

    /**
     * Checks if this [RequestPresentation] object is equal to the specified [other] object.
     *
     * Two [RequestPresentation] objects are considered equal if they meet the following conditions:
     * - The two objects have the same class type.
     * - The id, body, attachments, thid, from, to, and type properties of the two objects are also equal.
     *
     * @param other The object to compare with this [RequestPresentation] object.
     * @return true if the specified [other] object is equal to this [RequestPresentation] object, false otherwise.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RequestPresentation

        if (id != other.id) return false
        if (body != other.body) return false
        if (!attachments.contentEquals(other.attachments)) return false
        if (thid != other.thid) return false
        if (from != other.from) return false
        if (to != other.to) return false
        return type == other.type
    }

    /**
     * Calculates the hash code for the [RequestPresentation] object.
     *
     * The hash code is calculated based on the following properties of the [RequestPresentation] object:
     * - [id]
     * - [body]
     * - [attachments]
     * - [thid]
     * - [from]
     * - [to]
     * - [type]
     *
     * @return The hash code value for the [RequestPresentation] object.
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

    companion object {
        /**
         * Converts a given [Message] object to a [RequestPresentation] object.
         *
         * @param fromMessage The [Message] object to convert.
         * @return The converted [RequestPresentation] object.
         * @throws EdgeAgentError.InvalidMessageType if the [Message] object does not represent the correct protocol
         *         or if it is missing the "from" and "to" fields.
         */
        @JvmStatic
        @Throws(EdgeAgentError.InvalidMessageType::class)
        fun fromMessage(fromMessage: Message): RequestPresentation {
            if (fromMessage.piuri == ProtocolType.DidcommRequestPresentation.value &&
                fromMessage.from != null &&
                fromMessage.to != null
            ) {
                val json = Json {
                    ignoreUnknownKeys = true
                }
                return RequestPresentation(
                    id = fromMessage.id,
                    body = json.decodeFromString(fromMessage.body) ?: Body(proofTypes = emptyArray()),
                    attachments = fromMessage.attachments,
                    thid = fromMessage.thid,
                    from = fromMessage.from,
                    to = fromMessage.to,
                    direction = fromMessage.direction
                )
            } else {
                throw EdgeAgentError.InvalidMessageType(
                    type = fromMessage.piuri,
                    shouldBe = ProtocolType.DidcommRequestPresentation.value
                )
            }
        }

        /**
         * Creates a [RequestPresentation] object based on the given [msg].
         *
         * @param msg The [Message] object representing a proposal.
         * @return The newly created [RequestPresentation] object.
         * @throws EdgeAgentError.InvalidMessageType if the message type is invalid.
         */
        @JvmStatic
        @Throws(EdgeAgentError.InvalidMessageType::class)
        fun makeRequestFromProposal(msg: Message): RequestPresentation {
            val request = ProposePresentation(msg)

            return RequestPresentation(
                body = Body(
                    goalCode = request.body.goalCode,
                    comment = request.body.comment,
                    willConfirm = false,
                    proofTypes = request.body.proofTypes
                ),
                attachments = request.attachments,
                thid = msg.id,
                from = request.to,
                to = request.from,
                direction = msg.direction
            )
        }
    }

    /**
     * Represents a class that encapsulates the body of a message.
     *
     * @property goalCode The goal code associated with the body.
     * @property comment The comment associated with the body.
     * @property willConfirm A boolean indicating whether confirmation is required.
     * @property proofTypes An array of proof types.
     */
    @Serializable
    @OptIn(ExperimentalSerializationApi::class)
    data class Body
    @JvmOverloads constructor(
        @SerialName(GOAL_CODE)
        val goalCode: String? = null,
        val comment: String? = null,
        @SerialName(WILL_CONFIRM)
        val willConfirm: Boolean? = false,
        @EncodeDefault
        @SerialName(PROOF_TYPES)
        val proofTypes: Array<ProofTypes>? = emptyArray()
    ) {
        /**
         * Checks if this [Body] object is equal to the specified [other] object.
         *
         * Two [Body] objects are considered equal if they meet the following conditions:
         * - The two objects have the same class type.
         * - The goalCode, comment, willConfirm, and proofTypes properties of the two objects are also equal.
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
            if (willConfirm != other.willConfirm) return false
            if (!proofTypes.contentEquals(other.proofTypes)) return false

            return true
        }

        /**
         * Calculates the hash code for the current object.
         *
         * The hash code is calculated based on the values of the following properties:
         * - goalCode
         * - comment
         * - willConfirm
         * - proofTypes
         *
         * @return The hash code value for the current object.
         */
        override fun hashCode(): Int {
            var result = goalCode?.hashCode() ?: 0
            result = 31 * result + (comment?.hashCode() ?: 0)
            result = 31 * result + (willConfirm?.hashCode() ?: 0)
            result = 31 * result + proofTypes.contentHashCode()
            return result
        }
    }
}
