package io.iohk.atala.prism.walletsdk.prismagent.protocols.issueCredential

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CredentialFormat(
    @SerialName("attach_id")
    val attachId: String,
    val format: String
)
