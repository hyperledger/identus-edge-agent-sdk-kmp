package org.hyperledger.identus.walletsdk.domain.models

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encodeToString
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.json.jsonObject
import org.hyperledger.identus.apollo.base64.base64UrlDecoded
import org.hyperledger.identus.walletsdk.edgeagent.EdgeAgentError
import java.util.UUID
import kotlin.jvm.JvmOverloads

/**
 * The [AttachmentData] interface represents a generic attachment for a DIDComm [Message]. Any type that conforms to
 * [AttachmentData] can be used as an attachment.
 */
@Serializable
sealed class AttachmentData {

    /**
     * This method verifies the type of [AttachmentData] and returns the data as a json string.
     */
    fun getDataAsJsonString(): String {
        return when (this) {
            is AttachmentBase64 -> {
                this.base64.base64UrlDecoded
            }

            is AttachmentJsonData -> {
                this.data
            }

            else -> {
                throw EdgeAgentError.AttachmentTypeNotSupported()
            }
        }
    }

    /**
     * The [AttachmentHeader] data class represents the header for a DIDComm attachment.
     */
    @Serializable
    data class AttachmentHeader(
        val children: String
    ) : AttachmentData()

    /**
     * The [AttachmentJws] data class represents a DIDComm attachment containing a JWS (JSON Web Signature).
     */
    @Serializable
    data class AttachmentJws(
        val header: AttachmentHeader,
        val protected: String,
        val signature: String
    ) : AttachmentData()

    /**
     * The [AttachmentJwsData] data class represents a DIDComm attachment containing JWS data.
     */
    @Serializable
    data class AttachmentJwsData(
        val base64: String,
        val jws: AttachmentJws
    ) : AttachmentData()

    /**
     * The [AttachmentBase64] data class represents a DIDComm attachment containing base64-encoded data.
     */
    @Serializable
    data class AttachmentBase64(
        val base64: String
    ) : AttachmentData()

    /**
     * The [AttachmentLinkData] data class represents a DIDComm attachment containing a link to external data.
     */
    @Serializable
    data class AttachmentLinkData(
        val links: Array<String>,
        val hash: String
    ) : AttachmentData() {
        /**
         * Overrides the equals method of the [Any] class.
         *
         * Two [AttachmentLinkData] instances are considered equal if all of their properties have the same values.
         *
         * @param other the object to compare for equality
         * @return true if the objects are equal, false otherwise
         */
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as AttachmentLinkData

            if (!links.contentEquals(other.links)) return false
            if (hash != other.hash) return false

            return true
        }

        /**
         * Calculates the hash code for the [AttachmentLinkData] instance.
         *
         * The hash code is calculated by combining the hash codes of the `links` array and the `hash` property using the formula:
         * `result = 31 * result + hash.hashCode()`.
         *
         * @return the hash code value for the [AttachmentLinkData] instance
         */
        override fun hashCode(): Int {
            var result = links.contentHashCode()
            result = 31 * result + hash.hashCode()
            return result
        }
    }

    /**
     * The [AttachmentJsonData] data class represents a DIDComm attachment containing JSON data.
     */
    @Serializable
    data class AttachmentJsonData(
        val data: String
    ) : AttachmentData()
}

/**
 * The [AttachmentDescriptor] data class represents metadata for a DIDComm attachment.
 */
@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class AttachmentDescriptor
@JvmOverloads
constructor(
    val id: String = UUID.randomUUID().toString(),
    @SerialName("media_type")
    @JsonNames("media_type", "mediaType")
    val mediaType: String? = null,
    @Serializable(with = AttachmentDataSerializer::class)
    val data: AttachmentData,
    val filename: Array<String>? = null,
    val format: String? = null,
    @SerialName("lastmod_time")
    @JsonNames("lastmod_time", "lastModTime")
    val lastModTime: String? = null,
    @SerialName("byte_count")
    @JsonNames("byteCount", "byte_count")
    val byteCount: Int? = null,
    val description: String? = null
) {

    /**
     * Checks if this [AttachmentDescriptor] object is equal to the specified [other] object.
     *
     * Two [AttachmentDescriptor] objects are considered equal if they have the same values for the following properties:
     * - [id] (unique identifier)
     * - [mediaType] (media type of the attachment)
     * - [data] (attachment data)
     * - [filename] (array of filenames associated with the attachment)
     * - [format] (format of the attachment)
     * - [lastModTime] (last modification time of the attachment)
     * - [byteCount] (byte count of the attachment)
     * - [description] (description of the attachment)
     *
     * @param other The object to compare with this [AttachmentDescriptor] object.
     * @return true if the specified [other] object is equal to this [AttachmentDescriptor] object, false otherwise.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as AttachmentDescriptor

        if (id != other.id) return false
        if (mediaType != other.mediaType) return false
        if (data != other.data) return false
        if (!filename.contentEquals(other.filename)) return false
        if (format != other.format) return false
        if (lastModTime != other.lastModTime) return false
        if (byteCount != other.byteCount) return false
        if (description != other.description) return false

        return true
    }

    /**
     * Calculates the hash code for the AttachmentDescriptor object.
     *
     * @return The hash code value for the AttachmentDescriptor object.
     */
    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + (mediaType?.hashCode() ?: 0)
        result = 31 * result + data.hashCode()
        result = 31 * result + filename.contentHashCode()
        result = 31 * result + (format?.hashCode() ?: 0)
        result = 31 * result + (lastModTime?.hashCode() ?: 0)
        result = 31 * result + (byteCount ?: 0)
        result = 31 * result + (description?.hashCode() ?: 0)
        return result
    }
}

