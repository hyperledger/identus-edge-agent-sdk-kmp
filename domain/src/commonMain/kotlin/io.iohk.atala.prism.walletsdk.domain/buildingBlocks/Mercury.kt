package io.iohk.atala.prism.walletsdk.domain.buildingBlocks

import io.iohk.atala.prism.walletsdk.domain.models.Message

expect interface Mercury {
    fun packMessage(message: Message): String

    fun unpackMessage(message: String): Message
}
