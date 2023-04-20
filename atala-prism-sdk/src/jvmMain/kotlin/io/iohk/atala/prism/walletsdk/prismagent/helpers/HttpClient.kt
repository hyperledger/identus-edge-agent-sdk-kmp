package io.iohk.atala.prism.walletsdk.prismagent.helpers

import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.okhttp.OkHttp

internal actual fun HttpClient(config: HttpClientConfig<*>.() -> Unit) = io.ktor.client.HttpClient(OkHttp) {
    config(this)
}
