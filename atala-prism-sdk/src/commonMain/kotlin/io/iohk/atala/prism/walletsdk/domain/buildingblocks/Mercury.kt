package io.iohk.atala.prism.walletsdk.domain.buildingblocks

import io.iohk.atala.prism.walletsdk.domain.models.Message

interface Mercury {
    fun packMessage(message: Message): String

    fun unpackMessage(message: String): Message

    suspend fun sendMessage(message: Message): ByteArray?

    suspend fun sendMessageParseResponse(message: Message): Message?
}
