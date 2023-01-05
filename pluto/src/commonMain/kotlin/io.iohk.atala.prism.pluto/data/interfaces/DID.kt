package io.iohk.atala.prism.pluto.data.interfaces

import io.iohk.atala.prism.pluto.models.DID
import io.iohk.atala.prism.pluto.models.DIDKeyPairIndex
import kotlinx.coroutines.flow.Flow

interface DID {

    fun getAll(): Flow<DIDKeyPairIndex>

    fun getDIDInfo(alias: String): Flow<DIDKeyPairIndex>

    fun getDIDInfo(did: DID): Flow<DIDKeyPairIndex>

    fun getDIDInfo(keyPairIndex: Int): Flow<DIDKeyPairIndex>

    fun getLastKeyPairIndex(): Flow<Int>

    fun addDID(did: DID, keyPairIndex: Int, alias: String? = null)

    fun removeDID(did: DID)

    fun removeAll()
}
