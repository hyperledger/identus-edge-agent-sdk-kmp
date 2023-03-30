package io.iohk.atala.prism.walletsdk.mercury

import io.iohk.atala.prism.walletsdk.domain.buildingBlocks.Castor
import io.iohk.atala.prism.walletsdk.domain.buildingBlocks.Mercury
import io.iohk.atala.prism.walletsdk.domain.buildingBlocks.Pluto
import io.iohk.atala.prism.walletsdk.domain.models.Message

interface Api {
    fun request(httpMethod: String, url: String, body: Any): ByteArray?
}

interface DIDCommProtocol {
    fun packEncrypted(message: Message): String

    fun unpack(message: String): Message
}

expect class MercuryImpl(
    castor: Castor,
    protocol: DIDCommProtocol,
    api: Api
) : Mercury {
    override fun packMessage(message: Message): String
    override fun unpackMessage(message: String): Message
}
