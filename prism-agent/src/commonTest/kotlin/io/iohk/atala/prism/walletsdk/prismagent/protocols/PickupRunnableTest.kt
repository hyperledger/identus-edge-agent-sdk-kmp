package io.iohk.atala.prism.walletsdk.prismagent.protocols

import io.iohk.atala.prism.domain.models.AttachmentBase64
import io.iohk.atala.prism.domain.models.AttachmentDescriptor
import io.iohk.atala.prism.domain.models.AttachmentHeader
import io.iohk.atala.prism.domain.models.AttachmentJsonData
import io.iohk.atala.prism.domain.models.Message
import io.iohk.atala.prism.domain.models.PrismAgentError
import io.iohk.atala.prism.walletsdk.prismagent.MercuryMock
import io.iohk.atala.prism.walletsdk.prismagent.protocols.pickup.PickUpRunnable
import io.ktor.util.encodeBase64
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class PickupRunnableTest {

    private lateinit var mercuryMock: MercuryMock

    @BeforeTest
    fun setup() {
        mercuryMock = MercuryMock()
    }

    @Test
    fun testPickUpRunnable_whenInvalidMessage_thenFailsWithInvalidPickupDeliveryMessageError() = runTest {
        val message = Message(
            piuri = "",
            body = ""
        )

        val runnable = PickUpRunnable(message, mercuryMock)
        assertFailsWith<PrismAgentError.invalidPickupDeliveryMessageError> {
            runnable.run()
        }
    }

    @Test
    fun testPickUpRunnable_whenMessageIsPickUpStatus_thenResponseNull() = runTest {
        val message = Message(
            piuri = ProtocolType.PickupStatus.value,
            body = ""
        )

        val runnable = PickUpRunnable(message, mercuryMock)
        val pickUpResponse = runnable.run()
        assertNull(pickUpResponse)
    }

    @Test
    fun testPickUpRunnable_whenMessageIsPickUpDeliveryAndInvalidAttachment_thenResponseNull() = runTest {
        val attachmentJsonData = AttachmentHeader("Invalid")
        val attachmentDescriptor = AttachmentDescriptor(
            "00000000-64ff-4f8f-0000-0000d7513f07",
            data = attachmentJsonData,
            filename = arrayOf("filename")
        )

        val message = Message(
            piuri = ProtocolType.PickupDelivery.value,
            body = "",
            attachments = arrayOf(attachmentDescriptor)
        )

        val runnable = PickUpRunnable(message, mercuryMock)
        val pickUpResponse = runnable.run()
        assertNull(pickUpResponse)
    }

    @Test
    fun testPickUpRunnable_whenMessageIsPickUpDeliveryAndJsonAttachment_thenResponseOk() = runTest {
        val attachmentJsonData = AttachmentJsonData("""{"key": "value"}""")
        val attachmentDescriptor = AttachmentDescriptor(
            "00000000-64ff-4f8f-0000-0000d7513f07",
            data = attachmentJsonData,
            filename = arrayOf("filename")
        )

        val message = Message(
            piuri = ProtocolType.PickupDelivery.value,
            body = "",
            attachments = arrayOf(attachmentDescriptor)
        )

        val expected = Message(
            piuri = ProtocolType.PickupReceived.value,
            body = ""
        )

        mercuryMock.unpackMessageResponse = expected

        val runnable = PickUpRunnable(message, mercuryMock)
        val pickUpResponse = runnable.run()
        assertNotNull(pickUpResponse)
        assertEquals(attachmentDescriptor.id, pickUpResponse.attachmentId)
        assertEquals(expected, pickUpResponse.message)
    }

    @Test
    fun testPickUpRunnable_whenMessageIsPickUpDeliveryAndBase64Attachment_thenResponseOk() = runTest {
        val attachmentBase64Data = AttachmentBase64("""{"key": "value"}""".encodeBase64())
        val attachmentDescriptor = AttachmentDescriptor(
            "00000000-64ff-4f8f-0000-0000d7513f07",
            data = attachmentBase64Data,
            filename = arrayOf("filename")
        )

        val message = Message(
            piuri = ProtocolType.PickupDelivery.value,
            body = "",
            attachments = arrayOf(attachmentDescriptor)
        )

        val expected = Message(
            piuri = ProtocolType.PickupReceived.value,
            body = ""
        )

        mercuryMock.unpackMessageResponse = expected

        val runnable = PickUpRunnable(message, mercuryMock)
        val pickUpResponse = runnable.run()
        assertNotNull(pickUpResponse)
        assertEquals(attachmentDescriptor.id, pickUpResponse.attachmentId)
        assertEquals(expected, pickUpResponse.message)
    }
}
