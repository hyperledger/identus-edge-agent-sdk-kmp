package io.iohk.atala.prism.domain.models

interface DIDResolver {
    val method: String

    suspend fun resolve(did: DID): DIDDocument
}
