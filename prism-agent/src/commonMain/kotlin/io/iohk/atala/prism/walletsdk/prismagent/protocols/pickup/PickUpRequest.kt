package io.iohk.atala.prism.walletsdk.prismagent.protocols.pickup

import io.iohk.atala.prism.domain.models.DID
import io.iohk.atala.prism.domain.models.Message
import io.iohk.atala.prism.walletsdk.prismagent.protocols.ProtocolType
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

data class PickUpRequest(
    val from: DID,
    val to: DID,
    val body: Body
) {
    @Serializable
    data class Body(
        val recipientKey: String? = null,
        val limit: String
    )

    fun makeMessage(): Message {
        return Message(
            piuri = ProtocolType.PickupRequest.value,
            from = from,
            to = to,
            body = Json.encodeToString(body),
        )
    }
}
