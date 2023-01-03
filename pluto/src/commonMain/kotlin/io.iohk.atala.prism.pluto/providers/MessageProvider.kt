package io.iohk.atala.prism.pluto.providers

import io.iohk.atala.prism.pluto.models.DID
import io.iohk.atala.prism.pluto.models.Message
import kotlinx.coroutines.flow.Flow

interface MessageProvider {

    fun getAll(): Flow<Message>

    fun getAllFor(did: DID): Flow<Message>

    fun getAllSentTo(did: DID): Flow<Message>

    fun getAllReceivedFrom(did: DID): Flow<Message>

    fun getAllOfType(type: String, relatedWithDID: DID?): Flow<Message>

    fun getAll(from: DID, to: DID): Flow<Message>

    fun getMessage(id: String): Flow<Message?>

}
