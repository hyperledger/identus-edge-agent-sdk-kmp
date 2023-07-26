package io.iohk.atala.prism.walletsdk.prismagent.protocols.pickup

import io.iohk.atala.prism.apollo.base64.base64UrlDecoded
import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Mercury
import io.iohk.atala.prism.walletsdk.domain.models.AttachmentBase64
import io.iohk.atala.prism.walletsdk.domain.models.AttachmentDescriptor
import io.iohk.atala.prism.walletsdk.domain.models.AttachmentJsonData
import io.iohk.atala.prism.walletsdk.domain.models.Message
import io.iohk.atala.prism.walletsdk.prismagent.PrismAgentError
import io.iohk.atala.prism.walletsdk.prismagent.protocols.ProtocolType

class PickupRunner(message: Message, private val mercury: Mercury) {

    enum class PickupResponseType(val type: String) {
        STATUS("status"),
        DELIVERY("delivery")
    }

    data class PickupResponse(val type: PickupResponseType, val message: Message)

    data class PickupAttachment(
        val attachmentId: String,
        val data: String
    )

    private val message: PickupResponse

    init {
        when (message.piuri) {
            ProtocolType.PickupStatus.value -> {
                this.message = PickupResponse(PickupResponseType.STATUS, message)
            }

            ProtocolType.PickupDelivery.value -> {
                this.message = PickupResponse(PickupResponseType.DELIVERY, message)
            }

            else -> {
                throw PrismAgentError.InvalidMessageType(
                    type = message.piuri,
                    shouldBe = "${ProtocolType.PickupStatus.value} or ${ProtocolType.PickupDelivery.value}"
                )
            }
        }
    }

    suspend fun run(): Array<Pair<String, Message>> {
        return if (message.type == PickupResponseType.DELIVERY) {
            message.message.attachments
                .mapNotNull { processAttachment(it) }
                .map { Pair(it.attachmentId, mercury.unpackMessage(it.data)) }
                .toTypedArray()
        } else {
            arrayOf()
        }
    }

    // TODO: Clean this method
    private fun processAttachment(attachment: AttachmentDescriptor): PickupAttachment? {
        return if (Message.isBase64Attachment(attachment.data)) {
            PickupAttachment(attachmentId = attachment.id, data = (attachment.data as AttachmentBase64).base64.base64UrlDecoded)
        } else if (Message.isJsonAttachment(attachment.data)) {
            PickupAttachment(
                attachmentId = attachment.id,
                data = (attachment.data as AttachmentJsonData).data
            )
        } else {
            null
        }
    }
}
