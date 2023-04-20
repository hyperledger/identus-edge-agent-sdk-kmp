package io.iohk.atala.prism.walletsdk.domain.models

interface DIDResolver {
    val method: String
    suspend fun resolve(didString: String): DIDDocument
}
