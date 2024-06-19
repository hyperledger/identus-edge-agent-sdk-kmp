package org.hyperledger.identus.walletsdk.domain.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import java.util.UUID

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
@OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
data class Mediator(
    val id: String = UUID.randomUUID().toString(),
    @SerialName("mediator_did")
    @JsonNames("mediator_did", "mediatorDID")
    val mediatorDID: DID,
    @SerialName("holder_did")
    @JsonNames("host_did", "hostDID", "holder_did")
    val hostDID: DID,
    @SerialName("routing_did")
    @JsonNames("routing_did", "routingDID")
    val routingDID: DID
)
