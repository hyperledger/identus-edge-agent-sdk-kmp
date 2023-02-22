package io.iohk.atala.prism.walletsdk.mercury

import io.iohk.atala.prism.walletsdk.domain.buildingBlocks.Castor
import io.iohk.atala.prism.walletsdk.domain.buildingBlocks.Mercury
import io.iohk.atala.prism.walletsdk.domain.buildingBlocks.Pluto
import io.iohk.atala.prism.walletsdk.domain.models.Message

actual class MercuryImpl actual constructor(castor: Castor, pluto: Pluto): Mercury {
    actual override fun packMessage(message: Message): String {
        TODO("Not yet implemented")
    }

    actual override fun unpackMessage(message: String): Message {
        TODO("Not yet implemented")
    }

    override suspend fun sendMessage(message: Message): ByteArray? {
        TODO("Not yet implemented")
    }

    override suspend fun sendMessageParseMessage(message: Message): Message? {
        TODO("Not yet implemented")
    }
}
