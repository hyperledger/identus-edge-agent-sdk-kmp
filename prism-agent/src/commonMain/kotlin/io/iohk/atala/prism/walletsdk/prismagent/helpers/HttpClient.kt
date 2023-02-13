package io.iohk.atala.prism.walletsdk.prismagent.helpers

import io.ktor.client.HttpClientConfig
import io.ktor.client.HttpClient as KtorClient

internal expect fun HttpClient(config: HttpClientConfig<*>.() -> Unit = {}): KtorClient
