package io.iohk.atala.prism.walletsdk.prismagent.protocols.outOfBand

import io.iohk.atala.prism.apollo.base64.base64UrlDecoded
import io.iohk.atala.prism.walletsdk.domain.models.CommonError
import io.iohk.atala.prism.walletsdk.prismagent.OOB
import io.ktor.http.*

class OutOfBandParser {

    @Throws(CommonError.InvalidURLError::class)
    fun parseMessage(url: Url): String {
        val urlBuilder = URLBuilder(url)

        urlBuilder.parameters[OOB]?.let { message ->
            return message.base64UrlDecoded
        } ?: throw CommonError.InvalidURLError(url = url.toString())
    }
}
