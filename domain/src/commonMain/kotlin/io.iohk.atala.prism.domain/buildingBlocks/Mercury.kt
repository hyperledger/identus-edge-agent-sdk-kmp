package io.iohk.atala.prism.domain.buildingBlocks

import io.iohk.atala.prism.domain.models.Message

interface Mercury {

    @Throws() // TODO: Add throw classes
    suspend fun packMessage(message: Message): String

    @Throws() // TODO: Add throw classes
    suspend fun unpackMessage(message: String): Message

    @Throws() // TODO: Add throw classes
    suspend fun sendMessage(message: Message): ByteArray?

    @Throws() // TODO: Add throw classes
    suspend fun sendMessageParseMessage(message: Message): Message?
}
