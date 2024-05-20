package org.hyperledger.identus.walletsdk.edgeagent.protocols.issueCredential

import kotlinx.serialization.Serializable
import org.hyperledger.identus.walletsdk.domain.models.Message
import org.hyperledger.identus.walletsdk.domain.models.UnknownError
import org.hyperledger.identus.walletsdk.edgeagent.connectionsmanager.DIDCommConnection

/**
 * A class that represents the issue credential protocol in the DIDCommv2 format.
 *
 * @property stage The current stage of the protocol.
 * @property propose The `ProposeCredential` object representing the propose message, or null if not applicable.
 * @property offer The `OfferCredential` object representing the offer message, or null if not applicable.
 * @property request The `RequestCredential` object representing the request message, or null if not applicable.
 * @property connector The `DIDCommConnection` object representing the connection used to send and receive messages.
 */
@Serializable
class IssueCredentialProtocol {

    var stage: Stage
    var propose: ProposeCredential? = null
    var offer: OfferCredential? = null
    var request: RequestCredential? = null
    val connector: DIDCommConnection

    /**
     * The IssueCredentialProtocol class represents a protocol for issuing credentials in the Atala PRISM architecture.
     * It handles different stages of the protocol and communicates with a DIDComm connection to exchange messages.
     *
     * @param stage The current stage of the protocol.
     * @param proposeMessage The propose message received in the protocol.
     * @param offerMessage The offer message received in the protocol.
     * @param requestMessage The request message received in the protocol.
     * @param connector The DIDComm connection to communicate with.
     */
    @JvmOverloads
    constructor(
        stage: Stage,
        proposeMessage: Message? = null,
        offerMessage: Message? = null,
        requestMessage: Message? = null,
        connector: DIDCommConnection
    ) {
        this.stage = stage
        this.connector = connector
        this.propose = proposeMessage?.let {
            try {
                ProposeCredential.fromMessage(it)
            } catch (e: Throwable) {
                null
            }
        }
        this.offer = offerMessage?.let {
            try {
                OfferCredential.fromMessage(it)
            } catch (e: Throwable) {
                null
            }
        }
        this.request = requestMessage?.let {
            try {
                RequestCredential.fromMessage(it)
            } catch (e: Throwable) {
                null
            }
        }
    }

    /**
     * Constructs an instance of [IssueCredentialProtocol] class.
     * This constructor initializes the object with the provided [message] and [connector].
     * It determines the stage of the credential issuance process based on the message type in [message],
     * and assigns the corresponding values to the [stage] and relevant property (propose, offer, or request).
     *
     * @param message The message object representing the received message.
     * @param connector The DIDCommConnection instance used for message exchange.
     * @throws [UnknownError.SomethingWentWrongError] if the message does not match any known message type.
     */
    @Throws(UnknownError.SomethingWentWrongError::class)
    constructor(message: Message, connector: DIDCommConnection) {
        this.connector = connector
        val proposed = try {
            ProposeCredential.fromMessage(message)
        } catch (e: Throwable) {
            null
        }
        val offered = try {
            OfferCredential.fromMessage(message)
        } catch (e: Throwable) {
            null
        }
        val requested = try {
            RequestCredential.fromMessage(message)
        } catch (e: Throwable) {
            null
        }

        when {
            proposed != null -> {
                this.stage = Stage.PROPOSE
                this.propose = proposed
            }

            offered != null -> {
                this.stage = Stage.OFFER
                this.offer = offered
            }

            requested != null -> {
                this.stage = Stage.REQUEST
                this.request = requested
            }

            else -> throw UnknownError.SomethingWentWrongError(
                message = "Invalid step"
            )
        }
    }

    /**
     * Proceeds to the next stage of the credential issuance process.
     *
     * If the current stage is PROPOSE:
     * - If `propose` is null, sets the stage to REFUSED and returns.
     *
     * If the current stage is OFFER:
     * - If `offer` is null, sets the stage to REFUSED and returns.
     *
     * Based on the current stage, performs the following actions:
     * - PROPOSE:
     *   - Creates an OfferCredential from the proposed credential using the method `makeOfferFromProposedCredential()`.
     *   - Sends the offer message over the connector's connection using `sendMessage()`.
     *   - Sets the `messageId` to the ID of the sent message.
     *
     * - OFFER:
     *   - Creates a RequestCredential from the offer credential using the method `makeRequestFromOfferCredential()`.
     *   - Sends the request message over the connector's connection using `sendMessage()`.
     *   - Sets the `messageId` to the ID of the sent message.
     *
     * Based on the value of `messageId`, performs the following actions:
     * - If `messageId` is null, returns.
     *
     * - Otherwise, awaits a response message with the specified `messageId` using `awaitMessageResponse()`.
     *   If no response message is received, returns.
     *
     * Based on the received response message, performs the following actions:
     * - If the response is an IssueCredential message, sets the stage to COMPLETED.
     * - If the response is an OfferCredential message, sets the stage to OFFER and assigns the received offer to `this.offer`.
     * - If the response is a RequestCredential message, sets the stage to REQUEST and assigns the received request to `this.request`.
     *
     * @throws Throwable If there is an error in processing the messages.
     */
    suspend fun nextStage() {
        if (this.stage == Stage.PROPOSE) {
            if (propose == null) {
                stage = Stage.REFUSED
                return
            }
        } else if (this.stage == Stage.OFFER) {
            if (offer == null) {
                stage = Stage.REFUSED
                return
            }
        }

        val messageId: String = when (this.stage) {
            Stage.PROPOSE -> {
                val message = OfferCredential.makeOfferFromProposedCredential(proposed = propose!!)
                connector.sendMessage(message.makeMessage())
                message.id
            }

            Stage.OFFER -> {
                val message = RequestCredential.makeRequestFromOfferCredential(offer = offer!!).makeMessage()
                connector.sendMessage(message)
                message.id
            }

            Stage.REQUEST -> null
            Stage.COMPLETED -> null
            Stage.REFUSED -> null
        } ?: return

        val response = connector.awaitMessageResponse(id = messageId) ?: return

        val issued = try {
            IssueCredential.fromMessage(response)
        } catch (e: Throwable) {
            null
        }
        val offered = try {
            OfferCredential.fromMessage(response)
        } catch (e: Throwable) {
            null
        }
        val requested = try {
            RequestCredential.fromMessage(response)
        } catch (e: Throwable) {
            null
        }

        when {
            offered != null -> {
                this.stage = Stage.OFFER
                this.offer = offered
            }

            issued != null -> {
                this.stage = Stage.COMPLETED
            }

            requested != null -> {
                this.stage = Stage.REQUEST
                this.request = requested
            }
        }
    }

    /**
     * Represents the different stages of the credential issuance process.
     * The stages are PROPOSE, OFFER, REQUEST, COMPLETED, and REFUSED.
     */
    enum class Stage {
        PROPOSE,
        OFFER,
        REQUEST,
        COMPLETED,
        REFUSED
    }
}
