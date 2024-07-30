package org.hyperledger.identus.walletsdk.edgeagent.shared

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.Url
import io.ktor.http.contentType
import io.ktor.http.path
import org.hyperledger.identus.walletsdk.domain.models.KeyValue

/**
 * Utility class for shared functionality used by Prism API.
 */
internal object PrismShared {
    /**
     * Maps an array of [KeyValue] objects to a [Map] with key-value pairs.
     *
     * @param array The array of [KeyValue] objects to be mapped.
     * @return A [Map] containing the key-value pairs from the array.
     */
    @JvmStatic
    fun mapFromKeyValueArray(array: Array<KeyValue>): Map<String, String> {
        val response = mutableMapOf<String, String>()
        array.forEach {
            response[it.key] = it.value
        }
        return response
    }

    /**
     * Constructs an HTTP request builder with the specified parameters.
     *
     * @param httpMethod the HTTP method to be used for the request (e.g., "GET", "POST", "PUT", "DELETE", etc.)
     * @param url the URL to send the request to
     * @param urlParametersArray the array of URL parameters to be included in the request (default is an empty array)
     * @param httpHeadersArray the array of HTTP headers to be included in the request (default is an empty array)
     * @param body the request body to be sent with the request (default is null)
     * @return the constructed HttpRequestBuilder object
     */
    @JvmStatic
    fun getRequestBuilder(
        httpMethod: HttpMethod,
        url: Url,
        urlParametersArray: Array<KeyValue>,
        httpHeadersArray: Array<KeyValue>,
        body: Any?
    ): HttpRequestBuilder {
        val urlParameters: Map<String, String> = mapFromKeyValueArray(urlParametersArray)
        val httpHeaders: Map<String, String> = mapFromKeyValueArray(httpHeadersArray)

        val builder = HttpRequestBuilder()
        for (header in httpHeaders) {
            builder.headers.append(header.key, header.value)
        }
        if (!builder.headers.contains(HttpHeaders.ContentType)) {
            builder.contentType(ContentType.Application.Json)
        }

        body?.let {
            builder.setBody(body)
        }

        builder.url {
            builder.method = httpMethod
            protocol = url.protocol
            host = url.host
            port = url.specifiedPort

            path(url.encodedPath)

            for (parameter in urlParameters) {
                parameters.append(parameter.key, parameter.value)
            }
        }
        return builder
    }
}
