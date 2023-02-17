package io.iohk.atala.prism.walletsdk.prismagent.protocols.pickup

import io.iohk.atala.prism.apollo.uuid.UUID
import io.iohk.atala.prism.walletsdk.domain.models.DID
import io.iohk.atala.prism.walletsdk.domain.models.Message
import io.iohk.atala.prism.walletsdk.prismagent.protocols.ProtocolType
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

final class PickupReceived {
    var id: String
    var type = ProtocolType.PickupReceived.value
    val from: DID
    val to: DID
    var body: Body

    constructor(
        id: String = UUID.randomUUID4().toString(),
        from: DID,
        to: DID,
        body: Body,
    ) {
        this.id = id
        this.from = from
        this.to = to
        this.body = body
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
            direction = Message.Direction.SENT,
        )
    }

    @Serializable
    data class Body(var messageIdList: Array<String>)
}
