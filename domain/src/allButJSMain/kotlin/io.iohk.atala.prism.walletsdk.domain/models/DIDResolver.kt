package io.iohk.atala.prism.walletsdk.domain.models

actual interface DIDResolver {
    actual val method: String
    suspend fun resolve(didString: String): DIDDocument
}
