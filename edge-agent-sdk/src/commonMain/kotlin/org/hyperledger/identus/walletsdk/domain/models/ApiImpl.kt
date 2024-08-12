package org.hyperledger.identus.walletsdk.domain.models

import io.ktor.client.HttpClient
import io.ktor.client.request.request
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpMethod
import io.ktor.http.Url
import org.hyperledger.identus.walletsdk.edgeagent.shared.PrismShared

/**
 * Implementation of API interface for http requests.
 */
open class ApiImpl(override var client: HttpClient) : Api {

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
    override suspend fun request(
        httpMethod: String,
        url: String,
        urlParameters: Array<KeyValue>,
        httpHeaders: Array<KeyValue>,
        body: Any?
    ): HttpResponse {
        val request = PrismShared.getRequestBuilder(
            httpMethod = HttpMethod(httpMethod),
            url = Url(url),
            urlParametersArray = urlParameters,
            httpHeadersArray = httpHeaders,
            body = body
        )
        try {
            val response = client.request(request)
            return HttpResponse(
                status = response.status.value,
                jsonString = response.bodyAsText()
            )
        } catch (ex: Exception) {
            return HttpResponse(
                status = 500,
                jsonString = """{"message": ${ex.localizedMessage}}"""
            )
        }
    }
}
