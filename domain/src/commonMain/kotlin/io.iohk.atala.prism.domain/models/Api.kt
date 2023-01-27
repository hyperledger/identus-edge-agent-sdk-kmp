package io.iohk.atala.prism.domain.models

import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.HttpStatement
import io.ktor.http.HttpMethod
import io.ktor.http.Url
import io.ktor.client.HttpClient as KtorClient

interface Api {
    val client: KtorClient
    suspend fun prepareRequest(
        httpMethod: HttpMethod,
        url: Url,
        urlParameters: Map<String, String> = mapOf(),
        httpHeaders: Map<String, String> = mapOf(),
        body: Any? = null
    ): HttpStatement

    suspend fun request(
        httpMethod: HttpMethod,
        url: Url,
        urlParameters: Map<String, String> = mapOf(),
        httpHeaders: Map<String, String> = mapOf(),
        body: Any? = null
    ): HttpResponse
}
