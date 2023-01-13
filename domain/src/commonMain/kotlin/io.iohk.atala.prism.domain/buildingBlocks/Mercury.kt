package io.iohk.atala.prism.domain.buildingBlocks

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
