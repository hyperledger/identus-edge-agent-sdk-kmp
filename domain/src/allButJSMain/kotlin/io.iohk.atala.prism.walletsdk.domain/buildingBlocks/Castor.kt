package io.iohk.atala.prism.walletsdk.domain.buildingBlocks

import io.iohk.atala.prism.walletsdk.domain.models.DID
import io.iohk.atala.prism.walletsdk.domain.models.DIDDocument
import io.iohk.atala.prism.walletsdk.domain.models.KeyPair
import io.iohk.atala.prism.walletsdk.domain.models.PublicKey

actual interface Castor {
    actual fun parseDID(did: String): DID
    actual fun createPrismDID(
        masterPublicKey: PublicKey,
        services: Array<DIDDocument.Service>?,
    ): DID

    actual fun createPeerDID(
        keyPairs: Array<KeyPair>,
        services: Array<DIDDocument.Service>,
    ): DID

    @Throws() // TODO: Add throw classes
    suspend fun resolveDID(did: String): DIDDocument

    @Throws() // TODO: Add throw classes
    suspend fun verifySignature(
        did: DID,
        challenge: ByteArray,
        signature: ByteArray,
    ): Boolean
}
