package org.hyperledger.identus.walletsdk.edgeagent.protocols.pickup

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hyperledger.identus.walletsdk.domain.models.AttachmentData.AttachmentJsonData
import org.hyperledger.identus.walletsdk.domain.models.AttachmentDescriptor
import org.hyperledger.identus.walletsdk.domain.models.Message
import org.hyperledger.identus.walletsdk.edgeagent.EdgeAgentError
import org.hyperledger.identus.walletsdk.edgeagent.MercuryMock
import org.hyperledger.identus.walletsdk.edgeagent.protocols.ProtocolType
import org.junit.Before
import org.junit.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class PickupRunnerTest {

    lateinit var message: Message
    lateinit var mercury: MercuryMock

    @Before
    fun setup() {
        mercury = MercuryMock()
    }

    @Test
    fun testPickupRun_whenPiuriNotValid_thenInvalidPickupDeliveryMessageError() {
        val message = Message(piuri = ProtocolType.DidcommRequestPresentation.value, body = "")
        assertFailsWith(EdgeAgentError.InvalidMessageType::class) {
            PickupRunner(message, mercury)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testPickupRun_whenMessageIsStatus_thenEmptyArrayMessages() = runTest {
        val message = Message(piuri = ProtocolType.PickupStatus.value, body = "")
        val messages = PickupRunner(message, mercury).run()
        assertTrue(messages.isEmpty())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testPickupRun_whenMessageIsDelivery_thenArrayOfMessages() = runTest {
        val attachmentData = AttachmentJsonData("{\"key\":\"value\"")
        val attachmentId = UUID.randomUUID().toString()
        val message = Message(
            piuri = ProtocolType.PickupDelivery.value,
            body = "",
            attachments = arrayOf(
                AttachmentDescriptor(
                    id = attachmentId,
                    data = attachmentData
                )
            )
        )
        mercury.unpackMessageResponse = message
        val messages = PickupRunner(message, mercury).run()
        assertTrue(messages.isNotEmpty())
        assertEquals(attachmentId, messages[0].first)
        assertEquals(message, messages[0].second)
    }
}
