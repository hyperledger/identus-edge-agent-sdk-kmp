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
import io.iohk.atala.prism.walletsdk.domain.models.Signature
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.KeyPair
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.PublicKey
import io.iohk.atala.prism.walletsdk.logger.LogComponent
import io.iohk.atala.prism.walletsdk.logger.LogLevel
import io.iohk.atala.prism.walletsdk.logger.Metadata
import io.iohk.atala.prism.walletsdk.logger.PrismLogger
import io.iohk.atala.prism.walletsdk.logger.PrismLoggerImpl
import kotlin.jvm.Throws

/**
 * Castor is a powerful and flexible library for working with DIDs. Whether you are building a decentralised application
 * or a more traditional system requiring secure and private identity management, Castor provides the tools and features
 * you need to easily create, manage, and resolve DIDs.
 */
class CastorImpl(apollo: Apollo, private val logger: PrismLogger = PrismLoggerImpl(LogComponent.CASTOR)) : Castor {
    val apollo: Apollo
    var resolvers: Array<DIDResolver>

    init {
        this.apollo = apollo
        this.resolvers = arrayOf(
            PeerDIDResolver(),
            LongFormPrismDIDResolver(this.apollo)
        )
    }

    /**
     * Parses a string representation of a Decentralized Identifier (DID) into a DID object.
     *
     * @param did The string representation of the DID.
     * @return The [DID] object.
     * @throws [CastorError.InvalidDIDString] if the string is not a valid DID.
     */
    @Throws(CastorError.InvalidDIDString::class)
    override fun parseDID(did: String): DID {
        return CastorShared.parseDID(did)
    }

    /**
     * Creates a DID for a prism (a device or server that acts as a DID owner and controller) using a
     * given master public key and list of services.
     *
     * @param masterPublicKey The master public key of the prism
     * @param services The list of services offered by the prism
     * @return [DID]
     */
    override fun createPrismDID(
        masterPublicKey: PublicKey,
        services: Array<DIDDocument.Service>?
    ): DID {
        return CastorShared.createPrismDID(
            apollo = apollo,
            masterPublicKey = masterPublicKey,
            services = services
        )
    }

    /**
     * Creates a DID for a peer (a device or server that acts as a DID subject) using given key agreement
     * and authentication key pairs and a list of services.
     *
     * @param keyPairs The key pair used for key agreement (establishing secure communication between peers) and
     * authentication (verifying the identity of a peer)
     * @param services The list of services offered by the peer
     * @return The [DID] of the peer
     */
    @Throws(CastorError.InvalidKeyError::class)
    override fun createPeerDID(
        keyPairs: Array<KeyPair>,
        services: Array<DIDDocument.Service>
    ): DID {
        return CastorShared.createPeerDID(
            keyPairs = keyPairs,
            services = services
        )
    }

    /**
     * Asynchronously resolves a DID to its corresponding DID Document. This function may throw an error if
     * the DID is invalid or the document cannot be retrieved.
     *
     * @param did The DID to resolve.
     * @return The DID Document associated with the DID.
     * @throws CastorError.NotPossibleToResolveDID if the DID is invalid or the document cannot be retrieved.
     */
    @Throws(CastorError.NotPossibleToResolveDID::class)
    override suspend fun resolveDID(did: String): DIDDocument {
        logger.debug(
            message = "Trying to resolve DID",
            metadata = arrayOf(Metadata.MaskedMetadataByLevel(key = "DID", value = did, level = LogLevel.DEBUG))
        )
        val resolver = CastorShared.getDIDResolver(did, resolvers)
        return resolver.resolve(did)
    }

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
