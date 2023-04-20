package io.iohk.atala.prism.walletsdk.prismagent.helpers

import io.iohk.atala.prism.walletsdk.domain.models.HttpResponse
import io.iohk.atala.prism.walletsdk.prismagent.shared.KeyValue
import io.ktor.client.HttpClient as KtorClient

interface Api {
    var client: KtorClient
    suspend fun request(
        httpMethod: String,
        url: String,
        urlParameters: Array<KeyValue>,
        httpHeaders: Array<KeyValue>,
        body: Any?,
    ): HttpResponse
}
