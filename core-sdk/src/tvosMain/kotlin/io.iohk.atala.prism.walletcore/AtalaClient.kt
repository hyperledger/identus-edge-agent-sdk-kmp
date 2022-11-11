package io.iohk.atala.prism.walletcore

import io.ktor.client.*
import io.ktor.client.engine.darwin.*

internal actual fun httpClient(config: HttpClientConfig<*>.() -> Unit) = HttpClient(Darwin) {
    config(this)
    engine {
        configureRequest {
            setAllowsCellularAccess(true)
        }
    }
}