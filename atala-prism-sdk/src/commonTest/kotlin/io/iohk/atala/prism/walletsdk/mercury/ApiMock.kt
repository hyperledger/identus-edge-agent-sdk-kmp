package io.iohk.atala.prism.walletsdk.mercury

import io.iohk.atala.prism.walletsdk.domain.models.Api
import io.iohk.atala.prism.walletsdk.domain.models.HttpResponse
import io.iohk.atala.prism.walletsdk.prismagent.shared.KeyValue
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import io.ktor.client.HttpClient as KtorClient

class ApiMock(
    val statusCode: HttpStatusCode,
    val response: String,
    override var client: KtorClient = KtorClient(
        engine = MockEngine { _ ->
            respond(
                content = response,
                status = statusCode,
                headers = headersOf(HttpHeaders.ContentType, APPLICATION_JSON)
            )
        }
    ) {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                    isLenient = true
                }
            )
        }
    }
) : Api {

    override suspend fun request(
        httpMethod: String,
        url: String,
        urlParameters: Array<KeyValue>,
        httpHeaders: Array<KeyValue>,
        body: Any?
    ): HttpResponse {
        return HttpResponse(statusCode.value, response)
    }
}
