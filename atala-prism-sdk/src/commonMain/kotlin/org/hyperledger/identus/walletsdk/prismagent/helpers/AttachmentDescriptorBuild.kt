package org.hyperledger.identus.walletsdk.prismagent.helpers

import io.iohk.atala.prism.apollo.base64.base64UrlEncoded
import org.hyperledger.identus.walletsdk.domain.models.AttachmentBase64
import org.hyperledger.identus.walletsdk.domain.models.AttachmentDescriptor
import io.ktor.http.ContentType
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

/**
 * Builds an AttachmentDescriptor object with the specified parameters.
 *
 * @param id The unique identifier for the attachment. If not provided, a random UUID will be generated.
 * @param payload The payload data for the attachment.
 * @param mediaType The media type of the attachment. If not provided, the default media type will be used.
 * @return The built AttachmentDescriptor object.
 */
@JvmOverloads
inline fun <reified T : Serializable> AttachmentDescriptor.Companion.build(
    id: String = UUID.randomUUID().toString(),
    payload: T,
    mediaType: String? = ContentType.Application.Json.toString()
): AttachmentDescriptor {
    val encoded = Json.encodeToString(payload).base64UrlEncoded
    val attachment = AttachmentBase64(base64 = encoded)
    return AttachmentDescriptor(id, mediaType, attachment)
}
