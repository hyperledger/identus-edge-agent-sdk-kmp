package org.hyperledger.identus.walletsdk.domain.models

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNames
import org.hyperledger.identus.walletsdk.domain.buildingblocks.Mercury
import org.hyperledger.identus.walletsdk.pluto.PlutoRestoreTask
import java.util.UUID
import kotlin.jvm.JvmOverloads
import kotlin.time.Duration.Companion.days

/**
 * The [Message] data class represents a DIDComm message, which is used for secure, decentralized communication in the
 * Atala PRISM architecture. A [Message] object includes information about the sender, recipient, message body, and other metadata.
 * [Message] objects are typically exchanged between DID controllers using the [Mercury] building block.
 */
@Serializable
data class Message
@OptIn(ExperimentalSerializationApi::class)
@JvmOverloads
constructor(
    @EncodeDefault
    val id: String = UUID.randomUUID().toString(),
    val piuri: String,
    @EncodeDefault
    val from: DID? = null,
    @EncodeDefault
    val to: DID? = null,
    @EncodeDefault
    val fromPrior: String? = null,
    val body: String,
    @SerialName("extra_headers")
    @JsonNames("extra_headers", "extraHeaders")
    val extraHeaders: Map<String, String> = emptyMap(),
    @SerialName("created_time")
    @JsonNames("created_time", "createdTime")
    val createdTime: String = Clock.System.now().epochSeconds.toString(),
    @SerialName("expires_time_plus")
    @JsonNames("expires_time_plus", "expiresTime", "expiresTimePlus")
    val expiresTimePlus: String = Clock.System.now().plus(1.days).epochSeconds.toString(),
    val attachments: Array<AttachmentDescriptor> = arrayOf(),
    val thid: String? = null,
    val pthid: String? = null,
    val ack: Array<String>? = emptyArray(),
    val direction: Direction = Direction.RECEIVED
) {
    /**
     * The `equals` method compares this `Message` object with another object for equality.
     *
     * @param other The object to compare with this `Message` object.
     * @return `true` if the objects are equal, `false` otherwise.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || this::class != other::class) {
            return false
        }

        other as Message

        if (id != other.id) {
            return false
        }
        if (piuri != other.piuri) {
            return false
        }
        if (from != other.from) {
            return false
        }
        if (to != other.to) {
            return false
        }
        if (fromPrior != other.fromPrior) {
            return false
        }
        if (body != other.body) {
            return false
        }
        if (createdTime != other.createdTime) {
            return false
        }
        if (expiresTimePlus != other.expiresTimePlus) {
            return false
        }
        if (!attachments.contentEquals(other.attachments)) {
            return false
        }
        if (thid != other.thid) {
            return false
        }
        if (pthid != other.pthid) {
            return false
        }
        if (!ack.contentEquals(other.ack)) {
            return false
        }

        return true
    }

    /**
     * Calculates the hash code for the Message object.
     * The hash code is calculated based on the values of the object's id, piuri, from, to, fromPrior,
     * body, extraHeaders, createdTime, expiresTimePlus, attachments, thid, pthid, and ack properties.
     * If any of these properties is null, it is treated as 0 in the calculation.
     *
     * @return The hash code for the Message object.
     */
    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + piuri.hashCode()
        result = 31 * result + (from?.hashCode() ?: 0)
        result = 31 * result + (to?.hashCode() ?: 0)
        result = 31 * result + (fromPrior?.hashCode() ?: 0)
        result = 31 * result + body.hashCode()
        result = 31 * result + extraHeaders.hashCode()
        result = 31 * result + createdTime.hashCode()
        result = 31 * result + expiresTimePlus.hashCode()
        result = 31 * result + attachments.contentHashCode()
        result = 31 * result + (thid?.hashCode() ?: 0)
        result = 31 * result + (pthid?.hashCode() ?: 0)
        result = 31 * result + ack.contentHashCode()
        return result
    }

    /**
     * Converts the current object to a JSON string representation.
     *
     * @return The JSON string representation of the object.
     */
    fun toJsonString(): String {
        return Json.encodeToString(this)
    }

    /**
     * Enumeration representing the direction of a message.
     *
     * @param value The numeric value representing the direction.
     */
    @Serializable
    enum class Direction(val value: Int) {
        SENT(0),
        RECEIVED(1);

        companion object {
            /**
             * Converts an integer value to a `Direction`.
             *
             * @param value The integer value to convert.
             * @return The corresponding `Direction` based on the given value.
             */
            @JvmStatic
            fun fromValue(value: Int): Direction {
                return entries.first { it.value == value }
            }
        }
    }

    /**
     * Creates a backup message object using the provided properties.
     *
     * @return The backup message object.
     */
    fun toBackUpMessage(): PlutoRestoreTask.BackUpMessage {
        fun isValidTimestamp(timestamp: String): Boolean {
            val regex = Regex("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?Z$") // Note the escaped backslashes
            return regex.matches(timestamp)
        }

        var createdTimeTemp: String? = this.createdTime
        if (isValidTimestamp(this.createdTime)) {
            createdTimeTemp = Instant.parse(this.createdTime).epochSeconds.toString()
        }
        var expiresTimePlusTemp: String? = this.expiresTimePlus
        if (isValidTimestamp(this.expiresTimePlus)) {
            expiresTimePlusTemp = Instant.parse(this.expiresTimePlus).epochSeconds.toString()
        }

        return PlutoRestoreTask.BackUpMessage(
            id,
            piuri,
            from,
            to,
            fromPrior,
            body,
            extraHeaders,
            createdTimeTemp?.toLong(),
            expiresTimePlusTemp?.toLong(),
            attachments,
            thid,
            pthid,
            ack,
            direction
        )
    }

    companion object {
        /**
         * Checks if the given [AttachmentData] is of type [AttachmentData.AttachmentBase64].
         *
         * @param data The [AttachmentData] object to check.
         * @return `true` if the [AttachmentData] is of type [AttachmentData.AttachmentBase64], `false` otherwise.
         */
        @JvmStatic
        fun isBase64Attachment(data: AttachmentData): Boolean {
            return data is AttachmentData.AttachmentBase64
        }

        /**
         * Checks if the given [AttachmentData] is of type [AttachmentData.AttachmentJsonData].
         *
         * @param data The attachment data to check.
         * @return `true` if the attachment data is of type [AttachmentData.AttachmentJsonData], `false` otherwise.
         */
        @JvmStatic
        fun isJsonAttachment(data: AttachmentData): Boolean {
            return data is AttachmentData.AttachmentJsonData
        }
    }
}

/**
 * Retrieves the direction of a message based on the given value.
 *
 * @param value The value representing the direction of the message.
 *              0 indicates SENT direction.
 *              1 indicates RECEIVED direction.
 *              Any other value defaults to SENT direction.
 * @return The message direction, either SENT or RECEIVED.
 */
fun getDirectionByValue(value: Int): Message.Direction {
    return when (value) {
        0 -> Message.Direction.SENT
        1 -> Message.Direction.RECEIVED
        else -> Message.Direction.SENT
    }
}
