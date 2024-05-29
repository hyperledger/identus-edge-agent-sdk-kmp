package org.hyperledger.identus.walletsdk.edgeagent.protocols.issueCredential

import org.hyperledger.identus.walletsdk.domain.models.DID
import org.hyperledger.identus.walletsdk.domain.models.Message
import org.hyperledger.identus.walletsdk.edgeagent.EdgeAgentError
import org.hyperledger.identus.walletsdk.edgeagent.helpers.fromIndex
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class IssueCredentialTest {

    @Test
    fun testCredentialFromMessage_whenValidIssueMessage_thenInitIssueCredential() {
        val fromDID = DID.fromIndex(index = 0)
        val toDID = DID.fromIndex(index = 1)
        val validIssueCredential = IssueCredential(
            body = IssueCredential.Body(
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
        val issueMessage = validIssueCredential.makeMessage()
        val testIssueCredentialFormat = IssueCredential.fromMessage(issueMessage)
        assertEquals(testIssueCredentialFormat, validIssueCredential)
    }

    @Test
    fun testWhenInvalidIssueMessageThenInitIssueCredential() {
        val invalidIssueCredential = Message(
            piuri = "InvalidType",
            from = null,
            to = null,
            body = ""
        )
        assertFailsWith<EdgeAgentError.InvalidMessageType> {
            IssueCredential.fromMessage(invalidIssueCredential)
        }
    }

    @Test
    fun testWhenValidRequestMessageThenInitIssueCredential() {
        val fromDID = DID.fromIndex(index = 0)
        val toDID = DID.fromIndex(index = 1)
        val validRequestCredential = RequestCredential(
            body = RequestCredential.Body(
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
        val testIssueCredential = IssueCredential.makeIssueFromRequestCedential(requestMessage)

        assertEquals(validRequestCredential.from, testIssueCredential.to)
        assertEquals(validRequestCredential.to, testIssueCredential.from)
        assertEquals(validRequestCredential.attachments, testIssueCredential.attachments)
        assertEquals(validRequestCredential.id, testIssueCredential.thid)
        assertEquals(testIssueCredential.thid, requestMessage.id)
        assertEquals(validRequestCredential.body.goalCode, testIssueCredential.body.goalCode)
        assertEquals(validRequestCredential.body.comment, testIssueCredential.body.comment)
//        assertContentEquals(validRequestCredential.body.formats, testIssueCredential.body.formats)
    }
}
