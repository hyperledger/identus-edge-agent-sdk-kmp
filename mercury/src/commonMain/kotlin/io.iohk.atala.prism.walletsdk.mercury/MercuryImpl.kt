package io.iohk.atala.prism.walletsdk.mercury

import io.iohk.atala.prism.walletsdk.domain.buildingBlocks.Castor
import io.iohk.atala.prism.walletsdk.domain.buildingBlocks.Mercury
import io.iohk.atala.prism.walletsdk.domain.buildingBlocks.Pluto
import io.iohk.atala.prism.walletsdk.domain.models.Message

expect class MercuryImpl(
    castor: Castor,
    pluto: Pluto
) : Mercury {
    override fun packMessage(message: Message): String
    override fun unpackMessage(message: String): Message
}
