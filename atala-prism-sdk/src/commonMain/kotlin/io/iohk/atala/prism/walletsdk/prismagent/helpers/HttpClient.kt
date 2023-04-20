package io.iohk.atala.prism.walletsdk.prismagent.helpers

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig

internal expect fun HttpClient(config: HttpClientConfig<*>.() -> Unit = {}): HttpClient
