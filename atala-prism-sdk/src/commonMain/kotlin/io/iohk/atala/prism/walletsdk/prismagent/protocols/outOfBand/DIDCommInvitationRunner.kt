package io.iohk.atala.prism.walletsdk.prismagent.protocols.outOfBand

import io.iohk.atala.prism.walletsdk.prismagent.PrismAgentError
import io.iohk.atala.prism.walletsdk.prismagent.protocols.ProtocolType
import io.ktor.http.Url
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

internal class DIDCommInvitationRunner(private val url: Url) {

    @Throws(PrismAgentError.UnknownInvitationTypeError::class)
    internal suspend fun run(): OutOfBandInvitation {
        val messageString = OutOfBandParser().parseMessage(url)
        val message: OutOfBandInvitation = Json.decodeFromString(messageString)
        if (message.type != ProtocolType.Didcomminvitation) {
            throw PrismAgentError.UnknownInvitationTypeError()
        }
        return message
    }
}
