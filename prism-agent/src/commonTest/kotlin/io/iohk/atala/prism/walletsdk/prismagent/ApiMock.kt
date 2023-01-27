package io.iohk.atala.prism.walletsdk.prismagent

import io.iohk.atala.prism.walletsdk.prismagent.helpers.Api
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.serialization.json.Json
import io.ktor.client.HttpClient as KtorClient

@OptIn(DelicateCoroutinesApi::class)
class ApiMock : Api {

    constructor(statusCode: HttpStatusCode, response: String) : super(
        KtorClient(
            engine = MockEngine { _ ->
                respond(
                    content = response,
                    status = statusCode,
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
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
    )
}
