package io.iohk.atala.prism.walletsdk.mercury

import io.iohk.atala.prism.walletsdk.domain.models.Message

class ProtocolMock: DIDCommProtocol {
    var packEncryptedWasCalledWith: Message? = null

    override fun packEncrypted(message: Message): String {
        packEncryptedWasCalledWith = message
        return "mock"
    }

    var unpackWasCalledWith: String? = null

    override fun unpack(message: String): Message {
        unpackWasCalledWith = message
        return Message(piuri = "piuri", body = "tesstBody")
    }
}
