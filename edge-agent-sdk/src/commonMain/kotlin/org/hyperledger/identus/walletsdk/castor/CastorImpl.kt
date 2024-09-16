@file:Suppress("ktlint:standard:import-ordering")

package org.hyperledger.identus.walletsdk.castor

import io.ipfs.multibase.Multibase
import org.hyperledger.identus.apollo.base64.base64UrlDecodedBytes
import org.hyperledger.identus.apollo.utils.KMMECSecp256k1PublicKey
import org.hyperledger.identus.walletsdk.apollo.utils.Ed25519PublicKey
import org.hyperledger.identus.walletsdk.apollo.utils.Secp256k1PublicKey
import org.hyperledger.identus.walletsdk.apollo.utils.X25519PublicKey
import org.hyperledger.identus.walletsdk.castor.resolvers.LongFormPrismDIDResolver
import org.hyperledger.identus.walletsdk.castor.resolvers.PeerDIDResolver
import org.hyperledger.identus.walletsdk.castor.shared.CastorShared
import org.hyperledger.identus.walletsdk.domain.buildingblocks.Apollo
import org.hyperledger.identus.walletsdk.domain.buildingblocks.Castor
import org.hyperledger.identus.walletsdk.domain.models.CastorError
import org.hyperledger.identus.walletsdk.domain.models.Curve
import org.hyperledger.identus.walletsdk.domain.models.DID
import org.hyperledger.identus.walletsdk.domain.models.DIDDocument
import org.hyperledger.identus.walletsdk.domain.models.DIDDocumentCoreProperty
import org.hyperledger.identus.walletsdk.domain.models.DIDResolver
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.KeyPair
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.PublicKey
import org.hyperledger.identus.walletsdk.logger.LogComponent
import org.hyperledger.identus.walletsdk.logger.LogLevel
import org.hyperledger.identus.walletsdk.logger.Metadata
import org.hyperledger.identus.walletsdk.logger.Logger
import org.hyperledger.identus.walletsdk.logger.LoggerImpl

/**
 * Castor is a powerful and flexible library for working with DIDs. Whether you are building a decentralised application
 * or a more traditional system requiring secure and private identity management, Castor provides the tools and features
 * you need to easily create, manage, and resolve DIDs.
 */
class CastorImpl
@JvmOverloads
constructor(
    val apollo: Apollo,
    private val logger: Logger = LoggerImpl(LogComponent.CASTOR)
) : Castor {
    var resolvers: Array<DIDResolver> = arrayOf(
        LongFormPrismDIDResolver(this.apollo),
        PeerDIDResolver()
    )

    fun addResolver(resolver: DIDResolver) {
        resolvers = resolvers.plus(resolver)
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
            metadata = arrayOf(
                Metadata.MaskedMetadataByLevel(
                    key = "DID",
                    value = did,
                    level = LogLevel.DEBUG
                )
            )
        )
        val resolvers = CastorShared.getDIDResolver(did, resolvers)
        resolvers.forEach { resolver ->
            try {
                val resolved = resolver.resolve(did)
                return resolved
            } catch (_: CastorError) {
            }
        }
        throw Exception("No resolver could resolve the provided DID.")
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
    override suspend fun verifySignature(
        did: DID,
        challenge: ByteArray,
        signature: ByteArray
    ): Boolean {
        val document = resolveDID(did.toString())
        val publicKeys: List<PublicKey> =
            getPublicKeysFromCoreProperties(document.coreProperties)

        if (publicKeys.isEmpty()) {
            throw CastorError.InvalidKeyError("DID was resolved, but does not contain public keys in its core properties.")
        }

        for (publicKey in publicKeys) {
            when (publicKey.getCurve()) {
                Curve.SECP256K1.value -> {
                    return (publicKey as Secp256k1PublicKey).verify(challenge, signature)
                }

                Curve.ED25519.value -> {
                    return (publicKey as Ed25519PublicKey).verify(challenge, signature)
                }
            }
        }

        return false
    }

    /**
     * Extract list of [PublicKey] from a list of [DIDDocumentCoreProperty].
     *
     * @param coreProperties list of [DIDDocumentCoreProperty] that we are going to extract a list of [DIDDocumentCoreProperty].
     * @return List<[PublicKey]>
     */
    override fun getPublicKeysFromCoreProperties(coreProperties: Array<DIDDocumentCoreProperty>): List<PublicKey> {
        return coreProperties
            .filterIsInstance<DIDDocument.Authentication>()
            .flatMap { it.verificationMethods.toList() }
            .mapNotNull { verificationMethod ->
                when {
                    verificationMethod.publicKeyJwk != null -> {
                        extractPublicKeyFromJwk(verificationMethod.publicKeyJwk)
                    }

                    verificationMethod.publicKeyMultibase != null -> {
                        extractPublicKeyFromMultibase(
                            verificationMethod.publicKeyMultibase,
                            verificationMethod.type
                        )
                    }

                    else -> null
                }
            }
    }

    private fun extractPublicKeyFromJwk(jwk: Map<String, String>): PublicKey? {
        if (jwk.containsKey("x") && jwk.containsKey("crv")) {
            val x = jwk["x"]
            val crv = jwk["crv"]
            return when (DIDDocument.VerificationMethod.getCurveByType(crv!!)) {
                Curve.SECP256K1 -> {
                    if (jwk.containsKey("y")) {
                        val y = jwk["y"]
                        val kmmSecp = KMMECSecp256k1PublicKey.secp256k1FromByteCoordinates(
                            x!!.base64UrlDecodedBytes,
                            y!!.base64UrlDecodedBytes
                        )
                        Secp256k1PublicKey(kmmSecp.raw)
                    } else {
                        Secp256k1PublicKey(x!!.base64UrlDecodedBytes)
                    }
                }

                Curve.ED25519 -> {
                    Ed25519PublicKey(x!!.base64UrlDecodedBytes)
                }

                Curve.X25519 -> {
                    X25519PublicKey(x!!.base64UrlDecodedBytes)
                }
            }
        }
        return null
    }

    private fun extractPublicKeyFromMultibase(publicKey: String, type: String): PublicKey {
        return when (DIDDocument.VerificationMethod.getCurveByType(type)) {
            Curve.SECP256K1 -> {
                Secp256k1PublicKey(Multibase.decode(publicKey))
            }

            Curve.ED25519 -> {
                Ed25519PublicKey(Multibase.decode(publicKey))
            }

            Curve.X25519 -> {
                X25519PublicKey(Multibase.decode(publicKey))
            }
        }
    }
}
