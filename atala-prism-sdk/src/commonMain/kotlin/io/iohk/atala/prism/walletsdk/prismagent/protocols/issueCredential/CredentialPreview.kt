package io.iohk.atala.prism.walletsdk.prismagent.protocols.issueCredential

import io.iohk.atala.prism.walletsdk.prismagent.protocols.ProtocolType
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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

    constructor(schemaId: String? = null, attributes: Array<Attribute>) : this(schemaId, Body(attributes))

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as CredentialPreview

        return type == other.type
    }

    override fun hashCode(): Int {
        return type.hashCode()
    }

    @Serializable
    data class Body(
        val attributes: Array<Attribute>
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as Body

            return attributes.contentEquals(other.attributes)
        }

        override fun hashCode(): Int {
            return attributes.contentHashCode()
        }
    }

    @Serializable
    data class Attribute(
        val name: String,
        val value: String,
        @SerialName("media_type")
        val mediaType: String?
    )
}
