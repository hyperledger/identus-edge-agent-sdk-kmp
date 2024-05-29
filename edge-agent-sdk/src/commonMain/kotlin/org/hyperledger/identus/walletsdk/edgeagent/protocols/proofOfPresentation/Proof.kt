package org.hyperledger.identus.walletsdk.edgeagent.protocols.proofOfPresentation

import kotlinx.serialization.Serializable

@Serializable
data class Proof(
    val type: String,
    val created: String,
    val proofPurpose: String,
    val verificationMethod: String,
    val jws: String? = null
) {
    enum class Purpose(val value: String) {
        AUTHENTICATION("authentication")
    }
}
