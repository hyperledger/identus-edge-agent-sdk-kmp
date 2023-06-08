package io.iohk.atala.prism.walletsdk.prismagent.protocols.pickup

import io.iohk.atala.prism.walletsdk.domain.models.AttachmentDescriptor
import io.iohk.atala.prism.walletsdk.domain.models.Message
import io.iohk.atala.prism.walletsdk.prismagent.PrismAgentError
import io.iohk.atala.prism.walletsdk.prismagent.protocols.ProtocolType

final class PickupDelivery
@Throws(PrismAgentError.InvalidMessageType::class)
constructor(fromMessage: Message) {
    var id: String
    var type = ProtocolType.PickupDelivery.value
    val attachments: Array<AttachmentDescriptor>

    init {
        if (fromMessage.piuri != ProtocolType.PickupDelivery.value) {
            throw PrismAgentError.InvalidMessageType(
                type = fromMessage.piuri,
                shouldBe = ProtocolType.PickupDelivery.value
            )
        }
        this.id = fromMessage.id
        this.attachments = fromMessage.attachments
    }
}
