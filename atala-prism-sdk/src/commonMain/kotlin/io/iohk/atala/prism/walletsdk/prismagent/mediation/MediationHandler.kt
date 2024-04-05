package io.iohk.atala.prism.walletsdk.prismagent.mediation

import io.iohk.atala.prism.walletsdk.domain.models.DID
import io.iohk.atala.prism.walletsdk.domain.models.Mediator
import io.iohk.atala.prism.walletsdk.domain.models.Message
import kotlinx.coroutines.flow.Flow

/**
 * An interface representing a mediator handler for handling mediator routing in the DIDComm protocol. The
 * MediatorHandler protocol defines methods and properties for booting registered mediators, achieving mediation,
 * updating key lists, picking up unread messages, and registering messages as read.
 */
interface MediationHandler {
    /**
     * The active mediator associated with the mediator handler
     */
    val mediator: Mediator?

    /**
     * The DID of the mediator associated with the mediator handler
     */
    val mediatorDID: DID

    /**
     * Boots the registered mediator associated with the mediator handler.
     *
     * @return The mediator that was booted.
     */
    suspend fun bootRegisteredMediator(): Mediator?

    /**
     * Achieves mediation with the mediatorDID with the specified host DID as a user.
     *
     * @param host The DID of the entity to mediate with.
     * @return The mediator associated with the achieved mediation.
     */
    fun achieveMediation(host: DID): Flow<Mediator>

    /**
     * Updates the key list with the specified DIDs.
     *
     * @param dids An array of DIDs to add to the key list.
     */
    suspend fun updateKeyListWithDIDs(dids: Array<DID>)

    /**
     * Picks up the specified number of unread messages.
     *
     * @param limit The maximum number of messages to pick up.
     * @return An array of pairs containing the message ID and the message itself.
     */
    fun pickupUnreadMessages(limit: Int): Flow<Array<Pair<String, Message>>>

    /**
     * Registers the specified message IDs as read.
     *
     * @param ids An array of message IDs to register as read.
     */
    suspend fun registerMessagesAsRead(ids: Array<String>)

    suspend fun listenUnreadMessages(serviceEndpointUri: String, onMessageCallback: OnMessageCallback)
}
