package io.iohk.atala.prism.walletsdk.prismagent.protocols.connection

import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Pluto
import io.iohk.atala.prism.walletsdk.domain.models.DID
import io.iohk.atala.prism.walletsdk.domain.models.DIDPair
import io.iohk.atala.prism.walletsdk.prismagent.PrismAgentError
import io.iohk.atala.prism.walletsdk.prismagent.connectionsmanager.DIDCommConnection
import io.iohk.atala.prism.walletsdk.prismagent.protocols.outOfBand.OutOfBandInvitation

/**
 * Represents a runner for the DIDComm connection process.
 *
 * @property invitationMessage The out-of-band invitation message.
 * @property pluto The Pluto instance.
 * @property ownDID The own DID.
 * @property connection The DIDComm connection.
 */
internal class DIDCommConnectionRunner(
    private val invitationMessage: OutOfBandInvitation,
    private val pluto: Pluto,
    private val ownDID: DID,
    private val connection: DIDCommConnection
) {

    /**
     * Executes the DIDComm connection process and returns a pair of DIDs.
     *
     * @return A [DIDPair] representing the sender and receiver DIDs of the connection.
     * @throws [PrismAgentError.InvitationIsInvalidError] if the invitation is invalid and cannot be parsed.
     */
    @Throws(PrismAgentError.InvitationIsInvalidError::class)
    internal suspend fun run(): DIDPair {
        val request = ConnectionRequest(invitationMessage, ownDID)
        connection.sendMessage(request.makeMessage())
        return DIDPair(ownDID, request.to, null)
        // TODO: Check this with @Gonçalo
//        val message = pluto.getAllMessagesReceived().first().first {
//            it.id == request.id
//        }
//        if (message.piuri == ProtocolType.DidcommconnectionResponse.value) {
//            return DIDPair(ownDID, request.to, null)
//        } else {
//            throw PrismAgentError.InvitationIsInvalidError()
//        }
    }
}
