package io.iohk.atala.prism.walletsdk.prismagent.connectionsmanager

import io.iohk.atala.prism.walletsdk.domain.models.Message

interface DIDCommConnection {
    suspend fun awaitMessages(): Array<Message>
    suspend fun awaitMessageResponse(id: String): Message?
    suspend fun sendMessage(message: Message): Message?
}
