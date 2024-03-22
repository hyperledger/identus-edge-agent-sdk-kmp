package org.hyperledger.identus.walletsdk.mercury

import org.hyperledger.identus.walletsdk.domain.buildingblocks.Castor
import org.hyperledger.identus.walletsdk.domain.models.DID
import org.hyperledger.identus.walletsdk.domain.models.DIDDocument
import org.hyperledger.identus.walletsdk.domain.models.DIDDocumentCoreProperty
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.KeyPair
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.PublicKey
import kotlin.jvm.Throws

class CastorMock : Castor {
    var parseDIDReturn: DID? = null
    var createPrismDIDReturn: DID? = null
    var createPeerDIDReturn: DID? = DID(
        "did",
        "prism",
        "b6c0c33d701ac1b9a262a14454d1bbde3d127d697a76950963c5fd930605:Cj8KPRI7CgdtYXN0ZXIwEAFKLgoJc2VmsxEiECSTjyV7sUfCr_ArpN9rvCwR9fRMAhcsr_S7ZRiJk4p5k"
    )
    var resolveDIDReturn: DIDDocument? = null
    var verifySignatureReturn: Boolean = false

    @Throws(Exception::class)
    override fun parseDID(did: String): DID {
        return parseDIDReturn ?: throw Exception("parseDID() not implemented in mock")
    }

    @Throws(Exception::class)
    override fun createPrismDID(
        masterPublicKey: PublicKey,
        services: Array<DIDDocument.Service>?
    ): DID {
        return createPrismDIDReturn ?: throw Exception("createPrismDID() not implemented in mock")
    }

    @Throws(Exception::class)
    override fun createPeerDID(
        keyPairs: Array<KeyPair>,
        services: Array<DIDDocument.Service>
    ): DID {
        return createPeerDIDReturn ?: throw Exception("createPeerDID() not implemented in mock")
    }

    @Throws(Exception::class)
    override suspend fun resolveDID(did: String): DIDDocument {
        return resolveDIDReturn ?: throw Exception("resolveDID() not implemented in mock")
    }

    override suspend fun verifySignature(did: DID, challenge: ByteArray, signature: ByteArray): Boolean {
        return verifySignatureReturn
    }

    override fun getPublicKeysFromCoreProperties(coreProperties: Array<DIDDocumentCoreProperty>): List<PublicKey> {
        TODO("Not yet implemented")
    }
}
