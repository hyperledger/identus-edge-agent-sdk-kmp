package org.hyperledger.identus.walletsdk.edgeagent.protocols.issueCredential

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.hyperledger.identus.walletsdk.edgeagent.protocols.ProtocolType

/**
 * https://github.com/hyperledger/aries-rfcs/tree/main/features/0453-issue-credential-v2#preview-credential
 */
@Serializable
data class CredentialPreview
@OptIn(ExperimentalSerializationApi::class)
@JvmOverloads
constructor(
    @SerialName("schema_id")
    @EncodeDefault
    val schemaId: String? = null,
    val body: Body
) {
    val type: String = ProtocolType.DidcommCredentialPreview.value

    /**
     * Initializes a new instance of [CredentialPreview].
     *
     * @param schemaId The ID of the schema associated with the credential. Defaults to `null` if not provided.
     * @param attributes An array of attributes to include in the credential preview.
     */
    constructor(schemaId: String? = null, attributes: Array<Attribute>) : this(schemaId, Body(attributes))

    /**
     * Compares this [CredentialPreview] object to the specified [other] object for equality.
     *
     * @param other The object to compare for equality.
     * @return `true` if the objects are equal, `false` otherwise.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as CredentialPreview

        return type == other.type
    }

    /**
     * Computes the hash code value for this object.
     *
     * @return The hash code value for this object.
     */
    override fun hashCode(): Int {
        return type.hashCode()
    }

    /**
     * Represents a body object.
     *
     * @property attributes The array of attributes.
     *
     * @see Attribute
     */
    @Serializable
    data class Body(
        val attributes: Array<Attribute>
    ) {
        /**
         * Checks if this `Body` object is equal to another object.
         *
         * @param other The object to compare to this `Body` object.
         * @return `true` if the objects are equal, `false` otherwise.
         */
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as Body

            return attributes.contentEquals(other.attributes)
        }

        /**
         * Calculates the hash code for this `Body` object.
         *
         * The hash code is calculated based on the `attributes` property of the `Body` object.
         *
         * @return The hash code value for this object.
         *
         * @see Body
         * @see Attribute
         */
        override fun hashCode(): Int {
            return attributes.contentHashCode()
        }
    }

    /**
     * Represents an attribute in a credential preview.
     *
     * @property name The name of the attribute.
     * @property value The value of the attribute.
     * @property mediaType The media type of the attribute, if applicable.
     */
    @Serializable
    data class Attribute(
        val name: String,
        val value: String,
        @SerialName("media_type")
        val mediaType: String?
    )
}
