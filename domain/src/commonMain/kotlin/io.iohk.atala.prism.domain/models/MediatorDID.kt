package io.iohk.atala.prism.domain.models

data class MediatorDID(
    val id: String,
    val mediatorDID: DID,
    val hostDID: DID,
    val routingDID: DID
)
