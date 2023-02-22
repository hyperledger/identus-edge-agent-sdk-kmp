package io.iohk.atala.prism.walletsdk.domain.buildingBlocks

import io.iohk.atala.prism.walletsdk.domain.models.Message

actual interface Mercury {
    actual fun packMessage(message: Message): String

    actual fun unpackMessage(message: String): Message

    @Throws() // TODO: Add throw classes
    suspend fun sendMessage(message: Message): ByteArray?

    @Throws() // TODO: Add throw classes
    suspend fun sendMessageParseMessage(message: Message): Message?
}
