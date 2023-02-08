package io.iohk.atala.prism.domain.models

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.js.JsExport

@Serializable
@JsExport
data class Message(
    val id: String,
    val piuri: String,
    val from: DID? = null,
    val to: DID? = null,
    val fromPrior: String? = null,
    val body: String,
    val extraHeaders: Array<String> = arrayOf(),
    val createdTime: String = Clock.System.now().toString(),
    val expiresTimePlus: String = Clock.System.now().toString(),
    val attachments: Array<AttachmentDescriptor>,
    val thid: String? = null,
    val pthid: String? = null,
    val ack: Array<String>? = emptyArray(),
    val direction: Direction = Direction.RECEIVED
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Message

        if (id != other.id) return false
        if (piuri != other.piuri) return false
        if (from != other.from) return false
        if (to != other.to) return false
        if (fromPrior != other.fromPrior) return false
        if (body != other.body) return false
        if (!extraHeaders.contentEquals(other.extraHeaders)) return false
        if (createdTime != other.createdTime) return false
        if (expiresTimePlus != other.expiresTimePlus) return false
        if (!attachments.contentEquals(other.attachments)) return false
        if (thid != other.thid) return false
        if (pthid != other.pthid) return false
        if (!ack.contentEquals(other.ack)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + piuri.hashCode()
        result = 31 * result + (from?.hashCode() ?: 0)
        result = 31 * result + (to?.hashCode() ?: 0)
        result = 31 * result + (fromPrior?.hashCode() ?: 0)
        result = 31 * result + body.hashCode()
        result = 31 * result + extraHeaders.contentHashCode()
        result = 31 * result + createdTime.hashCode()
        result = 31 * result + expiresTimePlus.hashCode()
        result = 31 * result + attachments.contentHashCode()
        result = 31 * result + (thid?.hashCode() ?: 0)
        result = 31 * result + (pthid?.hashCode() ?: 0)
        result = 31 * result + ack.contentHashCode()
        return result
    }

    fun toJsonString(): String {
        return Json.encodeToString(this)
    }

    enum class Direction(val value: Int) {
        SENT(0),
        RECEIVED(1)
    }
}

fun getDirectionByValue(value: Int): Message.Direction {
    return when (value) {
        0 -> Message.Direction.SENT
        1 -> Message.Direction.RECEIVED
        else -> Message.Direction.SENT
    }
}
