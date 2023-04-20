package io.iohk.atala.prism.walletsdk.prismagent.protocols.connection

import io.iohk.atala.prism.apollo.uuid.UUID
import io.iohk.atala.prism.walletsdk.domain.models.DID
import io.iohk.atala.prism.walletsdk.domain.models.Message
import io.iohk.atala.prism.walletsdk.domain.models.PrismAgentError
import io.iohk.atala.prism.walletsdk.prismagent.protocols.ProtocolType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ConnectionAccept {

    val type: String = ProtocolType.DidcommconnectionResponse.value
    lateinit var id: String
    lateinit var from: DID
    lateinit var to: DID
    var thid: String? = null
    lateinit var body: Body

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

    constructor(fromMessage: Message) {
        if (fromMessage.piuri == ProtocolType.DidcommconnectionResponse.value && fromMessage.from != null && fromMessage.to != null) {
            ConnectionAccept(from = fromMessage.from!!, to = fromMessage.to!!, body = Body(fromMessage.body))
        } else {
            throw PrismAgentError.InvalidMessageError()
        }
    }

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

    @Serializable
    data class Body(
        @SerialName("goal_code") val goalCode: String? = null,
        val goal: String? = null,
        val accept: Array<String>? = null,
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
