package io.iohk.atala.prism.domain.buildingBlocks

interface Castor {

    @Throws()
    fun parseDID(did: String): DID

    @Throws()
    fun createPrismDID(
        masterPublicKey: PublicKey,
        services: Array<DIDDocument.Service>? = null
    ): DID

    @Throws()
    fun createPeerDID(
        keyAgreementKeyPair: KeyPair,
        authenticationKeyPair: KeyPair,
        services: Array<DIDDocument.Service>
    ): DID

    @Throws()
    suspend fun resolveDID(did: DID): DIDDocument

    @Throws()
    suspend fun verifySignature(
        did: DID,
        challenge: ByteArray,
        signature: ByteArray
    ) : Boolean

    @Throws()
    fun getEcnumbasis(
        did: DID,
        keyPair: KeyPair
    ): String
}
