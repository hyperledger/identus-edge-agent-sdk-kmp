@file:Suppress("ktlint:standard:import-ordering")

package org.hyperledger.identus.walletsdk.edgeagent.protocols.outOfBand

import io.ktor.http.URLBuilder
import io.ktor.http.Url
import org.hyperledger.identus.apollo.base64.base64UrlDecoded
import org.hyperledger.identus.walletsdk.domain.models.CommonError
import org.hyperledger.identus.walletsdk.edgeagent.OOB

/**
 * The OutOfBandParser class is responsible for parsing out-of-band messages.
 */
class OutOfBandParser {

    /**
     * Parses the message from the given URL.
     *
     * @param url The URL object representing the message.
     * @return The parsed message.
     * @throws CommonError.InvalidURLError If the URL is invalid.
     */
    @Throws(CommonError.InvalidURLError::class)
    fun parseMessage(url: Url): String {
        val urlBuilder = URLBuilder(url)

        urlBuilder.parameters[OOB]?.let { message ->
            return message.base64UrlDecoded
        } ?: throw CommonError.InvalidURLError(url = url.toString())
    }
}
