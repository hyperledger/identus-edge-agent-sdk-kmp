package org.hyperledger.identus.walletsdk.mercury.forward

import io.ktor.http.ContentType
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.hyperledger.identus.walletsdk.domain.models.AttachmentData.AttachmentJsonData
import org.hyperledger.identus.walletsdk.domain.models.AttachmentDescriptor
import org.hyperledger.identus.walletsdk.domain.models.DID
import org.hyperledger.identus.walletsdk.domain.models.Message
import java.util.UUID

/**
 * The ForwardMessage class represents a message for forwarding data using the DIDComm protocol.
 * It contains the necessary information for creating a forward message, including the message body,
 * sender and recipient DIDs, encrypted message, and an optional ID.
 *
 * @param body The body of the forward message.
 * @param from The sender DID of the forward message.
 * @param to The recipient DID of the forward message.
 * @param encryptedMessage The encrypted message to be forwarded.
 * @param id The optional ID of the forward message. If not provided, a random UUID will be generated.
 */
@OptIn(ExperimentalSerializationApi::class)
class ForwardMessage @JvmOverloads constructor(
    val body: ForwardBody,
    val from: DID,
    val to: DID,
    val encryptedMessage: String,
    @EncodeDefault
    val id: String = UUID.randomUUID().toString()
) {
    /**
     * Creates a [Message] object with the provided data.
     *
     * @return The created [Message] object.
     */
    fun makeMessage(): Message {
        val forwardBody = Json.encodeToString(body)
        val attachmentData = AttachmentJsonData(encryptedMessage)
        val attachment =
            AttachmentDescriptor(UUID.randomUUID().toString(), ContentType.Application.Json.toString(), attachmentData)

        val message = Message(
            id = id,
            piuri = TYPE,
            from = from,
            to = to,
            body = forwardBody,
            attachments = arrayOf(attachment)
        )

        return message
    }

    /**
     * Represents the body of a forward message.
     *
     * @property next The next message recipient's DID.
     */
    @Serializable
    data class ForwardBody(val next: String)

    companion object {
        /**
         * The constant variable `TYPE` represents the URL of the type of forward message.
         *
         * @see ForwardMessage
         * @see ForwardMessage.makeMessage
         */
        const val TYPE = "https://didcomm.org/routing/2.0/forward"
    }
}
