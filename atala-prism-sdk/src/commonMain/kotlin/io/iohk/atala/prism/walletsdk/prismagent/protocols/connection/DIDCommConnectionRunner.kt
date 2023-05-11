package io.iohk.atala.prism.walletsdk.prismagent.protocols.connection

import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Pluto
import io.iohk.atala.prism.walletsdk.domain.models.DID
import io.iohk.atala.prism.walletsdk.domain.models.DIDPair
import io.iohk.atala.prism.walletsdk.domain.models.PrismAgentError
import io.iohk.atala.prism.walletsdk.prismagent.connectionsmanager.DIDCommConnection
import io.iohk.atala.prism.walletsdk.prismagent.protocols.outOfBand.OutOfBandInvitation
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
