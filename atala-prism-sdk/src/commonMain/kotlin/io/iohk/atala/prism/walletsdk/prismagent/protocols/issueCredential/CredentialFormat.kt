package io.iohk.atala.prism.walletsdk.prismagent.protocols.issueCredential

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * know Format:
 * https://github.com/hyperledger/aries-rfcs/tree/main/features/0453-issue-credential-v2#propose-attachment-registry
 * - dif/credential-manifest@v1.0
 * - aries/ld-proof-vc-detail@v1.0
 * - hlindy/cred-filter@v2.0
 */
@Serializable
data class CredentialFormat(
    @SerialName("attach_id")
    val attachId: String,
    val format: String
)
