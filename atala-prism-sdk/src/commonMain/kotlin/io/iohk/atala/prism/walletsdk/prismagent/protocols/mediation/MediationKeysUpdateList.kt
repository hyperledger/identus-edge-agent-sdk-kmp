package io.iohk.atala.prism.walletsdk.prismagent.protocols.mediation

import io.iohk.atala.prism.apollo.uuid.UUID
import io.iohk.atala.prism.walletsdk.domain.models.DID
import io.iohk.atala.prism.walletsdk.domain.models.Message
import io.iohk.atala.prism.walletsdk.prismagent.ADD
import io.iohk.atala.prism.walletsdk.prismagent.RECEPIENT_DID
import io.iohk.atala.prism.walletsdk.prismagent.UPDATES
import io.iohk.atala.prism.walletsdk.prismagent.protocols.ProtocolType
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
final class MediationKeysUpdateList {
    var id: String
    var from: DID
    var to: DID
    var type = ProtocolType.DidcommMediationKeysUpdate.value
    var body: Body

    @JvmOverloads
    constructor(
        id: String = UUID.randomUUID4().toString(),
        from: DID,
        to: DID,
        recipientDids: Array<DID>
    ) {
        this.id = id
        this.from = from
        this.to = to
        this.body = Body(
            updates = recipientDids.map {
                Update(recipientDid = it.toString())
            }.toTypedArray()
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
            extraHeaders = emptyMap(),
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
    data class Update
    @OptIn(ExperimentalSerializationApi::class)
    @JvmOverloads
    constructor(
        @SerialName(RECEPIENT_DID)
        var recipientDid: String,
        @EncodeDefault
        var action: String = ADD
    )

    @Serializable
    data class Body @JvmOverloads constructor(var updates: Array<Update> = emptyArray()) {
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

        fun toMapStringAny(): Map<String, Any?> {
            return mapOf(Pair(UPDATES, updates))
        }
    }
}
