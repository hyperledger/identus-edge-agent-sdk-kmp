package io.iohk.atala.prism.walletsdk.mercury.forward

import io.iohk.atala.prism.apollo.base64.base64UrlEncoded
import io.iohk.atala.prism.walletsdk.domain.models.AttachmentBase64
import io.iohk.atala.prism.walletsdk.domain.models.AttachmentDescriptor
import io.iohk.atala.prism.walletsdk.domain.models.DID
import io.iohk.atala.prism.walletsdk.domain.models.Message
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.*

class ForwardMessage @JvmOverloads constructor(
    val body: String,
    val from: DID,
    val to: DID,
    val encryptedMessage: String,
    val id: String = UUID.randomUUID().toString()
) {
    companion object {
        const val type = "https://didcomm.org/routing/2.0/forward"
    }

    fun makeMessage(): Message {
        val forwardBody = Json.encodeToString(ForwardBody(body)).base64UrlEncoded
        val attachmentData = AttachmentBase64(encryptedMessage)
        val attachment = AttachmentDescriptor(UUID.randomUUID().toString(), "application/json", attachmentData)
        val message = Message(
            id = id,
            piuri = type,
            from = from,
            to = to,
            body = forwardBody,
            attachments = arrayOf(attachment)
        )

        return message
    }
}

@Serializable
data class ForwardBody(val next: String)
