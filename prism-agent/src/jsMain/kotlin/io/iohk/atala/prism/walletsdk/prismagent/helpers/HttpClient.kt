package io.iohk.atala.prism.walletsdk.prismagent.helpers

import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.js.Js
import io.ktor.client.HttpClient as KtorClient

internal actual fun HttpClient(config: HttpClientConfig<*>.() -> Unit) = KtorClient(Js) {
    config(this)
}
