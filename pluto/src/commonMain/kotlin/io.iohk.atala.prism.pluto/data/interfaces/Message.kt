package io.iohk.atala.prism.pluto.data.interfaces

import io.iohk.atala.prism.pluto.models.DID
import io.iohk.atala.prism.pluto.models.Message
import kotlinx.coroutines.flow.Flow

interface Message {

    fun getAll(): Flow<Message>

    fun getAllFor(did: DID): Flow<Message>

    fun getAllSentTo(did: DID): Flow<Message>

    fun getAllReceivedFrom(did: DID): Flow<Message>

    fun getAllOfType(type: String, relatedWithDID: DID?): Flow<Message>

    fun getAll(from: DID, to: DID): Flow<Message>

    fun getMessage(id: String): Flow<Message?>

    fun addMessages(messages: Array<Message>)

    fun addMessage(message: Message)

    fun removeMessage(id: String)

    fun removeAll()

}
