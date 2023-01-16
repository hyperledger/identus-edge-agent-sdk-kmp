package io.iohk.atala.prism.domain.models

data class MediatorDID(
    val did: DID,
    val routingDID: DID,
    val mediatorDID: DID
)
