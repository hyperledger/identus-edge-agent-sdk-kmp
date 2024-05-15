package io.iohk.atala.prism.walletsdk.prismagent.protocols.outOfBand

import io.iohk.atala.prism.walletsdk.prismagent.PrismAgentError
import io.iohk.atala.prism.walletsdk.prismagent.protocols.ProtocolType
import io.ktor.http.Url
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/**
 * This class represents a runner for the DIDComm Invitation protocol. It is responsible for running the invitation
 * process and parsing the invitation message.
 *
 * @property url The URL of the invitation message.
 */
internal class DIDCommInvitationRunner(private val url: Url) {

    /**
     * Runs the DIDComm Invitation protocol.
     *
     * @return The parsed OutOfBandInvitation message.
     * @throws PrismAgentError.UnknownInvitationTypeError If the type of the invitation is unknown or unsupported.
     */
    @Throws(PrismAgentError.UnknownInvitationTypeError::class)
    internal suspend fun run(): OutOfBandInvitation {
        val messageString = OutOfBandParser().parseMessage(url)
        val message: OutOfBandInvitation = Json.decodeFromString(messageString)
        if (message.type != ProtocolType.Didcomminvitation) {
            throw PrismAgentError.UnknownInvitationTypeError(message.type.toString())
        }
        return message
    }
}
