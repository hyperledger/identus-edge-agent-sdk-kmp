package io.iohk.atala.prism.walletsdk.prismagent.connectionsManager

import io.iohk.atala.prism.domain.models.Message

interface DIDCommConnection {
    suspend fun awaitMessages(): Array<Message>
    suspend fun awaitMessageResponse(id: String): Message?
    suspend fun sendMessage(message: Message): Message?
}
