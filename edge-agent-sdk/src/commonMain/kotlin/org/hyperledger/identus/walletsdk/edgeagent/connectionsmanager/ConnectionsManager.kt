package org.hyperledger.identus.walletsdk.edgeagent.connectionsmanager

import org.hyperledger.identus.walletsdk.domain.models.DIDPair

/**
 * This interface represents a manager for managing connections between different entities.
 * It provides methods for adding and removing connections.
 */
interface ConnectionsManager {
    /**
     * Adds a connection to the manager.
     *
     * @param paired The [DIDPair] representing the connection to be added.
     */
    suspend fun addConnection(paired: DIDPair)

    /**
     * Removes a connection from the manager.
     *
     * @param pair The [DIDPair] representing the connection to be removed.
     * @return The [DIDPair] object that was removed from the manager, or null if the connection was not found.
     */
    suspend fun removeConnection(pair: DIDPair): DIDPair?

    /**
     * Stops a connection
     */
    fun stopConnection()
}
