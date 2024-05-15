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
import java.util.UUID

/**
 * A class representing a connection acceptance message in the DIDComm protocol. The [ConnectionAccept] class defines
 * properties and methods for encoding, decoding, and sending connection acceptance messages in the DIDComm protocol.
 */
class ConnectionAccept {
    val type: String = ProtocolType.DidcommconnectionResponse.value
    var id: String = UUID.randomUUID().toString()
    lateinit var from: DID
    lateinit var to: DID
    var thid: String? = null
    lateinit var body: Body

    /**
     * Initializes a new instance of the ConnectionAccept struct with the specified parameters.
     *
     * @param from The DID of the sender of the connection acceptance message.
     * @param to The DID of the recipient of the connection acceptance message.
     * @param thid The thread ID of the connection acceptance message.
     * @param body The body of the connection acceptance message.
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
     * Initializes a new instance of the ConnectionAccept struct from the specified message.
     *
     * @param fromMessage The message to decode.
     */
    @Throws(EdgeAgentError.InvalidMessageType::class)
    constructor(fromMessage: Message) {
        if (fromMessage.piuri == ProtocolType.DidcommconnectionResponse.value && fromMessage.from != null && fromMessage.to != null) {
            ConnectionAccept(from = fromMessage.from, to = fromMessage.to, body = Body(fromMessage.body))
        } else {
            throw EdgeAgentError.InvalidMessageType(
                type = fromMessage.piuri,
                shouldBe = ProtocolType.DidcommconnectionResponse.value
            )
        }
    }

    /**
     * Initializes a new instance of the ConnectionAccept struct from the specified request.
     *
     * @param fromRequest The request to use for initialization.
     */
    constructor(fromRequest: ConnectionRequest) {
        ConnectionAccept(
            from = fromRequest.from,
            to = fromRequest.to,
            thid = id,
            body = Body(fromRequest.body.goalCode, fromRequest.body.goal, fromRequest.body.accept)
        )
    }

    /**
     * The `makeMessage` method creates a new `Message` object with the specified parameters.
     *
     * @return A new `Message` object.
     */
    fun makeMessage(): Message {
        return Message(
            id = id,
            piuri = type,
            from = from,
            to = to,
            body = Json.encodeToString(body),
            thid = thid
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
         * Checks if this [Body] object is equal to the specified [other] object.
         *
         * Two [Body] objects are considered equal if their [goalCode], [goal], and [accept] properties are equal.
         *
         * @param other The object to compare for equality. If the [other] object is not of type [Body], the method returns false.
         * @return true if this [Body] object is equal to the specified [other] object, false otherwise.
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
         * Returns a hash code value for the object.
         *
         * The hash code is calculated based on the `goalCode`, `goal`, and `accept` properties.
         * If the `goalCode` is not null, its hash code value is added to the result.
         * If the `goal` is not null, its hash code value is multiplied by 31 and added to the result.
         * If the `accept` array is not null, its content hash code value is multiplied by 31 and added to the result.
         *
         * @return The hash code value for the object.
         */
        override fun hashCode(): Int {
            var result = goalCode?.hashCode() ?: 0
            result = 31 * result + (goal?.hashCode() ?: 0)
            result = 31 * result + (accept?.contentHashCode() ?: 0)
            return result
        }
    }
}
