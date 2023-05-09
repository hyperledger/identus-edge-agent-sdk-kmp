package io.iohk.atala.prism.walletsdk.prismagent.protocols.pickup

import io.iohk.atala.prism.apollo.uuid.UUID
import io.iohk.atala.prism.walletsdk.domain.models.DID
import io.iohk.atala.prism.walletsdk.domain.models.Message
import io.iohk.atala.prism.walletsdk.prismagent.protocols.ProtocolType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

final class PickupReceived {
    var id: String
    var type = ProtocolType.PickupReceived.value
    val from: DID
    val to: DID
    var body: Body

    @JvmOverloads
    constructor(
        id: String = UUID.randomUUID4().toString(),
        from: DID,
        to: DID,
        body: Body
    ) {
        this.id = id
        this.from = from
        this.to = to
        this.body = body
    }

    fun makeMessage(): Message {
        return Message(
            piuri = type,
            from = from,
            to = to,
            body = Json.encodeToString(body)
        )
    }

    @Serializable
    data class Body(@SerialName("message_id_list") var messageIdList: Array<String>) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Body

            if (!messageIdList.contentEquals(other.messageIdList)) return false

            return true
        }

        override fun hashCode(): Int {
            return messageIdList.contentHashCode()
        }
    }
}
