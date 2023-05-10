package io.iohk.atala.prism.walletsdk.domain.models

import io.iohk.atala.prism.walletsdk.prismagent.shared.KeyValue
import io.ktor.client.HttpClient as KtorClient

interface Api {
    var client: KtorClient
    suspend fun request(
        httpMethod: String,
        url: String,
        urlParameters: Array<KeyValue> = emptyArray(),
        httpHeaders: Array<KeyValue> = emptyArray(),
        body: Any?
    ): HttpResponse
}
