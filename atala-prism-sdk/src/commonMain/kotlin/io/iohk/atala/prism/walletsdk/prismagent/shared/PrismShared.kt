package io.iohk.atala.prism.walletsdk.prismagent.shared

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.Url
import io.ktor.http.contentType
import io.ktor.http.path

object PrismShared {
    @JvmStatic
    fun mapFromKeyValueArray(array: Array<KeyValue>): Map<String, String> {
        val response = mutableMapOf<String, String>()
        array.forEach {
            response[it.key] = it.value
        }
        return response
    }

    @JvmStatic
    fun getRequestBuilder(
        httpMethod: HttpMethod,
        url: Url,
        urlParametersArray: Array<KeyValue>,
        httpHeadersArray: Array<KeyValue>,
        body: Any?,
    ): HttpRequestBuilder {
        val urlParameters: Map<String, String> = mapFromKeyValueArray(urlParametersArray)
        val httpHeaders: Map<String, String> = mapFromKeyValueArray(httpHeadersArray)

        val builder = HttpRequestBuilder()
        for (header in httpHeaders) {
            if (
                httpMethod == HttpMethod.Get &&
                header.key == HttpHeaders.ContentType &&
                header.value.contains(ContentType.Application.Json.contentSubtype)
            ) {
                continue
            }
            builder.headers.append(header.key, header.value)
        }
        builder.contentType(ContentType.Application.Json)

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
