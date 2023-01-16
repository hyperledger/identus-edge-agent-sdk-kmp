package io.iohk.atala.prism.domain.models

interface DIDResolverDomain {
    val method: DIDMethod

    fun resolve(did: DID): DIDDocument
}
