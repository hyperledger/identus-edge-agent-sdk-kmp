package io.iohk.atala.prism.walletsdk.mercury

import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Castor
import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Mercury
import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Pluto
import io.iohk.atala.prism.walletsdk.domain.models.Message

class MercuryImpl(castor: Castor, pluto: Pluto) : Mercury {
    override fun packMessage(message: Message): String {
        TODO("Not yet implemented")
    }

    override fun unpackMessage(message: String): Message {
        TODO("Not yet implemented")
    }

    override suspend fun sendMessage(message: Message): ByteArray? {
        TODO("Not yet implemented")
    }

    override suspend fun sendMessageParseMessage(message: Message): Message? {
        TODO("Not yet implemented")
    }
}
