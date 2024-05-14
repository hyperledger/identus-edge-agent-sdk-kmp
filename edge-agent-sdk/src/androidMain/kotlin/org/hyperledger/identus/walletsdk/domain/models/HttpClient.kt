package org.hyperledger.identus.walletsdk.domain.models

import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.okhttp.OkHttp

/**
 * Creates an HTTP client with the specified configuration.
 *
 * @param config The configuration block for the HTTP client.
 * @return The created HTTP client.
 */
actual fun httpClient(config: HttpClientConfig<*>.() -> Unit) = io.ktor.client.HttpClient(OkHttp) {
    config(this)
}
