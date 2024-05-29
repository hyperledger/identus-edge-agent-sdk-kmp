package org.hyperledger.identus.walletsdk.sampleapp.ui.messages

import org.hyperledger.identus.walletsdk.domain.models.AttachmentDescriptor

data class UiMessage(
    val id: String,
    val piuri: String,
    val from: String,
    val to: String,
    val status: String? = null,
    val attachments: Array<AttachmentDescriptor>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as UiMessage

        if (id != other.id) return false
        if (piuri != other.piuri) return false
        if (from != other.from) return false
        if (to != other.to) return false
        if (status != other.status) return false
        return attachments.contentEquals(other.attachments)
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + piuri.hashCode()
        result = 31 * result + from.hashCode()
        result = 31 * result + to.hashCode()
        result = 31 * result + (status?.hashCode() ?: 0)
        result = 31 * result + attachments.contentHashCode()
        return result
    }
}
