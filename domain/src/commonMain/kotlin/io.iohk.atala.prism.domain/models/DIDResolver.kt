package io.iohk.atala.prism.domain.models

interface DIDResolver {
    val method: String

    fun resolve(did: DID): DIDDocument
}
