@file:Suppress("ktlint:standard:multiline-if-else")

package io.iohk.atala.prism.walletsdk.prismagent.protocols.proofOfPresentation

data class PresentationOptions(
    val name: String? = "Presentation",
    val purpose: String = "Presentation definition",
    val challenge: String? = null,
    val domain: String? = null,
    val jwtAlg: Array<String>? = null,
    val jwtVcAlg: Array<String>? = null,
    val jwtVpAlg: Array<String>? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as PresentationOptions

        if (name != other.name) return false
        if (purpose != other.purpose) return false
        if (challenge != other.challenge) return false
        if (domain != other.domain) return false
        if (jwtAlg != null) {
            if (other.jwtAlg == null) return false
            if (!jwtAlg.contentEquals(other.jwtAlg)) return false
        } else if (other.jwtAlg != null) return false
        if (jwtVcAlg != null) {
            if (other.jwtVcAlg == null) return false
            if (!jwtVcAlg.contentEquals(other.jwtVcAlg)) return false
        } else if (other.jwtVcAlg != null) return false
        if (jwtVpAlg != null) {
            if (other.jwtVpAlg == null) return false
            if (!jwtVpAlg.contentEquals(other.jwtVpAlg)) return false
        } else if (other.jwtVpAlg != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name?.hashCode() ?: 0
        result = 31 * result + purpose.hashCode()
        result = 31 * result + (challenge?.hashCode() ?: 0)
        result = 31 * result + (domain?.hashCode() ?: 0)
        result = 31 * result + (jwtAlg?.contentHashCode() ?: 0)
        result = 31 * result + (jwtVcAlg?.contentHashCode() ?: 0)
        result = 31 * result + (jwtVpAlg?.contentHashCode() ?: 0)
        return result
    }
}
