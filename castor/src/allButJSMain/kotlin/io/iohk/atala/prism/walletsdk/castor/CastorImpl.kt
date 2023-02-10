package io.iohk.atala.prism.walletsdk.castor

import io.iohk.atala.prism.walletsdk.castor.resolvers.LongFormPrismDIDResolver
import io.iohk.atala.prism.walletsdk.castor.resolvers.PeerDIDResolver
import io.iohk.atala.prism.walletsdk.domain.buildingBlocks.Apollo
import io.iohk.atala.prism.walletsdk.domain.buildingBlocks.Castor
import io.iohk.atala.prism.walletsdk.domain.models.CastorError
import io.iohk.atala.prism.walletsdk.domain.models.DID
import io.iohk.atala.prism.walletsdk.domain.models.DIDDocument
import io.iohk.atala.prism.walletsdk.domain.models.DIDResolver
import io.iohk.atala.prism.walletsdk.domain.models.KeyPair
import io.iohk.atala.prism.walletsdk.domain.models.PublicKey
import io.iohk.atala.prism.walletsdk.domain.models.Signature

actual class CastorImpl actual constructor(apollo: Apollo) : Castor {
    actual val apollo: Apollo
    actual var resolvers: Array<DIDResolver>

    init {
        this.apollo = apollo
        this.resolvers = arrayOf(
            PeerDIDResolver(),
            LongFormPrismDIDResolver(this.apollo)
        )
    }

    actual override fun parseDID(did: String): DID {
        return io.iohk.atala.prism.walletsdk.castor.shared.ParseDID(did)
    }

    actual override fun createPrismDID(
        masterPublicKey: PublicKey,
        services: Array<DIDDocument.Service>?
    ): DID {
        TODO("Not yet implemented")
    }

    actual override fun createPeerDID(
        keyPairs: Array<KeyPair>,
        services: Array<DIDDocument.Service>
    ): DID {
        return io.iohk.atala.prism.walletsdk.castor.shared.CreatePeerDID(
            keyPairs = keyPairs,
            services = services
        )
    }

    override suspend fun resolveDID(did: String): DIDDocument {
        val resolver = io.iohk.atala.prism.walletsdk.castor.shared.GetDIDResolver(did, resolvers)
        return resolver.resolve(did)
    }

    override suspend fun verifySignature(did: DID, challenge: ByteArray, signature: ByteArray): Boolean {
        val document = resolveDID(did.toString())
        val keyPairs: List<PublicKey> =
            io.iohk.atala.prism.walletsdk.castor.shared.GetKeyPairFromCoreProperties(document.coreProperties)

        if (keyPairs.isEmpty()) {
            throw CastorError.InvalidKeyError()
        }

        for (keyPair in keyPairs) {
            val verified = apollo.verifySignature(keyPair, challenge, Signature(signature))
            if (verified) {
                return true
            }
        }

        return false
    }
}
