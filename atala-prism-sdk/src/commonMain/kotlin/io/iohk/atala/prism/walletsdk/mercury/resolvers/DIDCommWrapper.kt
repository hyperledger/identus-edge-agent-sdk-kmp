package io.iohk.atala.prism.walletsdk.mercury.resolvers

import com.nimbusds.jose.shaded.json.JSONObject
import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Apollo
import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Castor
import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Pluto
import io.iohk.atala.prism.walletsdk.domain.models.AttachmentBase64
import io.iohk.atala.prism.walletsdk.domain.models.AttachmentData
import io.iohk.atala.prism.walletsdk.domain.models.AttachmentDescriptor
import io.iohk.atala.prism.walletsdk.domain.models.AttachmentJsonData
import io.iohk.atala.prism.walletsdk.domain.models.AttachmentLinkData
import io.iohk.atala.prism.walletsdk.domain.models.DID
import io.iohk.atala.prism.walletsdk.domain.models.MercuryError
import io.iohk.atala.prism.walletsdk.domain.models.Message
import io.iohk.atala.prism.walletsdk.mercury.DIDCommProtocol
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.double
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import org.didcommx.didcomm.DIDComm
import org.didcommx.didcomm.common.Typ
import org.didcommx.didcomm.message.Attachment
import org.didcommx.didcomm.model.PackEncryptedParams
import org.didcommx.didcomm.model.UnpackParams
import org.didcommx.didcomm.utils.fromJsonToMap
import java.time.Instant.now

class DIDCommWrapper(castor: Castor, pluto: Pluto, apollo: Apollo) : DIDCommProtocol {
    private val didDocResolver = DIDCommDIDResolver(castor)
    private val secretsResolver = DIDCommSecretsResolver(pluto, apollo)
    private val didComm = DIDComm(didDocResolver, secretsResolver)

    private fun jsonObjectToMap(element: JsonElement): Map<String, Any?> {
        var bodyMap = mutableMapOf<String, Any?>()
        if (element is JsonPrimitive) {
        } else {
            val keys = element.jsonObject.keys
            keys.forEach { key ->
                when (val value = element.jsonObject[key]) {
                    is JsonObject -> {
                        bodyMap[key] = jsonObjectToMap(value)
                    }

                    is JsonArray -> {
                        var array = mutableListOf<Any>()
                        value.forEach {
                            when (it) {
                                is JsonObject -> {
                                    array.add(jsonObjectToMap(it))
                                }

                                is JsonPrimitive -> {
                                    if (it.isString) {
                                        array.add(it.content)
                                    } else if (it.intOrNull != null) {
                                        array.add(it.int)
                                    } else if (it.doubleOrNull != null) {
                                        array.add(it.double)
                                    } else if (it.booleanOrNull != null) {
                                        array.add(it.boolean)
                                    } else {
                                        array.add(it)
                                    }
                                }

                                else -> {
                                    array.add(it)
                                }
                            }
                        }
                        bodyMap[key] = array
                    }

                    is JsonPrimitive -> {
                        if (value.isString) {
                            bodyMap[key] = value.content
                        } else if (value.intOrNull != null) {
                            bodyMap[key] = value.int
                        } else if (value.doubleOrNull != null) {
                            bodyMap[key] = value.double
                        } else if (value.booleanOrNull != null) {
                            bodyMap[key] = value.boolean
                        } else {
                            bodyMap[key] = value
                        }
                    }

                    is JsonNull -> {
                        bodyMap[key] = null
                    }

                    else -> {
                        bodyMap[key] = value
                    }
                }
            }
        }
        return bodyMap
    }

    override fun packEncrypted(message: Message): String {
        val toString = message.to.toString()

        val element = Json.parseToJsonElement(message.body)
        val map = jsonObjectToMap(element)

        val didCommMsg = org.didcommx.didcomm.message.Message(
            id = message.id,
            body = map,
            typ = Typ.Plaintext,
            type = message.piuri,
            to = listOf(toString),
            from = message.from.toString(),
            fromPrior = null,
            fromPriorJwt = message.fromPrior,
            attachments = parseAttachments(message.attachments),
            createdTime = if (message.createdTime == "") Clock.System.now().epochSeconds else Instant.parse(message.createdTime).epochSeconds,
            expiresTime = null,
            thid = message.thid,
            pthid = message.pthid,
            ack = null,
            pleaseAck = null,
            customHeaders = mapOf()
        )
        val builder = PackEncryptedParams.builder(didCommMsg, toString).forward(false).protectSenderId(false)
        didCommMsg.from?.let { builder.from(it) }
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
            return Attachment.Data.Json(fromJsonToMap(data.data))
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
            createdTime = result.message.createdTime?.toString() ?: now().toEpochMilli().toString(),
            // expiresTimePlus = result.message.expiresTime?.toString()
            // extraHeaders = result.message.customHeaders
            attachments = parseAttachmentsToDomain(result.message.attachments)
        )

        return domainMsg
    }

    @kotlin.jvm.Throws(MercuryError.MessageAttachmentWithoutIDError::class)
    private fun parseAttachmentsToDomain(attachments: List<Attachment>?): Array<AttachmentDescriptor> {
        return (attachments ?: emptyList()).fold(arrayOf()) { acc, attachment ->
            try {
                if (attachment.id.isEmpty()) {
                    throw MercuryError.MessageAttachmentWithoutIDError()
                }

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
        if (base64 is String) {
            return AttachmentBase64(base64)
        }

        val json = jsonObj["json"]
        if (json is JSONObject) {
            return AttachmentJsonData(JSONObject.toJSONString(json as Map<String, *>))
        }

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
