package io.iohk.atala.prism.domain.models

data class PrismDIDInfo(
    val did: DID,
    val keyPathIndex: Int? = 0,
    val alias: String? = null
)
