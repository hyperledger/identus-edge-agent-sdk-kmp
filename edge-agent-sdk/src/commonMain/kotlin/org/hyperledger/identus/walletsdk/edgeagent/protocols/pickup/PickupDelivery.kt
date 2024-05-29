package org.hyperledger.identus.walletsdk.edgeagent.protocols.pickup

import org.hyperledger.identus.walletsdk.domain.models.AttachmentDescriptor
import org.hyperledger.identus.walletsdk.domain.models.Message
import org.hyperledger.identus.walletsdk.edgeagent.EdgeAgentError
import org.hyperledger.identus.walletsdk.edgeagent.protocols.ProtocolType

/**
 * The `PickupDelivery` class represents a pickup delivery protocol type in the Atala PRISM architecture.
 * It is a final class, meaning it cannot be subclassed.
 *
 * @throws (EdgeAgentError.InvalidMessageType):: if the protocol type of the given message is not `ProtocolType.PickupDelivery`.
 *
 * @property id: The id of the pickup delivery.
 * @property type: The protocol type of the pickup delivery, always set to `ProtocolType.PickupDelivery.value`.
 * @property attachments: An array of attachment descriptors associated with the pickup delivery.
 */
final class PickupDelivery
@Throws(EdgeAgentError.InvalidMessageType::class)
constructor(fromMessage: Message) {
    var id: String
    var type = ProtocolType.PickupDelivery.value
    val attachments: Array<AttachmentDescriptor>

    init {
        if (fromMessage.piuri != ProtocolType.PickupDelivery.value) {
            throw EdgeAgentError.InvalidMessageType(
                type = fromMessage.piuri,
                shouldBe = ProtocolType.PickupDelivery.value
            )
        }
        this.id = fromMessage.id
        this.attachments = fromMessage.attachments
    }
}
