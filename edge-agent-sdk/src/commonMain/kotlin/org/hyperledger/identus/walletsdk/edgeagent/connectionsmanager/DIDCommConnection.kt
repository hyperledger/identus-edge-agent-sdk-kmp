package org.hyperledger.identus.walletsdk.edgeagent.connectionsmanager

import kotlinx.coroutines.flow.Flow
import org.hyperledger.identus.walletsdk.domain.models.Message

/**
 * An interface representing a connection for exchanging messages using the DIDComm protocol. The DIDCommConnection
 * protocol defines methods for awaiting and sending messages using the DIDComm protocol.
 */
interface DIDCommConnection {

    /**
     * Awaits messages from the connection.
     *
     * @return An array of messages received from the connection.
     */
    suspend fun awaitMessages(): Flow<Array<Pair<String, Message>>>

    /**
     * Awaits a response to a specified message ID from the connection.
     *
     * @param id The ID of the message for which to await a response.
     * @return The response message, if one is received.
     */
    suspend fun awaitMessageResponse(id: String): Message?

    /**
     * Sends a message over the connection.
     *
     * @param message The message to send.
     * @return The response message, if one is received.
     */
    suspend fun sendMessage(message: Message): Message?
}
