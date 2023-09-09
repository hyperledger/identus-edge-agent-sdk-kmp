package io.iohk.atala.prism.walletsdk.domain.buildingblocks

import io.iohk.atala.prism.walletsdk.domain.models.CastorError
import io.iohk.atala.prism.walletsdk.domain.models.DID
import io.iohk.atala.prism.walletsdk.domain.models.DIDDocument
import io.iohk.atala.prism.walletsdk.domain.models.KeyPair
import io.iohk.atala.prism.walletsdk.domain.models.PublicKey
import kotlin.jvm.Throws

/**
 * Castor is a powerful and flexible library for working with DIDs. Whether you are building a decentralised application
 * or a more traditional system requiring secure and private identity management, Castor provides the tools and features
 * you need to easily create, manage, and resolve DIDs.
 */
interface Castor {

    /**
     * Parses a string representation of a Decentralized Identifier (DID) into a DID object.
     *
     * @param did The string representation of the DID.
     * @return The [DID] object.
     * @throws [CastorError.InvalidDIDString] if the string is not a valid DID.
     */
    @Throws(CastorError.InvalidDIDString::class)
    fun parseDID(did: String): DID

    /**
     * Creates a DID for a prism (a device or server that acts as a DID owner and controller) using a
     * given master public key and list of services.
     *
     * @param masterPublicKey The master public key of the prism
     * @param services The list of services offered by the prism
     * @return [DID]
     */
    fun createPrismDID(
        masterPublicKey: PublicKey,
        services: Array<DIDDocument.Service>?
    ): DID

    /**
     * Creates a DID for a peer (a device or server that acts as a DID subject) using given key agreement
     * and authentication key pairs and a list of services.
     *
     * @param keyPairs The key pair used for key agreement (establishing secure communication between peers) and
     * authentication (verifying the identity of a peer)
     * @param services The list of services offered by the peer
     * @return The [DID] of the peer
     */
    fun createPeerDID(
        keyPairs: Array<KeyPair>,
        services: Array<DIDDocument.Service>
    ): DID

    /**
     * Asynchronously resolves a DID to its corresponding DID Document. This function may throw an error if
     * the DID is invalid or the document cannot be retrieved.
     *
     * @param did The DID to resolve.
     * @return The DID Document associated with the DID.
     * @throws CastorError.NotPossibleToResolveDID if the DID is invalid or the document cannot be retrieved.
     */
    @Throws(CastorError.NotPossibleToResolveDID::class)
    suspend fun resolveDID(did: String): DIDDocument

    /**
     * Verifies the authenticity of a signature using the corresponding DID Document, challenge, and signature data.
     * This function returns a boolean value indicating whether the signature is valid or not. This function may throw
     * an error if the DID Document or signature data are invalid.
     *
     * @param did The DID associate with the signature.
     * @param challenge The challenge used to generate the signature.
     * @param signature The signature data to verify.
     * @return A [Boolean] value indicating whether the signature is valid or not.
     * @throws [CastorError.InvalidKeyError] if the DID or signature data are invalid.
     */
    @Throws(CastorError.InvalidKeyError::class)
    suspend fun verifySignature(
        did: DID,
        challenge: ByteArray,
        signature: ByteArray
    ): Boolean
}
