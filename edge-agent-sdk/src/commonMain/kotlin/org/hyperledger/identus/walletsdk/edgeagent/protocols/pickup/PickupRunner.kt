package org.hyperledger.identus.walletsdk.edgeagent.protocols.pickup

import org.hyperledger.identus.walletsdk.domain.buildingblocks.Mercury
import org.hyperledger.identus.walletsdk.domain.models.AttachmentDescriptor
import org.hyperledger.identus.walletsdk.domain.models.Message
import org.hyperledger.identus.walletsdk.edgeagent.EdgeAgentError
import org.hyperledger.identus.walletsdk.edgeagent.protocols.ProtocolType

/**
 * The `PickupRunner` class is responsible for processing `Message` objects related to pickup requests and delivering them
 * using the `Mercury` service. It provides methods to run the pickup request and process the response.
 *
 * @property message The pickup request message to process.
 * @property mercury The `Mercury` service to use for message delivery.
 */
class PickupRunner(message: Message, private val mercury: Mercury) {

    /**
     * The [PickupResponseType] enum class represents the response types for the pickup functionality.
     * The response types can be either "status" or "delivery".
     *
     * @property type The string representation of the pickup response type.
     * @constructor Creates a [PickupResponseType] enum with the given type string.
     */
    enum class PickupResponseType(val type: String) {
        STATUS("status"),
        DELIVERY("delivery"),
        PROBLEM_REPORT("problem_report")
    }

    /**
     * The `PickupResponse` data class represents the response from the pickup functionality.
     * It includes the type of response (`PickupResponseType`) and a `Message` object.
     *
     * @property type The type of pickup response.
     * @property message The message object.
     */
    data class PickupResponse(val type: PickupResponseType, val message: Message)

    /**
     * The [PickupAttachment] data class represents an attachment in the pickup functionality.
     * It includes the attachment ID and the attachment data.
     *
     * @property attachmentId The ID of the attachment.
     * @property data The data of the attachment.
     */
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

            ProtocolType.ProblemReport.value -> {
                this.message = PickupResponse(PickupResponseType.PROBLEM_REPORT, message)
            }

            else -> {
                throw EdgeAgentError.InvalidMessageType(
                    type = message.piuri,
                    shouldBe = "${ProtocolType.PickupStatus.value} or ${ProtocolType.PickupDelivery.value}"
                )
            }
        }
    }

    /**
     * Runs the pickup functionality and returns an array of pairs containing attachment IDs and unpacked messages.
     * If the type of the pickup response is DELIVERY, it processes the attachments, unpacks the messages, and returns them as an array.
     * If the type is not DELIVERY, it returns an empty array.
     *
     * @return An array of pairs containing attachment IDs and unpacked messages.
     */
    fun run(): Array<Pair<String, Message>> {
        return if (message.type == PickupResponseType.DELIVERY) {
            message.message.attachments
                .mapNotNull { processAttachment(it) }
                .map { Pair(it.attachmentId, mercury.unpackMessage(it.data)) }
                .toTypedArray()
        } else {
            arrayOf()
        }
    }

    /**
     * Process the given attachment and convert it to a PickupAttachment object.
     *
     * @param attachment The AttachmentDescriptor to be processed.
     * @return The PickupAttachment object if the attachment data is of type AttachmentBase64 or AttachmentJsonData, otherwise null.
     */
    private fun processAttachment(attachment: AttachmentDescriptor): PickupAttachment? {
        val data = attachment.data.getDataAsJsonString()
        val id = attachment.id

        return PickupAttachment(
            attachmentId = id,
            data = data
        )
    }
}
