package org.hyperledger.identus.walletsdk.edgeagent.protocols.mediation

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.hyperledger.identus.walletsdk.domain.models.DID
import org.hyperledger.identus.walletsdk.domain.models.Message
import org.hyperledger.identus.walletsdk.edgeagent.ADD
import org.hyperledger.identus.walletsdk.edgeagent.RECEPIENT_DID
import org.hyperledger.identus.walletsdk.edgeagent.UPDATES
import org.hyperledger.identus.walletsdk.edgeagent.protocols.ProtocolType
import java.util.UUID

/**
 * MediationKeysUpdateList is a serializable class representing the list of keys updated in a mediation process.
 *
 * @property id The ID of the mediation keys update list.
 * @property from The sender of the mediation keys update list.
 * @property to The recipient of the mediation keys update list.
 * @property type The type of the mediation keys update list.
 * @property body The body of the mediation keys update list, containing the updates.
 */
@Serializable
final class MediationKeysUpdateList {
    var id: String
    var from: DID
    var to: DID
    var type = ProtocolType.DidcommMediationKeysUpdate.value
    var body: Body

    /**
     * The `MediationKeysUpdateList` class represents a list of updates for mediation keys.
     * It is used to create a `Message` object with the specified parameters.
     *
     * @property id The ID of the `MediationKeysUpdateList` object.
     * @property from The sender DID.
     * @property to The recipient DID.
     * @property recipientDids An array of recipient DIDs.
     */
    @JvmOverloads
    constructor(
        id: String = UUID.randomUUID().toString(),
        from: DID,
        to: DID,
        recipientDids: Array<DID>
    ) {
        this.id = id
        this.from = from
        this.to = to
        this.body = Body(
            updates = recipientDids.map {
                Update(recipientDid = it.toString())
            }.toTypedArray()
        )
    }

    /**
     * This method creates a [Message] object with the specified parameters.
     * The [Message] object represents a DIDComm message used for secure and decentralized communication in the Atala PRISM architecture.
     * The [Message] object includes information about the sender, recipient, message body, and other metadata.
     * The method sets default values for some properties and returns the created [Message] object.
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
            extraHeaders = emptyMap(),
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
     * Represents an update object for mediation keys.
     *
     * @property recipientDid The recipient DID.
     * @property action The action to be performed.
     * @constructor Creates an instance of [Update].
     */
    @Serializable
    data class Update
    @OptIn(ExperimentalSerializationApi::class)
    @JvmOverloads
    constructor(
        @SerialName(RECEPIENT_DID)
        var recipientDid: String,
        @EncodeDefault
        var action: String = ADD
    )

    /**
     * Represents a body object that contains `updates` for mediation keys.
     *
     * @property updates An array of [Update] objects representing the updates for mediation keys.
     */
    @Serializable
    data class Body @JvmOverloads constructor(var updates: Array<Update> = emptyArray()) {
        /**
         * Checks if this object is equal to another object.
         *
         * @param other The object to compare with.
         * @return true if the objects are equal, false otherwise.
         */
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as Body

            return updates.contentEquals(other.updates)
        }

        /**
         * Calculates the hash code for the object.
         *
         * @return The hash code value for the object.
         */
        override fun hashCode(): Int {
            return updates.contentHashCode()
        }

        /**
         * Converts the updates in this [Body] object to a [Map] with keys of type [String]
         * and values of type [Any?].
         *
         * @return A [Map] containing the updates as key-value pairs.
         */
        fun toMapStringAny(): Map<String, Any?> {
            return mapOf(Pair(UPDATES, updates))
        }
    }
}