/**
 * The `AttachmentDataSerializer` is responsible for serializing and deserializing instances of `AttachmentData`.
 *
 * To serialize an `AttachmentData` object, use the `serialize` function. This function takes an `Encoder` and the
 * `AttachmentData` object to be serialized as parameters. It delegates the serialization to the appropriate serializer
 * based on the type of the `AttachmentData` object. The supported types are:
 * - `AttachmentData.AttachmentHeader`
 * - `AttachmentData.AttachmentJws`
 * - `AttachmentData.AttachmentJwsData`
 * - `AttachmentData.AttachmentBase64`
 * - `AttachmentData.AttachmentLinkData`
 * - `AttachmentData.AttachmentJsonData`
 *
 * To deserialize an `AttachmentData` object, use the `deserialize` function. This function takes a `Decoder` as a
 * parameter. It first checks the JSON object read from the decoder to determine the type of the `AttachmentData`
 * object. It then uses the appropriate deserializer to deserialize the JSON object into the corresponding
 * `AttachmentData` subclass. The supported JSON object structures are:
 * - `{ "children": ... }` for `AttachmentData.AttachmentHeader` objects
 * - `{ "protected": ..., "signature": ... }` for `AttachmentData.AttachmentJws` objects
 * - `{ "base64": ..., "jws": ... }` for `AttachmentData.AttachmentJwsData` objects
 * - `{ "base64": ... }` for `AttachmentData.AttachmentBase64` objects
 * - `{ "links": ..., "hash": ... }` for `AttachmentData.AttachmentLinkData` objects
 * - `{ "data": ... }` for `AttachmentData.AttachmentJsonData` objects
 *
 * If the JSON object does not match any of the supported structures, a `SerializationException` is thrown.
 */
object AttachmentDataSerializer : KSerializer<AttachmentData> {
    /**
     * This variable represents the descriptor for the `AttachmentData` class when it is serialized and deserialized.
     * It is an instance of the `SerialDescriptor` class.
     *
     * @see SerialDescriptor
     */
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("AttachmentData")

    /**
     * Serializes the given [AttachmentData] object using the provided [Encoder].
     *
     * @param encoder The encoder used for serialization.
     * @param value The [AttachmentData] object to be serialized.
     */
    override fun serialize(encoder: Encoder, value: AttachmentData) {
        when (value) {
            is AttachmentData.AttachmentHeader -> encoder.encodeSerializableValue(
                AttachmentData.AttachmentHeader.serializer(),
                value
            )

            is AttachmentData.AttachmentJws -> encoder.encodeSerializableValue(
                AttachmentData.AttachmentJws.serializer(),
                value
            )

            is AttachmentData.AttachmentJwsData -> encoder.encodeSerializableValue(
                AttachmentData.AttachmentJwsData.serializer(),
                value
            )

            is AttachmentData.AttachmentBase64 -> encoder.encodeSerializableValue(
                AttachmentData.AttachmentBase64.serializer(),
                value
            )

            is AttachmentData.AttachmentLinkData -> encoder.encodeSerializableValue(
                AttachmentData.AttachmentLinkData.serializer(),
                value
            )

            is AttachmentData.AttachmentJsonData -> encoder.encodeSerializableValue(
                AttachmentData.AttachmentJsonData.serializer(),
                value
            )
        }
    }

    /**
     * Deserializes a JSON representation of AttachmentData into an AttachmentData object.
     *
     * @param decoder The decoder used to decode the JSON representation.
     * @return The deserialized AttachmentData object.
     * @throws SerializationException if the JSON representation is of an unknown AttachmentData type.
     */
    override fun deserialize(decoder: Decoder): AttachmentData {
        val jsonDecoder = decoder as JsonDecoder
        val json = jsonDecoder.decodeJsonElement().jsonObject

        val jsonSerializable = Json { this.ignoreUnknownKeys = true }

        return when {
            json.containsKey("children") -> {
                jsonSerializable.decodeFromJsonElement(AttachmentData.AttachmentHeader.serializer(), json)
            }

            json.containsKey("protected") && json.containsKey("signature") -> {
                jsonSerializable.decodeFromJsonElement(AttachmentData.AttachmentJws.serializer(), json)
            }

            json.containsKey("base64") -> {
                if (json.containsKey("jws")) {
                    jsonSerializable.decodeFromJsonElement(AttachmentData.AttachmentJwsData.serializer(), json)
                } else {
                    jsonSerializable.decodeFromJsonElement(AttachmentData.AttachmentBase64.serializer(), json)
                }
            }

            json.containsKey("links") && json.containsKey("hash") -> {
                jsonSerializable.decodeFromJsonElement(AttachmentData.AttachmentLinkData.serializer(), json)
            }

            json.containsKey("data") -> {
                jsonSerializable.decodeFromJsonElement(AttachmentData.AttachmentJsonData.serializer(), json)
            }

            json.containsKey("json") -> {
                AttachmentData.AttachmentJsonData(data = Json.encodeToString(json["json"]!!.jsonObject))
            }

            else -> throw SerializationException("Unknown AttachmentData type")
        }
    }
}
