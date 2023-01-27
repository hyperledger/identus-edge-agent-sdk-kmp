package io.iohk.atala.prism.walletsdk.prismagent.helpers

import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.HttpClient as KtorClient

internal actual fun HttpClient(config: HttpClientConfig<*>.() -> Unit) = KtorClient(OkHttp) {
    config(this)
}
