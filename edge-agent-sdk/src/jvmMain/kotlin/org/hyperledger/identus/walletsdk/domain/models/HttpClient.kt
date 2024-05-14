package org.hyperledger.identus.walletsdk.domain.models

import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.okhttp.OkHttp

actual fun httpClient(config: HttpClientConfig<*>.() -> Unit) = io.ktor.client.HttpClient(OkHttp) {
    config(this)
}
