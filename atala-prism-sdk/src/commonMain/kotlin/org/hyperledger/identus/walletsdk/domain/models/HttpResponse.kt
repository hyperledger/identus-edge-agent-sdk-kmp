package org.hyperledger.identus.walletsdk.domain.models

import kotlinx.serialization.Serializable

/**
 * Represents an HTTP response.
 *
 * @param status The status code of the response.
 * @param jsonString The JSON string representing the response body.
 */
@Serializable
data class HttpResponse(val status: Int, val jsonString: JsonString)
