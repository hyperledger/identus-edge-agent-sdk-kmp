package org.hyperledger.identus.walletsdk.edgeagent.protocols.pickup

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.hyperledger.identus.walletsdk.domain.models.DID
import org.hyperledger.identus.walletsdk.domain.models.Message
import org.hyperledger.identus.walletsdk.edgeagent.MESSAGE_ID_LIST
import org.hyperledger.identus.walletsdk.edgeagent.protocols.ProtocolType
import java.util.UUID

/**
 * Represents a pickup received message.
 *
 * @property id The unique identifier of the pickup received message. If not provided, a random UUID will be generated.
 * @property from The sender's DID (Decentralized Identifier) of the pickup received message.
 * @property to The recipient's DID (Decentralized Identifier) of the pickup received message.
 * @property body The body of the pickup received message.
 */
final class PickupReceived @JvmOverloads constructor(
    var id: String = UUID.randomUUID().toString(),
    val from: DID,
    val to: DID,
    var body: Body
) {
    var type = ProtocolType.PickupReceived.value

    /**
     * Creates a [Message] object for sending a message.
     *
     * @return A [Message] object representing the message to send.
     */
    fun makeMessage(): Message {
        return Message(
            piuri = type,
            from = from,
            to = to,
            body = Json.encodeToString(body),
            extraHeaders = mapOf(Pair("return_route", "all"))
        )
    }

    /**
     * Represents a body of a message.
     *
     * @property messageIdList An array of message IDs.
     */
    @Serializable
    data class Body(@SerialName(MESSAGE_ID_LIST) var messageIdList: Array<String>) {
        /**
         * Overrides the default implementation of `equals` defined in the `Any` class.
         * This method checks if the current instance is equal to the specified [other] object.
         *
         * Two objects are considered equal if they meet the following conditions:
         *  - They refer to the same memory address (identity check)
         *  - They are of the same class
         *  - The [messageIdList] property of both objects is equal
         *
         * @param other The object to compare equality with
         * @return `true` if the objects are equal, `false` otherwise
         */
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Body

            return messageIdList.contentEquals(other.messageIdList)
        }

        /**
         * Returns the hash code value for this object.
         *
         * This method overrides the default implementation of `hashCode` defined in the `Any` class.
         * The hash code of an object is an integer value that represents its unique identity.
         * Two objects that are equal must also have the same hash code.
         *
         * This implementation calculates the hash code based on the content of the `messageIdList` array.
         *
         * @return the hash code value for this object
         */
        override fun hashCode(): Int {
            return messageIdList.contentHashCode()
        }
    }
}
