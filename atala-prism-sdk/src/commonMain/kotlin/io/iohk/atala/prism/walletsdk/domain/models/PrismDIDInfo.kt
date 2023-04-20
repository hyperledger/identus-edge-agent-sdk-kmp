package io.iohk.atala.prism.walletsdk.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class PrismDIDInfo(
    val did: DID,
    val keyPathIndex: Int? = 0,
    val alias: String? = null
)
