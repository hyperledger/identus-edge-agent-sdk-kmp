package io.iohk.atala.prism.walletsdk.prismagent.protocols

import io.iohk.atala.prism.apollo.uuid.UUID
import io.iohk.atala.prism.domain.models.DID
import io.iohk.atala.prism.domain.models.Message
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
class MediationKeysUpdateList {
    var id: String
    var from: DID
    var to: DID
    var type = ProtocolType.DidcommMediationKeysUpdate.value
    var body: Body

    constructor(
        id: String = UUID.randomUUID4().toString(),
        from: DID,
        to: DID,
        recipientDid: DID
    ) {
        this.id = id
        this.from = from
        this.to = to
        this.body = Body(
            updates = arrayOf(
                Update(recipientDid = recipientDid.toString())
            )
        )
    }

    fun makeMessage(): Message {
        return Message(
            id = id,
            piuri = type,
            from = from,
            to = to,
            fromPrior = null,
            body = Json.encodeToString(body),
            extraHeaders = emptyArray(),
            createdTime = "",
            expiresTimePlus = "",
            attachments = emptyArray(),
            thid = null,
            pthid = null,
            ack = emptyArray(),
            direction = Message.Direction.SENT
        )
    }

    @Serializable
    data class Update(var recipientDid: String, var action: String = "add")

    @Serializable
    data class Body(var updates: Array<Update> = emptyArray()) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as Body

            if (!updates.contentEquals(other.updates)) return false

            return true
        }

        override fun hashCode(): Int {
            return updates.contentHashCode()
        }
    }
}
