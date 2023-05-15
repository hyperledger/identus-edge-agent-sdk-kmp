package io.iohk.atala.prism.walletsdk.prismagent.protocols.outOfBand

import io.iohk.atala.prism.walletsdk.domain.models.PrismAgentError
import io.iohk.atala.prism.walletsdk.prismagent.protocols.ProtocolType
import io.ktor.http.Url
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class DIDCommInvitationRunner(private val url: Url) {

    @Throws(PrismAgentError.UnknownInvitationTypeError::class)
    suspend fun run(): OutOfBandInvitation {
        val messageString = OutOfBandParser().parseMessage(url)
        val message: OutOfBandInvitation = Json.decodeFromString(messageString)
        if (message.type != ProtocolType.Didcomminvitation) {
            throw PrismAgentError.UnknownInvitationTypeError()
        }
        return message
    }
}
