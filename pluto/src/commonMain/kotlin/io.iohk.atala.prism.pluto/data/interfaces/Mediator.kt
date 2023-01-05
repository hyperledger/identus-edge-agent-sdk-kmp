package io.iohk.atala.prism.pluto.data.interfaces

import io.iohk.atala.prism.pluto.models.DID
import io.iohk.atala.prism.pluto.models.DIDMediator
import kotlinx.coroutines.flow.Flow

interface Mediator {

    fun getAll(): Flow<DIDMediator>

    fun addMediator(peer: DID, routingDID: DID, mediatorDID: DID)

    fun removeMediator(peer: DID)
}
