package io.iohk.atala.prism.walletsdk.prismagent.protocols.connection

import io.iohk.atala.prism.apollo.uuid.UUID
import io.iohk.atala.prism.walletsdk.domain.models.DID
import io.iohk.atala.prism.walletsdk.domain.models.Message
import io.iohk.atala.prism.walletsdk.domain.models.PrismAgentError
import io.iohk.atala.prism.walletsdk.prismagent.protocols.ProtocolType
import io.iohk.atala.prism.walletsdk.prismagent.protocols.outOfBand.OutOfBandInvitation
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.jvm.Throws

class ConnectionRequest {

    val type: String = ProtocolType.DidcommconnectionRequest.value
    var id: String = UUID.randomUUID4().toString()
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
        this.from = from
        this.to = to
        this.thid = thid
        this.body = body
    }

    @Throws(PrismAgentError.InvitationIsInvalidError::class)
    constructor(inviteMessage: Message, from: DID) {
        inviteMessage.from?.let { toDID ->
            val body = Json.decodeFromString<Body>(inviteMessage.body)
            ConnectionRequest(from = from, to = toDID, thid = inviteMessage.id, body = body)
        } ?: throw PrismAgentError.InvitationIsInvalidError()
    }

    constructor(inviteMessage: OutOfBandInvitation, from: DID) {
        val toDID = inviteMessage.from
        ConnectionRequest(
            from = from,
            to = DID(toDID),
            thid = inviteMessage.id,
            body = Body(
                goalCode = inviteMessage.body.goalCode,
                goal = inviteMessage.body.goal,
                accept = inviteMessage.body.accept?.toTypedArray()
            )
        )
    }

    @Throws(PrismAgentError.InvalidMessageError::class)
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
            throw PrismAgentError.InvalidMessageError()
        }
    }

    fun makeMessage(): Message {
        return Message(
            piuri = this.type,
            from = this.from,
            to = this.to,
            body = Json.encodeToString(this.body),
            thid = this.thid
        )
    }

    @Serializable
    data class Body(
        @SerialName("goal_code")
        val goalCode: String? = null,
        val goal: String? = null,
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
