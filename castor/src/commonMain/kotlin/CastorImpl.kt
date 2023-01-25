package io.iohk.atala.prism.castor

import io.iohk.atala.prism.domain.buildingBlocks.Castor
import io.iohk.atala.prism.domain.models.CastorError
import io.iohk.atala.prism.domain.models.DID
import io.iohk.atala.prism.domain.models.DIDDocument
import io.iohk.atala.prism.domain.models.DIDResolver
import io.iohk.atala.prism.domain.models.KeyPair
import io.iohk.atala.prism.domain.models.PublicKey

open class CastorImpl : Castor {
    var resolvers: Array<DIDResolver> = arrayOf(
        PeerDIDResolver()
    )

    override fun parseDID(did: String): DID {
        return DIDParser.parse(did)
    }

    override fun createPrismDID(masterPublicKey: PublicKey, services: Array<DIDDocument.Service>?): DID {
        TODO("Not yet implemented")
    }

    override fun createPeerDID(
        keyAgreementKeyPair: KeyPair,
        authenticationKeyPair: KeyPair,
        services: Array<DIDDocument.Service>
    ): DID {
        TODO("Not yet implemented")
    }

    @Throws(CastorError.NotPossibleToResolveDID::class)
    override suspend fun resolveDID(did: DID): DIDDocument {
        val resolver = resolvers.find { it.method == did.method } ?: throw CastorError.NotPossibleToResolveDID()
        return resolver.resolve(did)
    }

    override suspend fun verifySignature(did: DID, challenge: ByteArray, signature: ByteArray): Boolean {
        TODO("Not yet implemented")
    }

    override fun getEcnumbasis(did: DID, keyPair: KeyPair): String {
        TODO("Not yet implemented")
    }
}
