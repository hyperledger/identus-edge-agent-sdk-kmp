package io.iohk.atala.prism.walletsdk.prismagent.protocols.pickup

import io.iohk.atala.prism.walletsdk.domain.models.AttachmentDescriptor
import io.iohk.atala.prism.walletsdk.domain.models.Message
import io.iohk.atala.prism.walletsdk.domain.models.PrismAgentError
import io.iohk.atala.prism.walletsdk.prismagent.protocols.ProtocolType
import kotlin.jvm.Throws

final class PickupDelivery
@Throws(PrismAgentError.InvalidPickupDeliveryMessageError::class)
constructor(fromMessage: Message) {
    var id: String
    var type = ProtocolType.PickupDelivery.value
    val attachments: Array<AttachmentDescriptor>

    init {
        if (fromMessage.piuri != ProtocolType.PickupDelivery.value) {
            throw PrismAgentError.InvalidPickupDeliveryMessageError()
        }
        this.id = fromMessage.id
        this.attachments = fromMessage.attachments
    }
}
