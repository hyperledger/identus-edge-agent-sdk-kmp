package org.hyperledger.identus.walletsdk.edgeagent.protocols.outOfBand

import io.ktor.http.Url
import kotlinx.serialization.json.Json
import org.hyperledger.identus.walletsdk.edgeagent.EdgeAgentError
import org.hyperledger.identus.walletsdk.edgeagent.protocols.ProtocolType

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
     * @throws EdgeAgentError.UnknownInvitationTypeError If the type of the invitation is unknown or unsupported.
     */
    @Throws(EdgeAgentError.UnknownInvitationTypeError::class)
    internal fun run(): OutOfBandInvitation {
        val messageString = OutOfBandParser().parseMessage(url)
        val message: OutOfBandInvitation = Json.decodeFromString(messageString)
        if (message.type != ProtocolType.Didcomminvitation) {
            throw EdgeAgentError.UnknownInvitationTypeError(message.type.toString())
        }
        return message
    }
}
