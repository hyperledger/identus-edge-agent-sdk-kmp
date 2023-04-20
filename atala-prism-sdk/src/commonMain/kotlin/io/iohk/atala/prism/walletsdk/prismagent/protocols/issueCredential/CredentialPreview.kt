package io.iohk.atala.prism.walletsdk.prismagent.protocols.issueCredential

import io.iohk.atala.prism.walletsdk.prismagent.protocols.ProtocolType
import kotlinx.serialization.Serializable

@Serializable
data class CredentialPreview(val attributes: Array<Attribute>) {

    @Serializable
    data class Attribute(val name: String, val value: String, val mimeType: String?)

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
