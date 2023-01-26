package io.iohk.atala.prism.walletsdk.prismagent

import io.iohk.atala.prism.domain.buildingBlocks.Castor
import io.iohk.atala.prism.domain.models.DID
import io.iohk.atala.prism.domain.models.DIDDocument
import io.iohk.atala.prism.domain.models.KeyPair
import io.iohk.atala.prism.domain.models.PublicKey

class CastorMock : Castor {
    var parseDIDReturn: DID? = null
    var createPrismDIDReturn: DID? = null
    var createPeerDIDReturn: DID? = null
    var resolveDIDReturn: DIDDocument? = null
    var verifySignatureReturn: Boolean = false

    override fun parseDID(did: String): DID {
        return parseDIDReturn ?: throw Exception("parseDID() not implemented in mock")
    }

    override fun createPrismDID(
        masterPublicKey: PublicKey,
        services: Array<DIDDocument.Service>?
    ): DID {
        return createPrismDIDReturn ?: throw Exception("createPrismDID() not implemented in mock")
    }

    override fun createPeerDID(
        keyPairs: Array<KeyPair>,
        services: Array<DIDDocument.Service>
    ): DID {
        return createPeerDIDReturn ?: throw Exception("createPeerDID() not implemented in mock")
    }

    override suspend fun resolveDID(did: String): DIDDocument {
        return resolveDIDReturn ?: throw Exception("resolveDID() not implemented in mock")
    }

    override suspend fun verifySignature(did: DID, challenge: ByteArray, signature: ByteArray): Boolean {
        return verifySignatureReturn
    }
}
