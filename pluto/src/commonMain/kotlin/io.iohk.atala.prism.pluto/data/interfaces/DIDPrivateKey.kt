package io.iohk.atala.prism.pluto.data.interfaces

import io.iohk.atala.prism.pluto.models.DID
import io.iohk.atala.prism.pluto.models.DIDPrivateKey
import io.iohk.atala.prism.pluto.models.PrivateKey
import kotlinx.coroutines.flow.Flow

interface DIDPrivateKey {

    fun getAll(): Flow<DIDPrivateKey>

    fun getDIDInfo(did: DID): Flow<DIDPrivateKey>

    fun getPrivateKeys(did: DID): Flow<Array<PrivateKey>?>

    fun addDID(did: DID, privateKeys: Array<PrivateKey>)

    fun removeDID(did: DID)

    fun removeAll()
}
