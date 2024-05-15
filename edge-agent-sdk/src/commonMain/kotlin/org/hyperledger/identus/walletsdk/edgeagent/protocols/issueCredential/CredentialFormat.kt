package org.hyperledger.identus.walletsdk.edgeagent.protocols.issueCredential

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.hyperledger.identus.walletsdk.edgeagent.ATTACH_ID

/**
 * know Format:
 * https://github.com/hyperledger/aries-rfcs/tree/main/features/0453-issue-credential-v2#propose-attachment-registry
 * - dif/credential-manifest@v1.0
 * - aries/ld-proof-vc-detail@v1.0
 * - hlindy/cred-filter@v2.0
 */
@Serializable
data class CredentialFormat(
    @SerialName(ATTACH_ID)
    val attachId: String,
    val format: String
)
