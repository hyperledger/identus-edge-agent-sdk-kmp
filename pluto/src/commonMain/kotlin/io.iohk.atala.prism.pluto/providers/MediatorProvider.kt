package io.iohk.atala.prism.pluto.providers

import io.iohk.atala.prism.pluto.models.DIDMediator
import kotlinx.coroutines.flow.Flow

interface MediatorProvider {

    fun getAll(): Flow<DIDMediator>

}
