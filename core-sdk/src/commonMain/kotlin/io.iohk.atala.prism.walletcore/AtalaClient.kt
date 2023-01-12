package io.iohk.atala.prism.walletcore

import io.ktor.client.HttpClient
import io.ktor.client.request.prepareRequest
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.HttpStatement
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.URLProtocol
import io.ktor.http.path

class AtalaClient(
    private val baseURL: String,
    private val client: HttpClient = httpClient()
) {
    // TODO("We can add all endpoint calls here")

    suspend fun prepareRequest(
        httpMethod: HttpMethod,
        path: String,
        urlParameters: Map<String, String> = mapOf(),
        httpHeaders: Map<String, String> = mapOf()
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
            url {
                method = httpMethod
                protocol = URLProtocol.HTTPS
                host = baseURL
                path(path)
                for (parameter in urlParameters) {
                    parameters.append(parameter.key, parameter.value)
                }
            }
        }
    }

    suspend fun request(
        httpMethod: HttpMethod,
        path: String,
        urlParameters: Map<String, String> = mapOf(),
        httpHeaders: Map<String, String> = mapOf()
    ): HttpResponse {
        return prepareRequest(httpMethod, path, urlParameters, httpHeaders).execute()
    }

    /* example
    suspend fun login(request: LoginRequest): LoginResponse {
        return
    }
     */
}

/*
fun AtalaClient.login() {

}

suspend fun x() {
    val client = AtalaClient("www.google.com")
    val request = client.prepareRequest(
        HttpMethod.Post,
        ""
    )
    request.execute()
}
*/
