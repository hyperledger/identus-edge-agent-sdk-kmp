package io.iohk.atala.prism.domain.models

data class PrismDIDInfo(
    val did: DID,
    val keyPairIndex: Int,
    val alias: String? = null
)
