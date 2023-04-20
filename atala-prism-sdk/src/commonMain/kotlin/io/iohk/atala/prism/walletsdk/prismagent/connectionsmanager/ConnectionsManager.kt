package io.iohk.atala.prism.walletsdk.prismagent.connectionsmanager

import io.iohk.atala.prism.walletsdk.domain.models.DIDPair

interface ConnectionsManager {
    suspend fun addConnection(paired: DIDPair)
    suspend fun removeConnection(pair: DIDPair): DIDPair?
}
