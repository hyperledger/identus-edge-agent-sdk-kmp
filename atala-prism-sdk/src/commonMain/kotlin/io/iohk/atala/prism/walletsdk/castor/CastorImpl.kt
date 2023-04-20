package io.iohk.atala.prism.walletsdk.castor

import io.iohk.atala.prism.walletsdk.castor.resolvers.LongFormPrismDIDResolver
import io.iohk.atala.prism.walletsdk.castor.resolvers.PeerDIDResolver
import io.iohk.atala.prism.walletsdk.castor.shared.CastorShared
import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Apollo
import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Castor
import io.iohk.atala.prism.walletsdk.domain.models.CastorError
import io.iohk.atala.prism.walletsdk.domain.models.DID
import io.iohk.atala.prism.walletsdk.domain.models.DIDDocument
import io.iohk.atala.prism.walletsdk.domain.models.DIDResolver
import io.iohk.atala.prism.walletsdk.domain.models.KeyPair
import io.iohk.atala.prism.walletsdk.domain.models.PublicKey
import io.iohk.atala.prism.walletsdk.domain.models.Signature
import kotlin.jvm.Throws

class CastorImpl constructor(apollo: Apollo) : Castor {
    val apollo: Apollo
    var resolvers: Array<DIDResolver>

    init {
        this.apollo = apollo
        this.resolvers = arrayOf(
            PeerDIDResolver(),
            LongFormPrismDIDResolver(this.apollo),
        )
    }

    override fun parseDID(did: String): DID {
        return CastorShared.parseDID(did)
    }

    override fun createPrismDID(
        masterPublicKey: PublicKey,
        services: Array<DIDDocument.Service>?
    ): DID {
        return CastorShared.createPrismDID(
            apollo = apollo,
            masterPublicKey = masterPublicKey,
            services = services,
        )
    }

    @Throws(CastorError.InvalidKeyError::class)
    override fun createPeerDID(
        keyPairs: Array<KeyPair>,
        services: Array<DIDDocument.Service>
    ): DID {
        return CastorShared.createPeerDID(
            keyPairs = keyPairs,
            services = services,
        )
    }

    @Throws(CastorError.NotPossibleToResolveDID::class)
    override suspend fun resolveDID(did: String): DIDDocument {
        val resolver = CastorShared.getDIDResolver(did, resolvers)
        return resolver.resolve(did)
    }

    @Throws(CastorError.InvalidKeyError::class)
    override suspend fun verifySignature(did: DID, challenge: ByteArray, signature: ByteArray): Boolean {
        val document = resolveDID(did.toString())
        val keyPairs: List<PublicKey> =
            CastorShared.getKeyPairFromCoreProperties(document.coreProperties)

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
