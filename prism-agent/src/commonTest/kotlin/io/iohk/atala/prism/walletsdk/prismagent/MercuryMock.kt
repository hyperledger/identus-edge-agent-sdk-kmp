package io.iohk.atala.prism.walletsdk.prismagent

import io.iohk.atala.prism.domain.buildingBlocks.Mercury
import io.iohk.atala.prism.domain.models.Message
import io.iohk.atala.prism.walletsdk.prismagent.protocols.ProtocolType

class MercuryMock : Mercury {

    var unpackMessageResponse: Message = Message(
        piuri = ProtocolType.PickupDelivery.value,
        body = ""
    )

    override suspend fun packMessage(message: Message): String {
        TODO("Not yet implemented")
    }

    override suspend fun unpackMessage(message: String): Message {
        return unpackMessageResponse
    }

    override suspend fun sendMessage(message: Message): ByteArray? {
        TODO("Not yet implemented")
    }

    override suspend fun sendMessageParseMessage(message: Message): Message? {
        TODO("Not yet implemented")
    }
}
