package io.iohk.atala.prism.walletsdk.domain.models

data class CredentialIssueOptions(
    val type: CredentialType,
    val linkSecret: String? = null
)
