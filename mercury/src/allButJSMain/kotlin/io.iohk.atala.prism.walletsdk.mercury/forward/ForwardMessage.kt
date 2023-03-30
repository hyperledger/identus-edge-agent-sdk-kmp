package io.iohk.atala.prism.walletsdk.mercury.forward


import io.iohk.atala.prism.walletsdk.domain.models.AttachmentBase64
import io.iohk.atala.prism.walletsdk.domain.models.AttachmentDescriptor
import io.iohk.atala.prism.walletsdk.domain.models.DID
import io.iohk.atala.prism.walletsdk.domain.models.Message
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

class ForwardMessage(
    val body: String,
    val from: DID,
    val to: DID,
    val encryptedMessage: String,
    val id: String = UUID.randomUUID().toString()
) {
    val type = "https://didcomm.org/routing/2.0/forward"

    fun makeMessage(): Message {
//        val forwardBody = Json.encodeToString(ForwardBody(body)).base64UrlEncoded
        val forwardBody = Json.encodeToString(ForwardBody(body))
        val attachmentData = AttachmentBase64(forwardBody)
        val attachment = AttachmentDescriptor(UUID.randomUUID().toString(), "application/json", attachmentData)
        val message = Message(
            id = id,
            piuri = type,
            from = from,
            to = to,
            body = forwardBody
        )

        return message
    }
}

@kotlinx.serialization.Serializable
data class ForwardBody (val next: String)
