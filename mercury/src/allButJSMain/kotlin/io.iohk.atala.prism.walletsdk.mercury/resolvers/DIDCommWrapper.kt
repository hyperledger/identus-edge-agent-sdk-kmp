package io.iohk.atala.prism.walletsdk.mercury.resolvers

import io.iohk.atala.prism.walletsdk.domain.buildingBlocks.Castor
import io.iohk.atala.prism.walletsdk.domain.buildingBlocks.Pluto
import io.iohk.atala.prism.walletsdk.domain.models.AttachmentBase64
import io.iohk.atala.prism.walletsdk.domain.models.AttachmentData
import io.iohk.atala.prism.walletsdk.domain.models.AttachmentDescriptor
import io.iohk.atala.prism.walletsdk.domain.models.AttachmentJsonData
import io.iohk.atala.prism.walletsdk.domain.models.AttachmentLinkData
import io.iohk.atala.prism.walletsdk.domain.models.DID
import io.iohk.atala.prism.walletsdk.domain.models.MercuryError
import io.iohk.atala.prism.walletsdk.domain.models.Message
import io.iohk.atala.prism.walletsdk.mercury.DIDCommProtocol
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import org.didcommx.didcomm.DIDComm
import org.didcommx.didcomm.common.Typ
import org.didcommx.didcomm.message.Attachment
import org.didcommx.didcomm.model.PackEncryptedParams
import org.didcommx.didcomm.model.UnpackParams

class DIDCommWrapper(castor: Castor, pluto: Pluto) : DIDCommProtocol {
    private val didDocResolver = DIDCommDIDResolver(castor)
    private val secretsResolver = DIDCommSecretsResolver(pluto)
    private val didComm = DIDComm(didDocResolver, secretsResolver)

    override fun packEncrypted(message: Message): String {
        val toString = message.to.toString()
        val didCommMsg = org.didcommx.didcomm.message.Message(
            id = message.id,
            body = mapOf(),
            typ = Typ.Plaintext,
            type = message.piuri,
            to = listOf(toString),
            from = message.from.toString(),
            fromPrior = null,
//            fromPrior = message.fromPrior,
            fromPriorJwt = null,
            attachments = parseAttachments(message.attachments),
            createdTime = message.createdTime.toLong(),
            expiresTime = null,
            thid = message.thid,
            pthid = message.pthid,
            ack = "",
            pleaseAck = null,
            customHeaders = mapOf()
        )

        val builder = PackEncryptedParams.builder(didCommMsg, toString)
        val params = builder.build()
        val result = didComm.packEncrypted(params)

        return result.packedMessage
    }

    private fun parseAttachments(attachments: Array<AttachmentDescriptor>): List<Attachment> {
        return attachments.fold(mutableListOf()) { acc, attachment ->
            acc.add(
                Attachment(
                    id = attachment.id,
                    byteCount = attachment.byteCount?.toLong(),
                    data = parseAttachmentData(attachment.data),
                    description = attachment.description,
                    filename = attachment.filename?.joinToString("/"),
                    format = attachment.format,
                    lastModTime = attachment.lastModTime?.toLong(),
                    mediaType = attachment.mediaType
                )
            )

            return acc
        }
    }

    @Throws(MercuryError.UnknownAttachmentDataError::class)
    private fun parseAttachmentData(data: AttachmentData): Attachment.Data {
        if (data is AttachmentBase64) {
            return Attachment.Data.Base64(data.base64)
        }

        if (data is AttachmentJsonData) {
            val json = Json.parseToJsonElement(data.data)
            return Attachment.Data.Json(json.jsonObject.toMap())
        }

        if (data is AttachmentLinkData) {
            return Attachment.Data.Links(data.links.toList(), data.hash)
        }

        throw MercuryError.UnknownAttachmentDataError()
    }

    override fun unpack(message: String): Message {
        val result = didComm.unpack(
            UnpackParams(
                packedMessage = message,
                didDocResolver = didDocResolver,
                secretResolver = secretsResolver,
                expectDecryptByAllKeys = false,
                unwrapReWrappingForward = false
            )
        )

        val domainMsg = Message(
            id = result.message.id,
            piuri = result.message.type,
            from = result.message.from?.let { from -> DID(from) },
            to = result.message.to?.let { to -> DID(to.first()) },
            fromPrior = result.message.fromPrior.toString(),
            body = result.message.body.toString(),
            thid = result.message.thid,
            pthid = result.message.pthid,
            ack = result.message.ack?.let { arrayOf(it) } ?: emptyArray(),
            createdTime = result.message.createdTime?.toString() ?: "",
//            expiresTimePlus = result.message.expiresTime?.toString()
//            extraHeaders = result.message.customHeaders
            attachments = parseAttachmentsToDomain(result.message.attachments)
        )

        return domainMsg
    }

    private fun parseAttachmentsToDomain(attachments: List<Attachment>?): Array<AttachmentDescriptor> {
        return (attachments ?: emptyList()).fold(arrayOf()) { acc, attachment ->
            try {
                if (attachment.id !is String || attachment.id.length === 0) throw MercuryError.MessageAttachmentWithoutIDError()

                val attachmentDescriptor = AttachmentDescriptor(
                    id = attachment.id,
                    data = parseAttachmentDataToDomain(attachment.data),
                    byteCount = attachment.byteCount?.toInt(),
                    description = attachment.description,
                    filename = attachment.filename?.split("/")?.toTypedArray(),
                    format = attachment.format,
                    lastModTime = attachment.lastModTime?.toString(),
                    mediaType = attachment.mediaType,
                )

                return acc.plus(attachmentDescriptor)
            } catch (e: Error) {
                return acc
            }
        }
    }

    @Throws(MercuryError.UnknownAttachmentDataError::class)
    private fun parseAttachmentDataToDomain(data: Attachment.Data): AttachmentData {
        val jsonObj = data.toJSONObject()

        val base64 = jsonObj["base64"]
        if (base64 is String) return AttachmentBase64(base64)

        val json = jsonObj["json"]
        if (json is String) return AttachmentJsonData(json)

        val links = jsonObj["links"]
        val hash = jsonObj["hash"]
        if (links is Array<*> && links.isArrayOf<String>() && hash is String) {
            return AttachmentLinkData(
                links as Array<String>,
                hash
            )
        }

        throw MercuryError.UnknownAttachmentDataError()
    }
}
