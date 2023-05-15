package io.iohk.atala.prism.walletsdk.prismagent.connectionsmanager

import io.iohk.atala.prism.walletsdk.domain.models.Message
import kotlinx.coroutines.flow.Flow

interface DIDCommConnection {
    suspend fun awaitMessages(): Flow<Array<Pair<String, Message>>>
    suspend fun awaitMessageResponse(id: String): Message?
    suspend fun sendMessage(message: Message): Message?
}
