package io.iohk.atala.prism.pluto.providers

import io.iohk.atala.prism.pluto.models.DID
import io.iohk.atala.prism.pluto.models.DIDPrivateKey
import io.iohk.atala.prism.pluto.models.PrivateKey
import kotlinx.coroutines.flow.Flow

interface DIDPrivateKeyProvider {

    fun getAll(): Flow<DIDPrivateKey>

    fun getDIDInfo(did: DID): Flow<DIDPrivateKey>

    fun getPrivateKeys(did: DID): Flow<Array<PrivateKey>?>

}
