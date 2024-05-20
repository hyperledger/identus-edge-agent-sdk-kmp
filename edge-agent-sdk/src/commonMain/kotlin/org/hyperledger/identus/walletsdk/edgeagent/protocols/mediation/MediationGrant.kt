package org.hyperledger.identus.walletsdk.edgeagent.protocols.mediation

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.hyperledger.identus.walletsdk.domain.models.Message
import org.hyperledger.identus.walletsdk.edgeagent.ROUTING_DID
import org.hyperledger.identus.walletsdk.edgeagent.protocols.ProtocolType
import java.util.UUID
import kotlin.jvm.Throws

/**
 * The `MediationProtocolError` class represents an error that can occur during mediation protocol processing.
 * It is derived from the [Throwable] class and is sealed, meaning that it cannot be directly instantiated.
 *
 * @see Throwable
 */
sealed class MediationProtocolError : Throwable() {
    /**
     * The `InvalidMediationGrantError` class represents an error that can occur when a provided message is not a valid mediation grant.
     *
     * @throws MediationProtocolError.InvalidMediationGrantError if the provided message is not a valid mediation grant.
     */
    class InvalidMediationGrantError : MediationProtocolError()
}

/**
 * The MediationGrant class represents a mediation grant in the Atala PRISM architecture.
 * A mediation grant is used for granting permission to mediate a specific request.
 *
 * @property id The unique identifier of the mediation grant.
 * @property type The type of the mediation grant.
 * @property body The body of the mediation grant containing the routing DID.
 * @constructor Creates a MediationGrant object with the specified ID and body.
 * @throws MediationProtocolError.InvalidMediationGrantError if the provided message is not a valid mediation grant.
 */
class MediationGrant {
    var id: String
    var type = ProtocolType.DidcommMediationGrant.value
    var body: Body

    /**
     * Represents a mediation grant in the Atala PRISM architecture.
     * A mediation grant is used for granting permission to mediate a specific request.
     *
     * @property id The unique identifier of the mediation grant.
     * @property type The type of the mediation grant.
     * @property body The body of the mediation grant containing the routing DID.
     *
     * @constructor Creates a MediationGrant object with the specified ID and body.
     *
     * @throws MediationProtocolError.InvalidMediationGrantError if the provided message is not a valid mediation grant.
     */
    constructor(
        id: String = UUID.randomUUID().toString(),
        body: Body
    ) {
        this.id = id
        this.body = body
    }

    /**
     * The `MediationGrant` class represents a mediation grant in the Atala PRISM architecture.
     * A mediation grant is used for granting permission to mediate a specific request.
     *
     * @property id The unique identifier of the mediation grant.
     * @property type The type of the mediation grant.
     * @property body The body of the mediation grant containing the routing DID.
     * @constructor Creates a MediationGrant object with the specified ID and body.
     * @throws MediationProtocolError.InvalidMediationGrantError if the provided message is not a valid mediation grant.
     */
    @Throws(MediationProtocolError.InvalidMediationGrantError::class)
    constructor(fromMessage: Message) {
        if (fromMessage.piuri != ProtocolType.DidcommMediationGrant.value) {
            throw MediationProtocolError.InvalidMediationGrantError()
        }
        this.id = fromMessage.id
        this.body = Json.decodeFromString(fromMessage.body)
    }

    /**
     * The [Body] class represents a body object that can be included in a [Message] object.
     * It is used for secure, decentralized communication in the Atala PRISM architecture.
     *
     * @see Message
     */
    @Serializable
    data class Body(@SerialName(ROUTING_DID)var routingDid: String)
}
