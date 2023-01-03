package io.iohk.atala.prism.pluto.models

data class DIDMediator(
    val did: DID,
    val routingDID: DID,
    val mediatorDID: DID
)
