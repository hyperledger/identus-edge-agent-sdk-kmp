package io.iohk.atala.prism.walletsdk.prismagent.protocols.pickup

import io.iohk.atala.prism.apollo.uuid.UUID
import io.iohk.atala.prism.walletsdk.domain.models.DID
import io.iohk.atala.prism.walletsdk.domain.models.Message
import io.iohk.atala.prism.walletsdk.prismagent.protocols.ProtocolType
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

final class PickupRequest @JvmOverloads constructor(
    var id: String = UUID.randomUUID4().toString(),
    val from: DID,
    val to: DID,
    var body: Body
) {
    var type = ProtocolType.PickupRequest.value

    fun makeMessage(): Message {
        return Message(
            id = id,
            piuri = type,
            from = from,
            to = to,
            fromPrior = null,
            body = Json.encodeToString(body),
            extraHeaders = mapOf(Pair("return_route", "all")),
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
    data class Body(var recipientKey: String? = null, var limit: Int)
}
