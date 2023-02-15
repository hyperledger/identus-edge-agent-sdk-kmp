package io.iohk.atala.prism.walletsdk.prismagent.protocols.pickup

import io.iohk.atala.prism.walletsdk.domain.models.AttachmentDescriptor
import io.iohk.atala.prism.walletsdk.domain.models.Message
import io.iohk.atala.prism.walletsdk.domain.models.PrismAgentError
import io.iohk.atala.prism.walletsdk.prismagent.protocols.ProtocolType

final class PickupDelivery {
    var id: String
    var type = ProtocolType.PickupDelivery.value
    val attachments: Array<AttachmentDescriptor>

    constructor(fromMessage: Message) {
        if (fromMessage.piuri != ProtocolType.PickupDelivery.value) {
            throw PrismAgentError.invalidPickupDeliveryMessageError()
        }
        this.id = fromMessage.id
        this.attachments = fromMessage.attachments
    }
}
