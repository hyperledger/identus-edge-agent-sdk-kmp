package io.iohk.atala.prism.domain.models

interface DIDResolverDomain {
    val method: String

    fun resolve(did: DID): DIDDocument
}
