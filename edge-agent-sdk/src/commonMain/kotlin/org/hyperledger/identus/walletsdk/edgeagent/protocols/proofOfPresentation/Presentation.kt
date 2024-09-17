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
import org.hyperledger.identus.walletsdk.edgeagent.protocols.ProtocolType
import java.util.UUID

/**
 * Data class representing proof types.
 *
 * @property schema The schema of the proof.
 * @property requiredFields An optional array of required fields for the proof.
 * @property trustIssuers An optional array of trusted issuers for the proof.
 */
@Serializable
@OptIn(ExperimentalSerializationApi::class)
data class ProofTypes @JvmOverloads constructor(
    val schema: String,
    @EncodeDefault
    @SerialName("required_fields")
    val requiredFields: Array<String>? = null,
    @EncodeDefault
    val trustIssuers: Array<String>? = null
) {
    /**
     * Overrides the equals method from the Any class to compare two ProofTypes objects for equality.
     *
     * @param other The object to compare for equality.
     * @return true if the objects are equal, false otherwise.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || this::class != other::class) {
            return false
        }

        other as ProofTypes

        if (schema != other.schema) {
            return false
        }
        if (requiredFields != null) {
            if (other.requiredFields == null) {
                return false
            }
            if (!requiredFields.contentEquals(other.requiredFields)) {
                return false
            }
        } else if (other.requiredFields != null) {
            return false
        }
        if (trustIssuers != null) {
            if (other.trustIssuers == null) {
                return false
            }
            if (!trustIssuers.contentEquals(other.trustIssuers)) {
                return false
            }
        } else if (other.trustIssuers != null) {
            return false
        }

        return true
    }

    /**
     * Overrides the hashCode method from the Any class to generate a hash code for the ProofTypes object.
     *
     * @return The hash code value for the ProofTypes object.
     */
    override fun hashCode(): Int {
        var result = schema.hashCode()
        result = 31 * result + (requiredFields?.contentHashCode() ?: 0)
        result = 31 * result + (trustIssuers?.contentHashCode() ?: 0)
        return result
    }
}

/**
 * The Presentation class represents a presentation message in the EdgeAgent software.
 * It contains the necessary information for constructing a presentation message.
 *
 * @property type The type of the presentation message.
 * @property id The unique identifier for the presentation message.
 * @property body The body of the presentation message, including goal code and comment.
 * @property attachments An array of AttachmentDescriptor objects representing the attachments in the message.
 * @property thid The thread ID of the presentation message.
 * @property from The sender's DID.
 * @property to The recipient's DID.
 */
class Presentation {
    val type = ProtocolType.DidcommPresentation.value
    lateinit var id: String
    lateinit var body: Body
    lateinit var attachments: Array<AttachmentDescriptor>
    var thid: String? = null
    lateinit var from: DID
    lateinit var to: DID

    /**
     * The Presentation class represents a presentation message in the EdgeAgent software.
     * It contains the necessary information for constructing a presentation message.
     *
     * @param id The unique identifier for the presentation message.
     * @param body The body of the presentation message, including goal code and comment.
     * @param attachments An array of AttachmentDescriptor objects representing the attachments in the message.
     * @param thid The thread ID of the presentation message.
     * @param from The sender's DID.
     * @param to The recipient's DID.
     */
    @JvmOverloads
    constructor(
        id: String? = null,
        body: Body,
        attachments: Array<AttachmentDescriptor>,
        thid: String? = null,
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
     * Constructor for creating a Presentation object from a Message object.
     *
     * @param fromMessage The Message object to create Presentation from.
     * @throws EdgeAgentError.InvalidMessageType if the message type does not represent the protocol "didcomm.presentation" or if the message does not have "from" and "to" fields.
     */
    @Throws(EdgeAgentError.InvalidMessageType::class)
    constructor(fromMessage: Message) {
        if (
            fromMessage.piuri == ProtocolType.DidcommPresentation.value &&
            fromMessage.from != null &&
            fromMessage.to != null
        ) {
            val body = Json.decodeFromString<Body>(fromMessage.body)
            Presentation(
                fromMessage.id,
                body,
                fromMessage.attachments,
                fromMessage.thid,
                fromMessage.from,
                fromMessage.to
            )
        } else {
            throw EdgeAgentError.InvalidMessageType(
                type = fromMessage.piuri,
                shouldBe = ProtocolType.DidcommPresentation.value
            )
        }
    }

    /**
     * Creates a new [Message] object based on the provided parameters.
     *
     * @return The newly created [Message] object.
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
     * Compares this Presentation object with the specified object for equality.
     *
     * @param other The object to compare with this Presentation object.
     * @return `true` if the specified object is equal to this Presentation object, `false` otherwise.
     */
    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (other::class != this::class) return false
        val otherPresentation = other as Presentation
        return otherPresentation.type == this.type &&
            otherPresentation.id == this.id &&
            otherPresentation.body == this.body &&
            otherPresentation.attachments.contentEquals(this.attachments) &&
            otherPresentation.thid == this.thid &&
            otherPresentation.from == this.from &&
            otherPresentation.to == this.to
    }

    /**
     * Calculates the hash code value for the Presentation object.
     *
     * The hash code is calculated based on the values of the following properties:
     * - type
     * - id
     * - body
     * - attachments
     * - thid (nullable)
     * - from
     * - to
     *
     * @return The hash code value for the Presentation object.
     */
    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + body.hashCode()
        result = 31 * result + attachments.contentHashCode()
        result = 31 * result + (thid?.hashCode() ?: 0)
        result = 31 * result + from.hashCode()
        result = 31 * result + to.hashCode()
        return result
    }

    /**
     * Represents the body of a Presentation object.
     *
     * @param goalCode The goal code of the presentation body.
     * @param comment The comment associated with the presentation body.
     */
    @Serializable
    data class Body @JvmOverloads constructor(
        val goalCode: String? = null,
        val comment: String? = null
    )

    companion object {
        fun fromMessage(fromMessage: Message): Presentation {
            if (fromMessage.piuri == ProtocolType.DidcommPresentation.value &&
                fromMessage.from != null &&
                fromMessage.to != null
            ) {
                return Presentation(
                    id = fromMessage.id,
                    body = Json.decodeFromString(fromMessage.body) ?: Body(),
                    attachments = fromMessage.attachments,
                    thid = fromMessage.thid,
                    from = fromMessage.from,
                    to = fromMessage.to
                )
            } else {
                throw EdgeAgentError.InvalidMessageType(
                    type = fromMessage.piuri,
                    shouldBe = ProtocolType.DidcommPresentation.value
                )
            }
        }
    }
}
