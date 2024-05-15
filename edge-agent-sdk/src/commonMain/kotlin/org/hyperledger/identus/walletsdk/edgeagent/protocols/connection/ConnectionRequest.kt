package org.hyperledger.identus.walletsdk.edgeagent.protocols.connection

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.hyperledger.identus.walletsdk.domain.models.DID
import org.hyperledger.identus.walletsdk.domain.models.Message
import org.hyperledger.identus.walletsdk.edgeagent.EdgeAgentError
import org.hyperledger.identus.walletsdk.edgeagent.GOAL_CODE
import org.hyperledger.identus.walletsdk.edgeagent.protocols.ProtocolType
import org.hyperledger.identus.walletsdk.edgeagent.protocols.outOfBand.OutOfBandInvitation
import java.util.UUID

/**
 * A class representing a connection request message in the DIDComm protocol. The [ConnectionRequest] class defines
 * properties and methods for encoding, decoding, and sending connection request messages in the DIDComm protocol.
 */
class ConnectionRequest {
    val type: String = ProtocolType.DidcommconnectionRequest.value
    var id: String = UUID.randomUUID().toString()
    lateinit var from: DID
    lateinit var to: DID
    var thid: String? = null
    lateinit var body: Body

    /**
     * Represents a connection request message in the messaging protocol.
     *
     * @param from The sender's DID.
     * @param to The recipient's DID.
     * @param thid The thread ID.
     * @param body The body of the connection request message.
     */
    constructor(
        from: DID,
        to: DID,
        thid: String? = null,
        body: Body
    ) {
        this.from = from
        this.to = to
        this.thid = thid
        this.body = body
    }

    /**
     * Initializes a new instance of the ConnectionRequest struct from the specified invitation message.
     *
     * @param inviteMessage The invitation message to use for initialization.
     * @param from The DID of the sender of the connection request message.
     */
    @Throws(EdgeAgentError.InvitationIsInvalidError::class)
    constructor(inviteMessage: Message, from: DID) {
        inviteMessage.from?.let { toDID ->
            val body = Json.decodeFromString<Body>(inviteMessage.body)
            ConnectionRequest(from = from, to = toDID, thid = inviteMessage.id, body = body)
        } ?: throw EdgeAgentError.InvitationIsInvalidError()
    }

    /**
     * Initializes a new instance of the ConnectionRequest struct from the specified out-of-band invitation.
     *
     * @param inviteMessage The out-of-band invitation to use for initialization.
     * @param from The DID of the sender of the connection request message.
     */
    constructor(inviteMessage: OutOfBandInvitation, from: DID) : this(
        from,
        DID(inviteMessage.from),
        inviteMessage.id,
        Body(
            goalCode = inviteMessage.body.goalCode,
            goal = inviteMessage.body.goal,
            accept = inviteMessage.body.accept?.toTypedArray()
        )
    )

    /**
     * Initializes a new instance of the ConnectionRequest struct from the specified message.
     *
     * @param fromMessage The message to decode.
     */
    @Throws(EdgeAgentError.InvalidMessageType::class)
    constructor(fromMessage: Message) {
        if (
            fromMessage.piuri == ProtocolType.DidcommconnectionRequest.value &&
            fromMessage.from != null &&
            fromMessage.to != null
        ) {
            ConnectionRequest(
                from = fromMessage.from,
                to = fromMessage.to,
                thid = fromMessage.id,
                body = Json.decodeFromString(fromMessage.body)
            )
        } else {
            throw EdgeAgentError.InvalidMessageType(
                type = fromMessage.piuri,
                shouldBe = ProtocolType.DidcommconnectionRequest.value
            )
        }
    }

    /**
     * Creates a [Message] object based on the current state of the [ConnectionRequest] instance.
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
            thid = this.thid
        )
    }

    /**
     * The body of the connection acceptance message, which is the same as the body of the invitation message
     */
    @Serializable
    data class Body(
        /**
         * The goal code of the connection acceptance message.
         */
        @SerialName(GOAL_CODE)
        val goalCode: String? = null,
        /**
         * The goal of the connection acceptance message
         */
        val goal: String? = null,
        /**
         * An array of strings representing the accepted message types
         */
        val accept: Array<String>? = null
    ) {
        /**
         * Checks if the current object is equal to the specified object.
         *
         * @param other The other object to compare.
         * @return true if the objects are equal, false otherwise.
         */
        override fun equals(other: Any?): Boolean {
            if (this === other) {
                return true
            }
            if (other == null || this::class != other::class) {
                return false
            }

            other as Body

            if (goalCode != other.goalCode) {
                return false
            }
            if (goal != other.goal) {
                return false
            }
            if (accept != null) {
                if (other.accept == null) {
                    return false
                }
                if (!accept.contentEquals(other.accept)) {
                    return false
                }
            } else if (other.accept != null) {
                return false
            }

            return true
        }

        /**
         * Generates a hash code for the object based on its properties.
         *
         * @return The hash code value.
         */
        override fun hashCode(): Int {
            var result = goalCode?.hashCode() ?: 0
            result = 31 * result + (goal?.hashCode() ?: 0)
            result = 31 * result + (accept?.contentHashCode() ?: 0)
            return result
        }
    }
}
