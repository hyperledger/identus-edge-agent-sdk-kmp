package org.hyperledger.identus.walletsdk.edgeagent

import org.hyperledger.identus.walletsdk.domain.buildingblocks.Mercury
import org.hyperledger.identus.walletsdk.domain.models.Message
import kotlin.jvm.Throws

class MercuryMock : Mercury {
    var packMessageResponse: String? = null
    var unpackMessageResponse: Message? = null
    var sendMessageResponse: ByteArray? = null
    var sendMessageParseMessageResponse: Message? = null

    @Throws()
    override fun packMessage(message: Message): String {
        return packMessageResponse ?: ""
    }

    @Throws()
    override fun unpackMessage(message: String): Message {
        return unpackMessageResponse ?: Message.testable()
    }

    override suspend fun sendMessage(message: Message): ByteArray? {
        return sendMessageResponse
    }

    override suspend fun sendMessageParseResponse(message: Message): Message? {
        return sendMessageParseMessageResponse
    }
}
