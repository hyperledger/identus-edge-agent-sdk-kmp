package io.iohk.atala.prism.walletsdk.prismagent.protocols.connection

import io.iohk.atala.prism.apollo.uuid.UUID
import io.iohk.atala.prism.walletsdk.domain.models.DID
import io.iohk.atala.prism.walletsdk.domain.models.Message
import io.iohk.atala.prism.walletsdk.domain.models.PrismAgentError
import io.iohk.atala.prism.walletsdk.prismagent.GOAL_CODE
import io.iohk.atala.prism.walletsdk.prismagent.protocols.ProtocolType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.jvm.Throws

/**
 * A class representing a connection acceptance message in the DIDComm protocol. The [ConnectionAccept] class defines
 * properties and methods for encoding, decoding, and sending connection acceptance messages in the DIDComm protocol.
 */
class ConnectionAccept {
    val type: String = ProtocolType.DidcommconnectionResponse.value
    lateinit var id: String
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
        id = UUID.randomUUID4().toString()
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
    @Throws(PrismAgentError.InvalidMessageError::class)
    constructor(fromMessage: Message) {
        if (fromMessage.piuri == ProtocolType.DidcommconnectionResponse.value && fromMessage.from != null && fromMessage.to != null) {
            ConnectionAccept(from = fromMessage.from, to = fromMessage.to, body = Body(fromMessage.body))
        } else {
            throw PrismAgentError.InvalidMessageError()
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
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as Body

            if (goalCode != other.goalCode) return false
            if (goal != other.goal) return false
            if (accept != null) {
                if (other.accept == null) return false
                if (!accept.contentEquals(other.accept)) return false
            } else if (other.accept != null) return false

            return true
        }

        override fun hashCode(): Int {
            var result = goalCode?.hashCode() ?: 0
            result = 31 * result + (goal?.hashCode() ?: 0)
            result = 31 * result + (accept?.contentHashCode() ?: 0)
            return result
        }
    }
}
