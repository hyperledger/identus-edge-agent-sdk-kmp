package io.iohk.atala.prism.walletsdk.prismagent.helpers

import io.iohk.atala.prism.domain.models.Api
import io.ktor.client.request.prepareRequest
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.HttpStatement
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.Url
import io.ktor.http.contentType
import io.ktor.http.path
import kotlinx.coroutines.DelicateCoroutinesApi
import io.ktor.client.HttpClient as KtorClient

@DelicateCoroutinesApi
open class ApiImpl(override val client: KtorClient) : Api {

    override suspend fun prepareRequest(
        httpMethod: HttpMethod,
        url: Url,
        urlParameters: Map<String, String>,
        httpHeaders: Map<String, String>,
        body: Any?
    ): HttpStatement {

        return client.prepareRequest {
            for (header in httpHeaders) {
                if (
                    httpMethod == HttpMethod.Get &&
                    header.key == HttpHeaders.ContentType &&
                    header.value.contains(ContentType.Application.Json.contentSubtype)
                ) {
                    continue
                }
                headers.append(header.key, header.value)
            }

            contentType(ContentType.Application.Json)

            body?.let {
                setBody(body)
            }

            url {
                method = httpMethod
                protocol = url.protocol
                host = url.host
                port = url.specifiedPort

                path(url.encodedPath)

                for (parameter in urlParameters) {
                    parameters.append(parameter.key, parameter.value)
                }
            }
        }
    }

    override suspend fun request(
        httpMethod: HttpMethod,
        url: Url,
        urlParameters: Map<String, String>,
        httpHeaders: Map<String, String>,
        body: Any?
    ): HttpResponse {
        return prepareRequest(httpMethod, url, urlParameters, httpHeaders, body).execute()
    }
}
