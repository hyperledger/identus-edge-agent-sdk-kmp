package org.hyperledger.identus.walletsdk.domain.models

import kotlinx.serialization.Serializable

/**
 * A data class representing a conjunction of DID, keyPathIndex and alias.
 */
@Serializable
data class PrismDIDInfo(
    val did: DID,
    val keyPathIndex: Int? = 0,
    val alias: String? = null
)
