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
import org.hyperledger.identus.walletsdk.edgeagent.protocols.ProtocolType
import java.util.UUID

/**
 * Class representing a proposal for a presentation.
 *
 * @property id The ID of the presentation.
 * @property type The type of the presentation.
 * @property body The body of the presentation.
 * @property attachments The attachments of the presentation.
 * @property thid The thread ID of the presentation.
 * @property from The sender of the presentation.
 * @property to The recipient of the presentation.
 * @constructor Creates a ProposePresentation instance.
 * @throws EdgeAgentError.InvalidMessageType If the message type is invalid.
 */
class ProposePresentation {

    lateinit var id: String
    val type = ProtocolType.DidcommProposePresentation.value
    lateinit var body: Body
    lateinit var attachments: Array<AttachmentDescriptor>
    var thid: String? = null
    lateinit var from: DID
    lateinit var to: DID

    /**
     * The `ProposePresentation` class represents a proposal for a presentation in the Prism agent.
     * It is used to create a new `ProposePresentation` object with the given parameters.
     *
     * @param id The ID of the proposal. If not provided, a new random UUID will be generated.
     * @param body The body of the proposal, including goal code, comment, and proof types.
     * @param attachments An array of attachment descriptors for the proposal.
     * @param thid The thread ID of the message.
     * @param from The sender's DID.
     * @param to The recipient's DID.
     */
    @JvmOverloads
    constructor(
        id: String? = UUID.randomUUID().toString(),
        body: Body,
        attachments: Array<AttachmentDescriptor>,
        thid: String?,
        from: DID,
        to: DID
    ) {
        this.id = id ?: UUID.randomUUID().toString()
        this.body = body
        this.attachments = attachments
        this.thid = thid
        this.from = from
        this.to = to
    }

    /**
     * Constructs a ProposePresentation object by processing a Message.
     *
     * @throws EdgeAgentError.InvalidMessageType if the fromMessage does not represent the expected protocol type
     *
     * @param fromMessage the input Message object
     */
    @Throws(EdgeAgentError.InvalidMessageType::class)
    constructor(fromMessage: Message) {
        if (fromMessage.piuri == ProtocolType.DidcommProposePresentation.value &&
            fromMessage.from != null &&
            fromMessage.to != null
        ) {
            ProposePresentation(
                id = fromMessage.id,
                body = Json.decodeFromString(fromMessage.body),
                attachments = fromMessage.attachments,
                thid = fromMessage.thid,
                from = fromMessage.from,
                to = fromMessage.to
            )
        } else {
            throw EdgeAgentError.InvalidMessageType(
                type = fromMessage.piuri,
                shouldBe = ProtocolType.DidcommProposePresentation.value
            )
        }
    }

    /**
     * Creates a [Message] object based on the current state of the [ProposePresentation] instance.
     * The [Message] object includes information about the sender, recipient, message body, and other metadata.
     *
     * @return The created [Message] object.
     */
    fun makeMessage(): Message {
        return Message(
            id = this.id,
            piuri = this.type,
            from = this.from,
            to = this.to,
            body = Json.encodeToString(this.body),
            attachments = this.attachments,
            thid = this.thid
        )
    }

    /**
     * Compares this object with the specified object for equality.
     *
     * @param other the object to compare for equality.
     * @return `true` if the objects are equal, `false` otherwise.
     */
    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (other::class != this::class) return false
        val otherPresentation = other as ProposePresentation
        return otherPresentation.type == this.type &&
            otherPresentation.id == this.id &&
            otherPresentation.body == this.body &&
            otherPresentation.attachments.contentEquals(this.attachments) &&
            otherPresentation.thid == this.thid &&
            otherPresentation.from == this.from &&
            otherPresentation.to == this.to
    }

    /**
     * Calculates the hash code of the [ProposePresentation] instance.
     *
     * The hash code is calculated based on the following fields of the instance:
     * - id
     * - type
     * - body
     * - attachments
     * - thid (nullable)
     * - from
     * - to
     *
     * @return The hash code of the [ProposePresentation] instance.
     */
    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + body.hashCode()
        result = 31 * result + attachments.contentHashCode()
        result = 31 * result + (thid?.hashCode() ?: 0)
        result = 31 * result + from.hashCode()
        result = 31 * result + to.hashCode()
        return result
    }

    /**
     * Represents the body of a message in the ProposePresentation protocol.
     *
     * @property goalCode The goal code for the presentation.
     * @property comment Additional comment about the presentation.
     * @property proofTypes An array of proof types.
     */
    @Serializable
    @OptIn(ExperimentalSerializationApi::class)
    data class Body
    @JvmOverloads constructor(
        @SerialName(GOAL_CODE)
        val goalCode: String? = null,
        val comment: String? = null,
        @EncodeDefault
        @SerialName(PROOF_TYPES)
        val proofTypes: Array<ProofTypes>? = emptyArray()
    ) {
        /**
         * Compares this [Body] object with the specified object for equality.
         *
         * @param other the object to compare for equality.
         * @return `true` if the objects are equal, `false` otherwise.
         */
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as Body

            if (goalCode != other.goalCode) return false
            if (comment != other.comment) return false
            if (!proofTypes.contentEquals(other.proofTypes)) return false

            return true
        }

        /**
         * Calculates the hash code of the [Body] instance.
         *
         * The hash code is calculated based on the following fields of the instance:
         * - goalCode
         * - comment
         * - proofTypes
         *
         * @return The hash code of the [Body] instance.
         */
        override fun hashCode(): Int {
            var result = goalCode?.hashCode() ?: 0
            result = 31 * result + (comment?.hashCode() ?: 0)
            result = 31 * result + proofTypes.contentHashCode()
            return result
        }
    }
}
