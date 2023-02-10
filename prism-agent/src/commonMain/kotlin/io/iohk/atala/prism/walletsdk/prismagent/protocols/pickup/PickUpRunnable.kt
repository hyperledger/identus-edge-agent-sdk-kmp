package io.iohk.atala.prism.walletsdk.prismagent.protocols.pickup

import io.iohk.atala.prism.domain.buildingBlocks.Mercury
import io.iohk.atala.prism.domain.models.AttachmentBase64
import io.iohk.atala.prism.domain.models.AttachmentJsonData
import io.iohk.atala.prism.domain.models.Message
import io.iohk.atala.prism.domain.models.PrismAgentError
import io.iohk.atala.prism.walletsdk.prismagent.protocols.ProtocolType

class PickUpRunnable(
    val message: Message,
    private val mercury: Mercury
) {

    data class PickupResponse(val attachmentId: String, val message: Message)

    suspend fun run(): PickupResponse? {
        when (message.piuri) {
            ProtocolType.PickupStatus.value -> {
                return null
            }

            ProtocolType.PickupDelivery.value -> {
                val data = message.attachments
                    .map { attachments ->
                        when (attachments.data::class) {
                            AttachmentBase64::class -> {
                                val attBase64 = attachments.data as AttachmentBase64
                                Pair(attBase64.base64, attachments.id)
                            }

                            AttachmentJsonData::class -> {
                                val attJson = attachments.data as AttachmentJsonData
                                attJson.data?.let {
                                    (Pair(it, attachments.id))
                                }
                            }

                            else -> {
                                null
                            }
                        }
                    }
                    .singleOrNull()
                data?.let {
                    return PickupResponse(it.second, mercury.unpackMessage(it.first))
                }
                return null
            }

            else -> throw PrismAgentError.invalidPickupDeliveryMessageError()
        }
    }
}
