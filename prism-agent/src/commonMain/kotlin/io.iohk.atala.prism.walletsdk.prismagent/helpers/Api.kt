package io.iohk.atala.prism.walletsdk.prismagent.helpers

import io.ktor.client.HttpClient as KtorClient

expect interface Api {
    var client: KtorClient
}
