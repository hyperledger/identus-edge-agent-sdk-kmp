package org.hyperledger.identus.walletsdk.mercury.resolvers

import com.nimbusds.jose.shaded.json.JSONObject
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
import org.hyperledger.identus.walletsdk.domain.buildingblocks.Apollo
import org.hyperledger.identus.walletsdk.domain.buildingblocks.Castor
import org.hyperledger.identus.walletsdk.domain.buildingblocks.Pluto
import org.hyperledger.identus.walletsdk.domain.models.AttachmentData
import org.hyperledger.identus.walletsdk.domain.models.AttachmentData.AttachmentBase64
import org.hyperledger.identus.walletsdk.domain.models.AttachmentData.AttachmentJsonData
import org.hyperledger.identus.walletsdk.domain.models.AttachmentData.AttachmentLinkData
import org.hyperledger.identus.walletsdk.domain.models.AttachmentDescriptor
import org.hyperledger.identus.walletsdk.domain.models.DID
import org.hyperledger.identus.walletsdk.domain.models.MercuryError
import org.hyperledger.identus.walletsdk.domain.models.Message
import org.hyperledger.identus.walletsdk.logger.LogComponent
import org.hyperledger.identus.walletsdk.logger.LogLevel
import org.hyperledger.identus.walletsdk.logger.LoggerImpl
import org.hyperledger.identus.walletsdk.logger.Metadata
import org.hyperledger.identus.walletsdk.mercury.ATTACHMENT_SEPARATOR
import org.hyperledger.identus.walletsdk.mercury.BASE64
import org.hyperledger.identus.walletsdk.mercury.DIDCommProtocol
import org.hyperledger.identus.walletsdk.mercury.HASH
import org.hyperledger.identus.walletsdk.mercury.JSON
import org.hyperledger.identus.walletsdk.mercury.LINKS
import java.time.Instant.now

/**
 * Wrapper class for the DIDComm functionality.
 *
 * @param castor The instance of Castor used for DID resolution.
 * @param pluto The instance of Pluto used for secrets resolution.
 * @param apollo The instance of Apollo used for secrets resolution.
 */
class DIDCommWrapper(castor: Castor, pluto: Pluto, apollo: Apollo) : DIDCommProtocol {
    private val didDocResolver = DIDCommDIDResolver(castor)
    private val secretsResolver = DIDCommSecretsResolver(pluto, apollo)
    private val didComm = DIDComm(didDocResolver, secretsResolver)
    private val logger = LoggerImpl(LogComponent.MERCURY)

