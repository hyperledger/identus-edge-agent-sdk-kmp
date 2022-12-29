package io.iohk.atala.prism.pluto

import io.iohk.atala.prism.pluto.models.DID
import kotlinx.coroutines.flow.Flow

interface DIDPrivateKeyProvider {

    fun getAll(): Flow<>

    fun getDIDInfo(did: DID)

    fun getPrivateKeys(did: DID)
}