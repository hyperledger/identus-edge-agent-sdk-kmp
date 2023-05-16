package io.iohk.atala.prism.walletsdk.prismagent.protocols.outOfBand

import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Mercury
import io.iohk.atala.prism.walletsdk.domain.models.Message
import io.ktor.http.Url

class InvitationRunner(private val mercury: Mercury, private val url: Url) {
    suspend fun run(): Message {
        val messageString = OutOfBandParser().parseMessage(url)
        return mercury.unpackMessage(messageString)
    }
}
