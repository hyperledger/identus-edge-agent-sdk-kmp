package io.iohk.atala.prism.walletsdk.mercury

import io.iohk.atala.prism.walletsdk.domain.buildingBlocks.Castor
import io.iohk.atala.prism.walletsdk.domain.buildingBlocks.Mercury
import io.iohk.atala.prism.walletsdk.domain.buildingBlocks.Pluto
import io.iohk.atala.prism.walletsdk.domain.models.Message
import kotlin.js.Promise

@OptIn(ExperimentalJsExport::class)
@JsExport
class MercuryImpl constructor(castor: Castor, pluto: Pluto) : Mercury {
    override fun packMessage(message: Message): String {
        TODO("Not yet implemented")
    }

    override fun unpackMessage(message: String): Message {
        TODO("Not yet implemented")
    }

    override fun sendMessage(message: Message): Promise<ByteArray> {
        TODO("Not yet implemented")
    }

    override fun sendMessageParseMessage(message: Message): Promise<Message?> {
        TODO("Not yet implemented")
    }
}
