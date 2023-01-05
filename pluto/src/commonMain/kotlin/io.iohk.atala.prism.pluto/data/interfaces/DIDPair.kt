package io.iohk.atala.prism.pluto.data.interfaces

import io.iohk.atala.prism.pluto.models.DID
import io.iohk.atala.prism.pluto.models.DIDPair
import kotlinx.coroutines.flow.Flow

interface DIDPair {

    fun getAll(): Flow<DIDPair>

    fun getPair(did: DID): Flow<DIDPair>

    fun getPair(name: String): Flow<DIDPair>

    fun addDIDPair(holder: DID, other: DID, name: String)

    fun removeDIDPair(holder: DID, other: DID)

    fun removeAll()
}
