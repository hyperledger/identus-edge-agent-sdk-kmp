package io.iohk.atala.prism.walletsdk.prismagent.protocols.outOfBand

import io.iohk.atala.prism.apollo.base64.base64UrlDecoded
import io.iohk.atala.prism.walletsdk.domain.models.PrismAgentError
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import kotlin.jvm.Throws

class OutOfBandParser {

    @Throws(PrismAgentError.InvalidURLError::class)
    fun parseMessage(url: Url): String {
        val urlBuilder = URLBuilder(url)

        urlBuilder.parameters["_oob"]?.let { message ->
            return message.base64UrlDecoded
        } ?: throw PrismAgentError.InvalidURLError()
    }
}
