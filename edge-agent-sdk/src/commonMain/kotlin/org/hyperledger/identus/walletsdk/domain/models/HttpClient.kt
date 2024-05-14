package org.hyperledger.identus.walletsdk.domain.models

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig

/**
 * Creates a new instance of HttpClient with the given configuration.
 *
 * @param config The configuration block for HttpClient. If not provided, default configuration will be used.
 * @return A new instance of HttpClient.
 */
expect fun httpClient(config: HttpClientConfig<*>.() -> Unit = {}): HttpClient
