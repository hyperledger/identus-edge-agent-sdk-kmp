package org.hyperledger.identus.walletsdk.edgeagent.protocols.connection

import org.hyperledger.identus.walletsdk.domain.buildingblocks.Pluto
import org.hyperledger.identus.walletsdk.domain.models.DID
import org.hyperledger.identus.walletsdk.domain.models.DIDPair
import org.hyperledger.identus.walletsdk.edgeagent.EdgeAgentError
import org.hyperledger.identus.walletsdk.edgeagent.connectionsmanager.DIDCommConnection
import org.hyperledger.identus.walletsdk.edgeagent.protocols.outOfBand.OutOfBandInvitation

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
     * @throws [EdgeAgentError.InvitationIsInvalidError] if the invitation is invalid and cannot be parsed.
     */
    @Throws(EdgeAgentError.InvitationIsInvalidError::class)
    internal suspend fun run(): DIDPair {
        val request = ConnectionRequest(invitationMessage, ownDID)
        connection.sendMessage(request.makeMessage())
        return DIDPair(ownDID, request.to, null)
    }
}
