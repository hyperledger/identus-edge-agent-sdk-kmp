package org.hyperledger.identus.walletsdk.domain.models

import io.ktor.client.HttpClient as KtorClient

/**
 * Interface that defines an API request
 */
interface Api {
    var client: KtorClient

    /**
     * Makes an HTTP request using the specified HTTP method, URL, URL parameters, HTTP headers, and request body.
     *
     * @param httpMethod the HTTP method to be used for the request (e.g., "GET", "POST", "PUT", "DELETE", etc.)
     * @param url the URL to send the request to
     * @param urlParameters the array of URL parameters to be included in the request (default is an empty array)
     * @param httpHeaders the array of HTTP headers to be included in the request (default is an empty array)
     * @param body the request body to be sent with the request (default is null)
     * @return the HttpResponse object representing the response received from the server
     */
    suspend fun request(
        httpMethod: String,
        url: String,
        urlParameters: Array<KeyValue> = emptyArray(),
        httpHeaders: Array<KeyValue> = emptyArray(),
        body: Any?
    ): HttpResponse
}
