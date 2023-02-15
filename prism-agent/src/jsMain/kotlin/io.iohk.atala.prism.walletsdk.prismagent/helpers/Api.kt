package io.iohk.atala.prism.walletsdk.prismagent.helpers

import io.iohk.atala.prism.walletsdk.domain.models.HttpResponse
import io.iohk.atala.prism.walletsdk.prismagent.shared.KeyValue
import io.ktor.client.HttpClient
import kotlin.js.Promise

@JsExport
actual interface Api {
    actual var client: HttpClient
    fun request(
        httpMethod: String,
        url: String,
        urlParameters: Array<KeyValue>,
        httpHeaders: Array<KeyValue>,
        body: Any?,
    ): Promise<HttpResponse>
}
