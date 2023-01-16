package io.iohk.atala.prism.domain.buildingBlocks

import io.iohk.atala.prism.domain.models.Message

interface Mercury {

    @Throws()
    suspend fun packMessage(message: Message): String

    @Throws()
    suspend fun unpackMessage(message: String): Message

    @Throws()
    suspend fun sendMessage(message: Message): ByteArray?

    @Throws()
    suspend fun sendMessageParseMessage(message: Message): Message?
}
