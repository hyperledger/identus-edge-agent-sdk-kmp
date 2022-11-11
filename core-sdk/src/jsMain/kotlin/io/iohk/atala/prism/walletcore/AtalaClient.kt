package io.iohk.atala.prism.walletcore

import io.ktor.client.*
import io.ktor.client.engine.js.*

internal actual fun httpClient(config: HttpClientConfig<*>.() -> Unit) = HttpClient(Js) {
    config(this)
}