@file:Suppress("ktlint:standard:multiline-if-else")

package org.hyperledger.identus.walletsdk.edgeagent.protocols.proofOfPresentation

import org.hyperledger.identus.walletsdk.domain.models.CredentialType

sealed interface PresentationOptions {
    val type: CredentialType
    val name: String?
    val purpose: String?
}

data class JWTPresentationOptions(
    override val name: String = "Presentation",
    override val purpose: String = "Presentation definition",
    val jwt: Array<String> = arrayOf("ES256K"),
    val domain: String,
    val challenge: String
) : PresentationOptions {
    override val type: CredentialType = CredentialType.JWT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as JWTPresentationOptions

        if (name != other.name) return false
        if (purpose != other.purpose) return false
        if (domain != other.domain) return false
        if (challenge != other.challenge) return false
        return jwt.contentEquals(other.jwt)
    }

    override fun hashCode(): Int {
        var result = name?.hashCode() ?: 0
        result = 31 * result + purpose.hashCode()
        result = 31 * result + (jwt.contentHashCode())
        result = 31 * result + domain.hashCode()
        result = 31 * result + challenge.hashCode()
        return result
    }
}

data class SDJWTPresentationOptions(
    override val name: String? = "Presentation",
    override val purpose: String = "Presentation definition",
    val sdjwt: Array<String> = arrayOf("ES256K")
) : PresentationOptions {
    override val type: CredentialType = CredentialType.SDJWT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as SDJWTPresentationOptions

        if (name != other.name) return false
        if (purpose != other.purpose) return false
        return sdjwt.contentEquals(other.sdjwt)
    }

    override fun hashCode(): Int {
        var result = name?.hashCode() ?: 0
        result = 31 * result + purpose.hashCode()
        result = 31 * result + (sdjwt.contentHashCode())
        return result
    }
}

data class AnoncredsPresentationOptions(
    // TODO: This should be a nonce from the anoncred wrapper
    val nonce: String,
    override val name: String? = null,
    override val purpose: String? = null
) : PresentationOptions {
    override val type: CredentialType = CredentialType.ANONCREDS_PROOF_REQUEST
}
