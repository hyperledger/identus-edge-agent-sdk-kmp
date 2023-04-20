package io.iohk.atala.prism.walletsdk.prismagent

import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Castor
import io.iohk.atala.prism.walletsdk.domain.models.DID
import io.iohk.atala.prism.walletsdk.domain.models.DIDDocument
import io.iohk.atala.prism.walletsdk.domain.models.KeyPair
import io.iohk.atala.prism.walletsdk.domain.models.PublicKey

class CastorMock : Castor {
    var parseDIDReturn: DID? = null
    var createPrismDIDReturn: DID? = null
    var createPeerDIDReturn: DID? = DID(
        "did",
        "prism",
        "b6c0c33d701ac1b9a262a14454d1bbde3d127d697a76950963c5fd930605:Cj8KPRI7CgdtYXN0ZXIwEAFKLgoJc2VmsxEiECSTjyV7sUfCr_ArpN9rvCwR9fRMAhcsr_S7ZRiJk4p5k",
    )
    var resolveDIDReturn: DIDDocument? = null
    var verifySignatureReturn: Boolean = false

    override fun parseDID(did: String): DID {
        return parseDIDReturn ?: throw Exception("parseDID() not implemented in mock")
    }

    override fun createPrismDID(
        masterPublicKey: PublicKey,
        services: Array<DIDDocument.Service>?,
    ): DID {
        return createPrismDIDReturn ?: throw Exception("createPrismDID() not implemented in mock")
    }

    override fun createPeerDID(
        keyPairs: Array<KeyPair>,
        services: Array<DIDDocument.Service>,
    ): DID {
        return createPeerDIDReturn ?: throw Exception("createPeerDID() not implemented in mock")
    }

    override suspend fun resolveDID(did: String): DIDDocument {
        val coreProperty = DIDDocument.Service(
            id = "DIDCommV2",
            type = arrayOf("DIDCommMessaging"),
            serviceEndpoint = DIDDocument.ServiceEndpoint(
                uri = "localhost:8082",
                accept = arrayOf("DIDCommMessaging"),
                routingKeys = arrayOf(),
            ),
        )
        resolveDIDReturn = DIDDocument(
            DID(
                "did",
                "prism",
                "b6c0c33d701ac1b9a262a14454d1bbde3d127d697a76950963c5fd930605:Cj8KPRI7CgdtYXN0ZXIwEAFKLgoJc2VmsxEiECSTjyV7sUfCr_ArpN9rvCwR9fRMAhcsr_S7ZRiJk4p5k",
            ),
            arrayOf(coreProperty),
        )
        return resolveDIDReturn ?: throw Exception("resolveDID() not implemented in mock")
    }

    override suspend fun verifySignature(did: DID, challenge: ByteArray, signature: ByteArray): Boolean {
        return verifySignatureReturn
    }
}
