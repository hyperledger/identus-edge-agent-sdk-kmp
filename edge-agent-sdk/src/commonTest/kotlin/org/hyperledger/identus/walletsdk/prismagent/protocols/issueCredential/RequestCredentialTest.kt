package org.hyperledger.identus.walletsdk.edgeagent.protocols.issueCredential

import org.hyperledger.identus.walletsdk.domain.models.DID
import org.hyperledger.identus.walletsdk.domain.models.Message
import org.hyperledger.identus.walletsdk.edgeagent.EdgeAgentError
import org.hyperledger.identus.walletsdk.edgeagent.helpers.fromIndex
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class RequestCredentialTest {

    @Test
    fun testWhenValidRequestMessageThenInitRequestCredential() {
        val fromDID = DID.fromIndex(index = 0)
        val toDID = DID.fromIndex(index = 1)
        val validRequestCredential = RequestCredential(
            body = RequestCredential.Body(
                goalCode = "test1",
                comment = "test1"
//                formats = arrayOf(
//                    CredentialFormat(
//                        attachId = "test1",
//                        format = "test"
//                    )
//                )
            ),
            attachments = arrayOf(),
            thid = "1",
            from = fromDID,
            to = toDID
        )
        val requestMessage = validRequestCredential.makeMessage()
        val testRequestCredentialFormat = RequestCredential.fromMessage(requestMessage)
        assertEquals(testRequestCredentialFormat, validRequestCredential)
    }

    @Test
    fun testWhenInvalidRequestMessageThenInitRequestCredential() {
        val invalidRequestCredential = Message(
            piuri = "InvalidType",
            from = null,
            to = null,
            body = ""
        )
        assertFailsWith<EdgeAgentError.InvalidMessageType> {
            RequestCredential.fromMessage(invalidRequestCredential)
        }
    }
}
