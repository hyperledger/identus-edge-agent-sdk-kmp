package org.hyperledger.identus.walletsdk.edgeagent.protocols.mediation

import org.hyperledger.identus.walletsdk.domain.models.DID
import org.hyperledger.identus.walletsdk.domain.models.Message
import org.hyperledger.identus.walletsdk.edgeagent.EMPTY_BODY
import org.hyperledger.identus.walletsdk.edgeagent.protocols.ProtocolType
import java.util.UUID

/**
 * The `MediationRequest` class represents a mediation request in the application.
 *
 * @property id The unique identifier of the mediation request.
 * @property type The type of the mediation request. Default value is [ProtocolType.DidcommMediationRequest].
 * @property from The source DID (Decentralized Identifier) of the mediation request.
 * @property to The target DID (Decentralized Identifier) of the mediation request.
 * @constructor Creates a `MediationRequest`.
 * @param id The unique identifier of the mediation request.
 * @param type The type of the mediation request. Default value is [ProtocolType.DidcommMediationRequest].
 * @param from The source DID (Decentralized Identifier) of the mediation request.
 * @param to The target DID (Decentralized Identifier) of the mediation request.
 */
final class MediationRequest @JvmOverloads constructor(
    val id: String,
    val type: String = ProtocolType.DidcommMediationRequest.value,
    val from: DID,
    val to: DID
) {
    /**
     * The `MediationRequest` class represents a request for mediation between two entities.
     * It contains information about the sender (`from`) and the recipient (`to`) of the mediation request.
     *
     * @property id The unique identifier of the mediation request.
     * @property from The DID of the entity sending the request.
     * @property to The DID of the entity receiving the request.
     *
     * @constructor Creates a new `MediationRequest` object with the specified `from` and `to` DIDs.
     * The `id` is automatically generated using a random UUID.
     */
    constructor(
        from: DID,
        to: DID
    ) : this(
        id = UUID.randomUUID().toString(),
        from = from,
        to = to
    )

    /**
     * Creates a new [Message] object with default values for some properties and returns it.
     *
     * @return The newly created [Message] object.
     */
    fun makeMessage(): Message {
        return Message(
            id = id,
            piuri = type,
            from = from,
            to = to,
            fromPrior = null,
            body = EMPTY_BODY,
            extraHeaders = mapOf(Pair("return_route", "all")),
            attachments = emptyArray(),
            thid = null,
            pthid = null,
            ack = emptyArray(),
            direction = Message.Direction.SENT
        )
    }

    /**
     * Checks if this [MediationRequest] object is equal to another object.
     *
     * Two [MediationRequest] objects are considered equal if their properties match.
     *
     * @param other The object to compare equality with.
     * @return true if this [MediationRequest] object is equal to the [other] object, false otherwise.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as MediationRequest

        if (id != other.id) return false
        if (type != other.type) return false
        if (from != other.from) return false
        if (to != other.to) return false

        return true
    }

    /**
     * Generates the hash code for the `MediationRequest` object.
     *
     * @return The hash code value for the `MediationRequest` object.
     */
    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + from.hashCode()
        result = 31 * result + to.hashCode()
        return result
    }
}
