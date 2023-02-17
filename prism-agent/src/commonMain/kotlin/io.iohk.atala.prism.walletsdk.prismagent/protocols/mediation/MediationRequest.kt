package io.iohk.atala.prism.walletsdk.prismagent.protocols.mediation

import io.iohk.atala.prism.apollo.uuid.UUID
import io.iohk.atala.prism.walletsdk.domain.models.DID
import io.iohk.atala.prism.walletsdk.domain.models.Message
import io.iohk.atala.prism.walletsdk.prismagent.protocols.ProtocolType

final class MediationRequest(
    val id: String,
    val type: String = ProtocolType.DidcommMediationRequest.value,
    val from: DID,
    val to: DID,
) {
    constructor(
        from: DID,
        to: DID,
    ) : this(
        id = UUID.randomUUID4().toString(),
        from = from,
        to = to,
    )

    fun makeMessage(): Message {
        return Message(
            id = id,
            piuri = type,
            from = from,
            to = to,
            fromPrior = null,
            body = "",
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as MediationRequest

        if (id != other.id) return false
        if (type != other.type) return false
        if (from != other.from) return false
        if (to != other.to) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + from.hashCode()
        result = 31 * result + to.hashCode()
        return result
    }
}
