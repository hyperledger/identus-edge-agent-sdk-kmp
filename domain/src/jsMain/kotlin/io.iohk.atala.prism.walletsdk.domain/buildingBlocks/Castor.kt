package io.iohk.atala.prism.walletsdk.domain.buildingBlocks

import io.iohk.atala.prism.walletsdk.domain.models.DID
import io.iohk.atala.prism.walletsdk.domain.models.DIDDocument
import io.iohk.atala.prism.walletsdk.domain.models.KeyPair
import io.iohk.atala.prism.walletsdk.domain.models.PublicKey
import kotlin.js.Promise

@OptIn(ExperimentalJsExport::class)
@JsExport
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

    fun resolveDID(did: String): Promise<DIDDocument>
    fun verifySignature(
        did: DID,
        challenge: ByteArray,
        signature: ByteArray,
    ): Promise<Boolean>
}
