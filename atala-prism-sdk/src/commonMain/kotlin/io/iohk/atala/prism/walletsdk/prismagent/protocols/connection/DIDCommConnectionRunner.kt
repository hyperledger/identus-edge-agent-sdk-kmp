package io.iohk.atala.prism.walletsdk.prismagent.protocols.connection

import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Pluto
import io.iohk.atala.prism.walletsdk.domain.models.DID
import io.iohk.atala.prism.walletsdk.domain.models.DIDPair
import io.iohk.atala.prism.walletsdk.domain.models.PrismAgentError
import io.iohk.atala.prism.walletsdk.prismagent.connectionsmanager.DIDCommConnection
import io.iohk.atala.prism.walletsdk.prismagent.protocols.ProtocolType
import io.iohk.atala.prism.walletsdk.prismagent.protocols.outOfBand.OutOfBandInvitation
import kotlinx.coroutines.flow.first
import kotlin.jvm.Throws

class DIDCommConnectionRunner(
    private val invitationMessage: OutOfBandInvitation,
    private val pluto: Pluto,
    private val ownDID: DID,
    private val connection: DIDCommConnection
) {

    @Throws(PrismAgentError.InvitationIsInvalidError::class)
    suspend fun run(): DIDPair {
        val request = ConnectionRequest(invitationMessage, ownDID)
        connection.sendMessage(request.makeMessage())
        val message = pluto.getAllMessagesReceived().first().first {
            it.thid == request.id
        }
        if (message.piuri == ProtocolType.DidcommconnectionResponse.value) {
            return DIDPair(ownDID, request.to, null)
        } else {
            throw PrismAgentError.InvitationIsInvalidError()
        }
    }
}
