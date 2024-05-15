package org.hyperledger.identus.walletsdk.edgeagent.protocols.pickup

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.hyperledger.identus.walletsdk.domain.models.DID
import org.hyperledger.identus.walletsdk.domain.models.Message
import org.hyperledger.identus.walletsdk.edgeagent.protocols.ProtocolType
import java.util.UUID

/**
 * A class representing a pickup request.
 *
 * @property id The unique identifier of the pickup request.
 * @property from The sender's DID (Decentralized Identifier).
 * @property to The receiver's DID (Decentralized Identifier).
 * @property body The body of the pickup request.
 * @property type The type of the pickup request.
 * @constructor Creates a pickup request with the specified parameters.
 */
final class PickupRequest @JvmOverloads constructor(
    var id: String = UUID.randomUUID().toString(),
    val from: DID,
    val to: DID,
    var body: Body
) {
    var type = ProtocolType.PickupRequest.value

    /**
     * Creates a [Message] object using the provided parameters.
     *
     * @return The created [Message] object.
     */
    fun makeMessage(): Message {
        return Message(
            id = id,
            piuri = type,
            from = from,
            to = to,
            fromPrior = null,
            body = Json.encodeToString(body),
            extraHeaders = mapOf(Pair("return_route", "all")),
            createdTime = "",
            expiresTimePlus = "",
            attachments = emptyArray(),
            thid = null,
            pthid = null,
            ack = emptyArray(),
            direction = Message.Direction.SENT
        )
    }

    /**
     * A class representing the body of a message.
     *
     * @property recipientKey The key of the message recipient.
     * @property limit The maximum number of messages to pick up.
     * @constructor Creates a Body object with the specified parameters.
     */
    @Serializable
    data class Body(var recipientKey: String? = null, var limit: Int)
}
