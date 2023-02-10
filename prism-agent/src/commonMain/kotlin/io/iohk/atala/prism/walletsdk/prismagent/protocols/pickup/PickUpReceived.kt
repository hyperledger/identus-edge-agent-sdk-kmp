package io.iohk.atala.prism.walletsdk.prismagent.protocols.pickup

import io.iohk.atala.prism.domain.models.DID
import io.iohk.atala.prism.domain.models.Message
import io.iohk.atala.prism.walletsdk.prismagent.protocols.ProtocolType
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

data class PickUpReceived(
    val from: DID,
    val to: DID,
    val body: Body
) {

    @Serializable
    data class Body(
        val messageIdList: Array<String> = arrayOf()
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false
            other as Body
            if (!messageIdList.contentEquals(other.messageIdList)) return false
            return true
        }

        override fun hashCode(): Int {
            return messageIdList.contentHashCode()
        }
    }

    fun makeMessage(): Message {
        return Message(
            piuri = ProtocolType.PickupReceived.value,
            from = from,
            to = to,
            body = Json.encodeToString(body),
        )
    }
}
