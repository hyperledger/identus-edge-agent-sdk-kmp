package io.iohk.atala.prism.domain.buildingBlocks

import io.iohk.atala.prism.domain.models.DID
import io.iohk.atala.prism.domain.models.DIDDocument
import io.iohk.atala.prism.domain.models.KeyPair
import io.iohk.atala.prism.domain.models.PublicKey

interface Castor {

    @Throws() // TODO: Add throw classes
    fun parseDID(did: String): DID

    @Throws() // TODO: Add throw classes
    fun createPrismDID(
        masterPublicKey: PublicKey,
        services: Array<DIDDocument.Service>? = null
    ): DID

    @Throws() // TODO: Add throw classes
    fun createPeerDID(
        keyAgreementKeyPair: KeyPair,
        authenticationKeyPair: KeyPair,
        services: Array<DIDDocument.Service>
    ): DID

    @Throws() // TODO: Add throw classes
    suspend fun resolveDID(did: String): DIDDocument

    @Throws() // TODO: Add throw classes
    suspend fun verifySignature(
        did: DID,
        challenge: ByteArray,
        signature: ByteArray
    ): Boolean

    @Throws() // TODO: Add throw classes
    fun getEcnumbasis(
        did: DID,
        keyPair: KeyPair
    ): String
}
