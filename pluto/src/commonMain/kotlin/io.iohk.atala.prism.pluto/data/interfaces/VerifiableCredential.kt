package io.iohk.atala.prism.pluto.data.interfaces

import io.iohk.atala.prism.pluto.models.VerifiableCredential
import kotlinx.coroutines.flow.Flow

interface VerifiableCredential {

    fun getAll(): Flow<Array<VerifiableCredential>>

    fun getCredential(id: String): Flow<VerifiableCredential?>

    fun addCredentials(credentials: Array<VerifiableCredential>)

    fun addCredential(credential: VerifiableCredential)

    fun removeCredential(id: String)

    fun removeAll()
}
