package org.hyperledger.identus.walletsdk.domain.models

/**
 * Represents the options for issuing credentials.
 *
 * @property type The type of verifiable credential to issue.
 * @property linkSecret The optional link secret used for linking credentials to a specific issuer.
 */
data class CredentialIssueOptions(
    val type: CredentialType,
    val linkSecret: String? = null
)
