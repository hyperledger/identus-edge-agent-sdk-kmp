package io.iohk.atala.prism.walletsdk.domain.models

import kotlinx.serialization.Serializable

/**
 * Mediator is a data class that represents a mediator entity.
 * It contains the mediator's ID, mediator DID, host DID, and routing DID.
 *
 * @property id The ID of the mediator.
 * @property mediatorDID The DID of the mediator.
 * @property hostDID The DID of the host.
 * @property routingDID The DID used for routing messages.
 */
@Serializable
data class Mediator(
    val id: String,
    val mediatorDID: DID,
    val hostDID: DID,
    val routingDID: DID
)
