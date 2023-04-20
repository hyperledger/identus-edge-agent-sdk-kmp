package io.iohk.atala.prism.walletsdk.domain.buildingblocks

import io.iohk.atala.prism.walletsdk.domain.models.Message

/**
 * TODO(Clarify what Castor methods stand for)
 * TODO(Add method documentations)
 * TODO(Update functions with @throws that are represented in their implementation)
 */
interface Mercury {
    fun packMessage(message: Message): String

    fun unpackMessage(message: String): Message

    suspend fun sendMessage(message: Message): ByteArray?

    suspend fun sendMessageParseMessage(message: Message): Message?
}
