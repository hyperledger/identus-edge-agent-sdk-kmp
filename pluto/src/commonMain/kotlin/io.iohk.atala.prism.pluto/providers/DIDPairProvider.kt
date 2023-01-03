package io.iohk.atala.prism.pluto.providers

import io.iohk.atala.prism.pluto.models.DID
import io.iohk.atala.prism.pluto.models.DIDPair
import kotlinx.coroutines.flow.Flow

interface DIDPairProvider {

    fun getAll(): Flow<DIDPair>

    fun getPair(did: DID): Flow<DIDPair>

    fun getPair(name: String): Flow<DIDPair>

}
