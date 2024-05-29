@file:Suppress("ktlint:standard:multiline-if-else")

package org.hyperledger.identus.walletsdk.edgeagent.protocols.proofOfPresentation

data class PresentationOptions(
    val name: String? = "Presentation",
    val purpose: String = "Presentation definition",
    val jwt: Array<String> = arrayOf("ES256K"),
    val domain: String,
    val challenge: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as PresentationOptions

        if (name != other.name) return false
        if (purpose != other.purpose) return false
        if (domain != other.domain) return false
        if (challenge != other.challenge) return false
        return jwt.contentEquals(other.jwt)
    }

    override fun hashCode(): Int {
        var result = name?.hashCode() ?: 0
        result = 31 * result + purpose.hashCode()
        result = 31 * result + (jwt.contentHashCode() ?: 0)
        result = 31 * result + domain.hashCode()
        result = 31 * result + challenge.hashCode()
        return result
    }
}
