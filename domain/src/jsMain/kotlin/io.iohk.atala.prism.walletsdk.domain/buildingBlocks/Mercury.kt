package io.iohk.atala.prism.walletsdk.domain.buildingBlocks

import io.iohk.atala.prism.walletsdk.domain.models.Message
import kotlin.js.Promise

@OptIn(ExperimentalJsExport::class)
@JsExport
actual interface Mercury {
    actual fun packMessage(message: Message): String

    actual fun unpackMessage(message: String): Message

    fun sendMessage(message: Message): Promise<ByteArray>

    fun sendMessageParseMessage(message: Message): Promise<Message?>
}
