package io.iohk.atala.prism.walletsdk.prismagent.protocols.issueCredential

import kotlinx.serialization.Serializable

@Serializable
data class CredentialFormat(val attachId: String, val format: String)
