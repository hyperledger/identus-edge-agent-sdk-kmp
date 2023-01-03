package io.iohk.atala.prism.pluto.providers

import io.iohk.atala.prism.pluto.models.VerifiableCredential
import kotlinx.coroutines.flow.Flow

interface VerifiableCredentialProvider {

    fun getAll(): Flow<Array<String>> // TODO: Change to VerifiableCredential

    fun getCredential(id: String): Flow<VerifiableCredential?> // TODO: Change to VerifiableCredential

}
