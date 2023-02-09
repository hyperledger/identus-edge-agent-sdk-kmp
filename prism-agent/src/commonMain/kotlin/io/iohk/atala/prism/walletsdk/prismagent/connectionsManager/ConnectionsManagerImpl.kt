package io.iohk.atala.prism.walletsdk.prismagent.connectionsManager

import io.iohk.atala.prism.domain.models.DIDPair

class ConnectionsManagerImpl : ConnectionsManager {
    override suspend fun addConnection(paired: DIDPair) {
        TODO("Not yet implemented")
    }

    override suspend fun removeConnection(pair: DIDPair): DIDPair? {
        TODO("Not yet implemented")
    }
}
