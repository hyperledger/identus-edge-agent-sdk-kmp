package io.iohk.atala.prism.walletsdk.domain.models

import kotlin.js.Promise

@JsExport
actual interface DIDResolver {
    actual val method: String
    fun resolve(didString: String): Promise<DIDDocument>
}
