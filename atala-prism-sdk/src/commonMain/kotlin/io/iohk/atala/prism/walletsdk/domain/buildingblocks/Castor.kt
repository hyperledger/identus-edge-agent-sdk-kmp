package io.iohk.atala.prism.walletsdk.domain.buildingblocks

import io.iohk.atala.prism.walletsdk.domain.models.DID
import io.iohk.atala.prism.walletsdk.domain.models.DIDDocument
import io.iohk.atala.prism.walletsdk.domain.models.KeyPair
import io.iohk.atala.prism.walletsdk.domain.models.PublicKey

/**
 * TODO(Clarify what Castor methods stand for)
 * TODO(Add method documentations)
 */
interface Castor {
    fun parseDID(did: String): DID
    fun createPrismDID(
        masterPublicKey: PublicKey,
        services: Array<DIDDocument.Service>?,
    ): DID

    fun createPeerDID(
        keyPairs: Array<KeyPair>,
        services: Array<DIDDocument.Service>,
    ): DID

    suspend fun resolveDID(did: String): DIDDocument

    suspend fun verifySignature(
        did: DID,
        challenge: ByteArray,
        signature: ByteArray,
    ): Boolean
}
