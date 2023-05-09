package io.iohk.atala.prism.walletsdk.prismagent.protocols.pickup

import io.iohk.atala.prism.apollo.uuid.UUID
import io.iohk.atala.prism.walletsdk.domain.models.AttachmentDescriptor
import io.iohk.atala.prism.walletsdk.domain.models.AttachmentJsonData
import io.iohk.atala.prism.walletsdk.domain.models.Message
import io.iohk.atala.prism.walletsdk.domain.models.PrismAgentError
import io.iohk.atala.prism.walletsdk.prismagent.MercuryMock
import io.iohk.atala.prism.walletsdk.prismagent.protocols.ProtocolType
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
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
        assertFailsWith(PrismAgentError.InvalidPickupDeliveryMessageError::class) {
            PickupRunner(message, mercury)
        }
    }

    @Test
    fun testPickupRun_whenMessageIsStatus_thenEmptyArrayMessages() = runTest {
        val message = Message(piuri = ProtocolType.PickupStatus.value, body = "")
        val messages = PickupRunner(message, mercury).run()
        assertTrue(messages.isEmpty())
    }

    @Test
    fun testPickupRun_whenMessageIsDelivery_thenArrayOfMessages() = runTest {
        val attachmentData = AttachmentJsonData("{\"key\":\"value\"")
        val attachmentId = UUID.randomUUID4().toString()
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
