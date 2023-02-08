package io.iohk.atala.prism.walletsdk.prismagent.helpers

import io.iohk.atala.prism.apollo.base64.base64UrlEncoded
import io.iohk.atala.prism.apollo.uuid.UUID
import io.iohk.atala.prism.domain.models.AttachmentBase64
import io.iohk.atala.prism.domain.models.AttachmentDescriptor
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

inline fun <reified T : Serializable> AttachmentDescriptor.Companion.build(
    id: String = UUID.randomUUID4().toString(),
    payload: T,
    mediaType: String? = "application/json"
): AttachmentDescriptor {
    val encoded = Json.encodeToString(payload).base64UrlEncoded
    val attachment = AttachmentBase64(base64 = encoded)
    return AttachmentDescriptor(id, mediaType, attachment)
}