    /**
     * Converts a JSON element to a map.
     *
     * @param element The JSON element to convert.
     * @return The resulting map.
     */
    private fun jsonObjectToMap(element: JsonElement): Map<String, Any?> {
        val bodyMap = mutableMapOf<String, Any?>()
        return if (element is JsonPrimitive) {
            bodyMap
        } else {
            val keys = element.jsonObject.keys
            keys.forEach { key ->
                when (val value = element.jsonObject[key]) {
                    is JsonObject -> {
                        bodyMap[key] = jsonObjectToMap(value)
                    }

                    is JsonArray -> {
                        val array = mutableListOf<Any>()
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
            bodyMap
        }
    }

    private fun String.canBeConvertedToLong(): Boolean {
        return try {
            this.toLong()
            true
        } catch (e: NumberFormatException) {
            false
        }
    }

    /**
     * Packs a [Message] object into an encrypted string message.
     *
     * @param message The [Message] object to be packed.
     * @return The packed message as a string.
     */
    override fun packEncrypted(message: Message): String {
        val toString = message.to.toString()

        val element: JsonElement = if (message.body.isBlank() || message.body.isEmpty()) {
            Json.parseToJsonElement("{}")
        } else {
            Json.parseToJsonElement(message.body)
        }
        val map: Map<String, Any?> = jsonObjectToMap(element)

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
            createdTime = if (message.createdTime == "") {
                Clock.System.now().epochSeconds
            } else if (message.createdTime.canBeConvertedToLong()) {
                message.createdTime.toLong()
            } else {
                Instant.parse(
                    message.createdTime
                ).epochSeconds
            },
            expiresTime = null,
            thid = message.thid,
            pthid = message.pthid,
            ack = null,
            pleaseAck = null,
            customHeaders = message.extraHeaders
        )
        val builder =
            PackEncryptedParams.builder(didCommMsg, toString).forward(false).protectSenderId(false)
        didCommMsg.from?.let { builder.from(it) }
        val params = builder.build()
        logger.debug(
            message = "Packing message ${message.piuri}",
            metadata = arrayOf(
                Metadata.MaskedMetadataByLevel(
                    key = "Sender",
                    value = message.from.toString(),
                    LogLevel.DEBUG
                ),
                Metadata.MaskedMetadataByLevel(
                    key = "Receiver",
                    value = message.to.toString(),
                    LogLevel.DEBUG
                )
            )
        )
        val result = didComm.packEncrypted(params)
        return result.packedMessage
    }

    /**
     * Parses an array of AttachmentDescriptors and converts them into a list of Attachments.
     *
     * @param attachments The array of AttachmentDescriptors to be parsed.
     * @return The list of parsed Attachments.
     */
    private fun parseAttachments(attachments: Array<AttachmentDescriptor>): List<Attachment> {
        return attachments.fold(mutableListOf()) { acc, attachment ->
            acc.add(
                Attachment(
                    id = attachment.id,
                    byteCount = attachment.byteCount?.toLong(),
                    data = parseAttachmentData(attachment.data),
                    description = attachment.description,
                    filename = attachment.filename?.joinToString(ATTACHMENT_SEPARATOR),
                    format = attachment.format,
                    lastModTime = attachment.lastModTime?.toLong(),
                    mediaType = attachment.mediaType
                )
            )

            return acc
        }
    }

    /**
     * Parse the given [AttachmentData] and return an instance of [Attachment.Data].
     *
     * @throws MercuryError.UnknownAttachmentDataError if an unknown [AttachmentData] type is found
     * @param data The [AttachmentData] to parse
     * @return The parsed [Attachment.Data]
     */
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

    /**
     * Unpacks a packed message into a domain-specific [Message] object.
     *
     * @param message The packed message to unpack.
     * @return The unpacked [Message] object.
     */
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
            // extraHeaders = result.message.customHeaders,
            attachments = parseAttachmentsToDomain(result.message.attachments)
        )

        return domainMsg
    }

    /**
     * Parses a list of attachments into an array of AttachmentDescriptors.
     *
     * @param attachments The list of attachments to be parsed. Can be null.
     * @return An array of parsed AttachmentDescriptors.
     * @throws MercuryError.MessageAttachmentWithoutIDError if a message attachment is found without an "id".
     */
    @Throws(MercuryError.MessageAttachmentWithoutIDError::class)
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
                    filename = attachment.filename?.split(ATTACHMENT_SEPARATOR)?.toTypedArray(),
                    format = attachment.format,
                    lastModTime = attachment.lastModTime?.toString(),
                    mediaType = attachment.mediaType
                )

                return acc.plus(attachmentDescriptor)
            } catch (e: Error) {
                return acc
            }
        }
    }

    /**
     * Parses the given [Attachment.Data] and converts it into an instance of [AttachmentData].
     * If the [Attachment.Data] represents base64-encoded data, it creates an instance of [AttachmentBase64].
     * If the [Attachment.Data] represents JSON data, it creates an instance of [AttachmentJsonData].
     * If the [Attachment.Data] represents a link to external data, it creates an instance of [AttachmentLinkData].
     *
     * @throws MercuryError.UnknownAttachmentDataError if an unknown [Attachment.Data] type is found
     * @param data The [Attachment.Data] to parse
     * @return The parsed [AttachmentData]
     */
    @Throws(MercuryError.UnknownAttachmentDataError::class)
    private fun parseAttachmentDataToDomain(data: Attachment.Data): AttachmentData {
        val jsonObj = data.toJSONObject()

        val base64 = jsonObj[BASE64]
        if (base64 is String) {
            return AttachmentBase64(base64)
        }

        val json = jsonObj[JSON]
        if (json is JSONObject) {
            @Suppress("UNCHECKED_CAST") // JSONObject can be always cast as Map<String, *>
            return AttachmentJsonData(JSONObject.toJSONString(json as Map<String, *>))
        }

        val links = jsonObj[LINKS]
        val hash = jsonObj[HASH]
        if (links is Array<*> && links.isArrayOf<String>() && hash is String) {
            @Suppress("UNCHECKED_CAST") // checks are applied in the if condition
            return AttachmentLinkData(links as Array<String>, hash)
        }

        throw MercuryError.UnknownAttachmentDataError()
    }
}
