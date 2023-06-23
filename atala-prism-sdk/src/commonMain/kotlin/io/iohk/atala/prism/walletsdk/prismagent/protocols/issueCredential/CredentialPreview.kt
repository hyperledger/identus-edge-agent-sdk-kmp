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
    val attributes: Array<Attribute>
) {

    @Serializable
    data class Attribute(
        val name: String,
        val value: String,
        val mimeType: String?
    )

    public val type: String = ProtocolType.DidcommCredentialPreview.value

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as CredentialPreview

        if (!attributes.contentEquals(other.attributes)) return false
        if (type != other.type) return false

        return true
    }

    override fun hashCode(): Int {
        var result = attributes.contentHashCode()
        result = 31 * result + type.hashCode()
        return result
    }
}
