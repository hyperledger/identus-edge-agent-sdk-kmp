package io.iohk.atala.prism.pluto.providers

import io.iohk.atala.prism.pluto.models.DID
import io.iohk.atala.prism.pluto.models.DIDKeyPairIndex
import kotlinx.coroutines.flow.Flow

interface DIDProvider {

    fun getAll(): Flow<DIDKeyPairIndex>

    fun getDIDInfo(alias: String): Flow<DIDKeyPairIndex>

    fun getDIDInfo(did: DID): Flow<DIDKeyPairIndex>

    fun getDIDInfo(keyPairIndex: Int): Flow<DIDKeyPairIndex>

    fun getLastKeyPairIndex(): Flow<Int>
}
