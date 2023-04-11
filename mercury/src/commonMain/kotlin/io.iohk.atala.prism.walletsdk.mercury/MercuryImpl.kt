package io.iohk.atala.prism.walletsdk.mercury

import io.iohk.atala.prism.walletsdk.domain.models.Message

interface Api {
    fun request(httpMethod: String, url: String, body: Any): ByteArray?
}

interface DIDCommProtocol {
    fun packEncrypted(message: Message): String

    fun unpack(message: String): Message
}
