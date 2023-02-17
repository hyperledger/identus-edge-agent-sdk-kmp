package io.iohk.atala.prism.walletsdk.prismagent.helpers

import io.iohk.atala.prism.walletsdk.domain.models.HttpResponse
import io.iohk.atala.prism.walletsdk.prismagent.shared.KeyValue
import io.iohk.atala.prism.walletsdk.prismagent.shared.PrismShared
import io.ktor.client.HttpClient
import io.ktor.client.request.request
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpMethod
import io.ktor.http.Url
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.promise
import kotlin.js.Promise

@JsExport
open class ApiImpl(override var client: HttpClient) : Api {
    @OptIn(DelicateCoroutinesApi::class)
    override fun request(
        httpMethod: String,
        url: String,
        urlParameters: Array<KeyValue>,
        httpHeaders: Array<KeyValue>,
        body: Any?,
    ): Promise<HttpResponse> {
        val request = PrismShared.getRequestBuilder(
            httpMethod = HttpMethod(httpMethod),
            url = Url(url),
            urlParametersArray = urlParameters,
            httpHeadersArray = httpHeaders,
            body = body,
        )
        return GlobalScope.promise {
            val response = client.request(request)
            HttpResponse(
                status = response.status.value,
                jsonString = response.bodyAsText(),
            )
        }
    }
}
