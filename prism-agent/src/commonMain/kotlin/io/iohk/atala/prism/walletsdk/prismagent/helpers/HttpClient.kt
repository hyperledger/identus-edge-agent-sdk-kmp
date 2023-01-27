package io.iohk.atala.prism.walletsdk.prismagent.helpers

import io.ktor.client.HttpClientConfig
import io.ktor.client.HttpClient as KtorClient

@Suppress("NO_ACTUAL_FOR_EXPECT")
internal expect fun HttpClient(config: HttpClientConfig<*>.() -> Unit = {}): KtorClient
