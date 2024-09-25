package org.hyperledger.identus.walletsdk.castor.shared

import io.iohk.atala.prism.didcomm.didpeer.DIDCommServicePeerDID
import io.iohk.atala.prism.didcomm.didpeer.DIDDocPeerDID
import io.iohk.atala.prism.didcomm.didpeer.MalformedPeerDIDException
import io.iohk.atala.prism.didcomm.didpeer.ServiceEndpoint
import io.iohk.atala.prism.didcomm.didpeer.VerificationMaterialAgreement
import io.iohk.atala.prism.didcomm.didpeer.VerificationMaterialAuthentication
import io.iohk.atala.prism.didcomm.didpeer.VerificationMaterialFormatPeerDID
import io.iohk.atala.prism.didcomm.didpeer.VerificationMethodPeerDID
import io.iohk.atala.prism.didcomm.didpeer.VerificationMethodTypeAgreement
import io.iohk.atala.prism.didcomm.didpeer.VerificationMethodTypeAuthentication
import io.iohk.atala.prism.didcomm.didpeer.core.toJsonElement
import io.iohk.atala.prism.didcomm.didpeer.createPeerDIDNumalgo2
import io.ipfs.multibase.Multibase
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.hyperledger.identus.apollo.base64.base64UrlDecodedBytes
import org.hyperledger.identus.apollo.base64.base64UrlEncoded
import org.hyperledger.identus.protos.AtalaOperation
import org.hyperledger.identus.protos.CreateDIDOperation
import org.hyperledger.identus.protos.Service
import org.hyperledger.identus.walletsdk.apollo.utils.Ed25519KeyPair
import org.hyperledger.identus.walletsdk.apollo.utils.Secp256k1KeyPair
import org.hyperledger.identus.walletsdk.apollo.utils.X25519KeyPair
import org.hyperledger.identus.walletsdk.castor.DID
import org.hyperledger.identus.walletsdk.castor.PRISM
import org.hyperledger.identus.walletsdk.castor.did.DIDParser
import org.hyperledger.identus.walletsdk.castor.did.DIDUrlParser
import org.hyperledger.identus.walletsdk.castor.did.prismdid.LongFormPrismDID
import org.hyperledger.identus.walletsdk.castor.did.prismdid.PrismDIDMethodId
import org.hyperledger.identus.walletsdk.castor.did.prismdid.PrismDIDPublicKey
import org.hyperledger.identus.walletsdk.castor.did.prismdid.defaultId
import org.hyperledger.identus.walletsdk.domain.buildingblocks.Apollo
import org.hyperledger.identus.walletsdk.domain.models.CastorError
import org.hyperledger.identus.walletsdk.domain.models.Curve
import org.hyperledger.identus.walletsdk.domain.models.DID
import org.hyperledger.identus.walletsdk.domain.models.DIDDocument
import org.hyperledger.identus.walletsdk.domain.models.DIDDocumentCoreProperty
import org.hyperledger.identus.walletsdk.domain.models.DIDResolver
import org.hyperledger.identus.walletsdk.domain.models.DIDUrl
import org.hyperledger.identus.walletsdk.domain.models.OctetPublicKey
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.KeyPair
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.PublicKey
import org.hyperledger.identus.walletsdk.logger.LogComponent
import org.hyperledger.identus.walletsdk.logger.LogLevel
import org.hyperledger.identus.walletsdk.logger.LoggerImpl
import org.hyperledger.identus.walletsdk.logger.Metadata
import org.kotlincrypto.hash.sha2.SHA256
import pbandk.decodeFromByteArray
import pbandk.encodeToByteArray
import kotlin.jvm.Throws
import io.iohk.atala.prism.didcomm.didpeer.resolvePeerDID as mercuryPeerDIDResolve

/**
 * The `CastorShared` class provides a set of logging methods for the Castor module.
 */
internal class CastorShared {
    companion object {

        /**
         * The logger property is a PrismLoggerImpl instance for logging purposes.
         * It is used to log debug, info, warning, and error messages, along with the associated metadata.
         *
         * @property logger The logger instance.
         */
        private val logger = LoggerImpl(LogComponent.CASTOR)

        /**
         * parseDID parses a string representation of a Decentralized Identifier (DID) into a DID object.
         *
         * @param did The string representation of the DID.
         * @return The [DID] object.
         * @throws [CastorError.InvalidDIDString] if the string is not a valid DID.
         */
        @JvmStatic
        @Throws(CastorError.InvalidDIDString::class)
        fun parseDID(did: String): DID {
            return DIDParser.parse(did)
        }

        /**
         * createPrismDID creates a DID for a prism (a device or server that acts as a DID owner and controller) using a
         * given master public key and list of services.
         *
         * @param apollo the cryptography suit representation.
         * @param masterPublicKey master public key used in creating the Prism DID.
         * @param services list of services.
         * @return [DID].
         */
        @JvmStatic
        internal fun createPrismDID(
            apollo: Apollo,
            masterPublicKey: PublicKey,
            services: Array<DIDDocument.Service>?
        ): DID {
            val atalaOperation = AtalaOperation(
                operation = AtalaOperation.Operation.CreateDid(
                    CreateDIDOperation(
                        didData = CreateDIDOperation.DIDCreationData(
                            publicKeys = listOf(
                                PrismDIDPublicKey(
                                    apollo = apollo,
                                    id = PrismDIDPublicKey.Usage.MASTER_KEY.defaultId(),
                                    usage = PrismDIDPublicKey.Usage.MASTER_KEY,
                                    keyData = masterPublicKey
                                ).toProto(),
                                PrismDIDPublicKey(
                                    apollo = apollo,
                                    id = PrismDIDPublicKey.Usage.AUTHENTICATION_KEY.defaultId(),
                                    usage = PrismDIDPublicKey.Usage.AUTHENTICATION_KEY,
                                    keyData = masterPublicKey
                                ).toProto()
                            ),
                            services = services?.map {
                                Service(
                                    id = it.id,
                                    type = it.type.first(),
                                    serviceEndpoint = listOf(it.serviceEndpoint.uri)
                                )
                            } ?: emptyList()
                        )
                    )
                )
            )

            val encodedState = atalaOperation.encodeToByteArray()
            val stateHash = SHA256().digest(encodedState).toHexString()
            val base64State = encodedState.base64UrlEncoded
            val methodSpecificId = PrismDIDMethodId(
                sections = listOf(
                    stateHash,
                    base64State
                )
            )

            return DID(
                schema = DID,
                method = PRISM,
                methodId = methodSpecificId.toString()
            )
        }

        /**
         * Resolve PeerDID String to [DIDDocument].
         *
         * @param didString that we need to resolve.
         * @return [DIDDocument]
         * @throws [CastorError.InvalidPeerDIDError] when the provided DID is invalid.
         * @throws [CastorError.NotPossibleToResolveDID] when unable to resolve the provided DID.
         */
        @JvmStatic
        @Throws(CastorError.InvalidPeerDIDError::class, CastorError.NotPossibleToResolveDID::class)
        internal fun resolvePeerDID(didString: String): DIDDocument {
            val peerDIDDocument = try {
                DIDDocPeerDID.fromJson(mercuryPeerDIDResolve(didString))
            } catch (e: MalformedPeerDIDException) {
                throw CastorError.InvalidPeerDIDError(e.message, e.cause)
            } catch (e: Throwable) {
                throw CastorError.NotPossibleToResolveDID(
                    did = didString,
                    reason = "Method or method id are invalid",
                    e
                )
            }

            val coreProperties: MutableList<DIDDocumentCoreProperty> = mutableListOf()

            coreProperties.add(
                DIDDocument.Authentication(
                    urls = arrayOf(),
                    verificationMethods = peerDIDDocument.authentication.map {
                        fromVerificationMethodPeerDID(it.id, it)
                    }.toTypedArray()
                )
            )
            coreProperties.add(
                DIDDocument.KeyAgreement(
                    urls = arrayOf(),
                    verificationMethods = peerDIDDocument.keyAgreement.map {
                        fromVerificationMethodPeerDID(it.id, it)
                    }.toTypedArray()
                )
            )

            val peerDIDServices = peerDIDDocument.service ?: listOf()
            val services: MutableList<DIDDocument.Service> = mutableListOf()

            peerDIDServices.forEach { service ->
                run {
                    if (service is DIDCommServicePeerDID) {
                        services.add(
                            DIDDocument.Service(
                                id = service.id,
                                type = arrayOf(service.type),
                                serviceEndpoint = DIDDocument.ServiceEndpoint(
                                    uri = service.serviceEndpoint.uri,
                                    accept = service.serviceEndpoint.accept.toTypedArray(),
                                    routingKeys = service.serviceEndpoint.routingKeys.toTypedArray()
                                )
                            )
                        )
                    }
                }
            }

            coreProperties.add(
                DIDDocument.Services(
                    services.toTypedArray()
                )
            )

            val did = DIDParser.parse(didString)

            return DIDDocument(
                id = did,
                coreProperties = coreProperties.toTypedArray()
            )
        }

        /**
         * Extract the [DIDDocument.VerificationMethod] for the [did] based on the [verificationMethod].
         *
         * @param did string of the PeerDID
         * @param verificationMethod of the PeerDID
         * @return [DIDDocument.VerificationMethod]
         * @throws [CastorError.InvalidKeyError] if provided [verificationMethod] is not supported.
         */
        @JvmStatic
        @Throws(CastorError.InvalidKeyError::class)
        private fun fromVerificationMethodPeerDID(
            did: String,
            verificationMethod: VerificationMethodPeerDID
        ): DIDDocument.VerificationMethod {
            val didUrl = DIDUrlParser.parse(did)
            val controller = DIDParser.parse(verificationMethod.controller)
            val type = when (verificationMethod.verMaterial.type.value) {
                VerificationMethodTypeAuthentication.ED25519VerificationKey2020.value,
                VerificationMethodTypeAuthentication.ED25519VerificationKey2018.value -> {
                    Curve.ED25519.value
                }

                VerificationMethodTypeAgreement.X25519KeyAgreementKey2020.value,
                VerificationMethodTypeAgreement.X25519KeyAgreementKey2019.value -> {
                    Curve.X25519.value
                }

                else -> {
                    throw CastorError.InvalidKeyError()
                }
            }

            return when (verificationMethod.verMaterial.format) {
                VerificationMaterialFormatPeerDID.JWK -> {
                    DIDDocument.VerificationMethod(
                        didUrl,
                        controller,
                        type,
                        Json.decodeFromString<Map<String, String>>(verificationMethod.verMaterial.value as String)
                    )
                }

                VerificationMaterialFormatPeerDID.BASE58 -> {
                    DIDDocument.VerificationMethod(
                        didUrl,
                        controller,
                        type,
                        null,
                        verificationMethod.verMaterial.value as String
                    )
                }

                VerificationMaterialFormatPeerDID.MULTIBASE -> {
                    DIDDocument.VerificationMethod(
                        didUrl,
                        controller,
                        type,
                        null,
                        verificationMethod.verMaterial.value as String
                    )
                }
            }
        }

        /**
         * Resolve Long Form of Prism DID.
         *
         * @param apollo the cryptography suit representation.
         * @param didString The DID string we want to resolve.
         * @return [DIDDocument]
         * @throws [CastorError.InitialStateOfDIDChanged] when decoding the state fails.
         */
        @JvmStatic
        @Throws(CastorError.InitialStateOfDIDChanged::class)
        internal fun resolveLongFormPrismDID(apollo: Apollo, didString: String): DIDDocument {
            val did = DIDParser.parse(didString)
            val prismDID = LongFormPrismDID(did)

            val (verificationMethods, services) = try {
                decodeState(
                    apollo = apollo,
                    did = did,
                    stateHash = prismDID.stateHash,
                    encodedData = prismDID.encodedState.base64UrlDecodedBytes
                )
            } catch (e: Throwable) {
                logger.error(
                    message = "The DID state hash does not match the state",
                    metadata = arrayOf(
                        Metadata.MaskedMetadataByLevel(
                            key = "DID",
                            value = didString,
                            level = LogLevel.DEBUG
                        )
                    )
                )
                throw CastorError.InitialStateOfDIDChanged(e.message)
            }

            val servicesProperty = DIDDocument.Services(services.toTypedArray())
            val verificationMethodsProperty = DIDDocument.VerificationMethods(verificationMethods.values.toTypedArray())
            val coreProperties = mutableListOf<DIDDocumentCoreProperty>()

            val authenticate = verificationMethods.entries.map {
                DIDDocument.Authentication(
                    urls = arrayOf(it.key),
                    verificationMethods = verificationMethods.values.toTypedArray()
                )
            }

            authenticate.forEach {
                coreProperties.add(it)
            }

            coreProperties.add(servicesProperty)
            coreProperties.add(verificationMethodsProperty)

            return DIDDocument(
                id = did,
                coreProperties = coreProperties.toTypedArray()
            )
        }

        /**
         * Decode the provided [encodedData] to [Pair] of a [Map] and of List<[DIDDocument.Service]> where the [Map] is
         * key as [String] and its value [DIDDocument.VerificationMethod].
         *
         * @param apollo the cryptography suit representation.
         * @param did the DID that is used in to decode.
         * @param stateHash the hashed version of the [encodedData] in Hex string format.
         * @param encodedData the [ByteArray]
         * @return [Pair] of a [Map] and of List<[DIDDocument.Service]> where the [Map] is key as [String] and its value
         * [DIDDocument.VerificationMethod].
         * @throws [CastorError.InitialStateOfDIDChanged]
         * @throws [Exception]
         */
        @JvmStatic
        @Throws(CastorError.InitialStateOfDIDChanged::class, Exception::class)
        private fun decodeState(
            apollo: Apollo,
            did: DID,
            stateHash: String,
            encodedData: ByteArray
        ): Pair<Map<String, DIDDocument.VerificationMethod>, List<DIDDocument.Service>> {
            val sha256 = SHA256()
            val verifyEncodedState = sha256.digest(encodedData)
            val verifyEncodedStateHex = verifyEncodedState.toHexString()

            require(stateHash == verifyEncodedStateHex) {
                throw CastorError.InitialStateOfDIDChanged()
            }

            val operation = AtalaOperation.decodeFromByteArray(encodedData)

            val publicKeys = operation.createDid?.didData?.publicKeys?.map {
                try {
                    PrismDIDPublicKey(apollo, it)
                } catch (e: Exception) {
                    logger.error(
                        message = "Failed to decode public key from document",
                        metadata = arrayOf(
                            Metadata.MaskedMetadataByLevel(
                                key = "DID",
                                value = did.toString(),
                                level = LogLevel.DEBUG
                            )
                        )
                    )
                    throw e
                }
            } ?: listOf()

            val services = operation.createDid?.didData?.services?.map {
                DIDDocument.Service(
                    it.id,
                    arrayOf(it.type),
                    DIDDocument.ServiceEndpoint(
                        uri = it.serviceEndpoint.first()
                    )
                )
            } ?: listOf()

            val verificationMethods =
                publicKeys.fold(emptyMap<String, DIDDocument.VerificationMethod>()) { partialResult, publicKey ->
                    val didUrl = DIDUrl(
                        did = did,
                        fragment = publicKey.id
                    )
                    val method = DIDDocument.VerificationMethod(
                        id = didUrl,
                        controller = did,
                        type = publicKey.keyData.getCurve(),
                        publicKeyMultibase = Multibase.encode(Multibase.Base.Base58BTC, publicKey.keyData.getValue())
                    )
                    partialResult + (didUrl.string() to method)
                }

            return Pair(verificationMethods, services)
        }

        /**
         * Get a DID resolver based on the provided DID string.
         *
         * @param did the DID string.
         * @param resolvers list of available resolvers.
         * @throws [CastorError.NotPossibleToResolveDID] if no resolver was found.
         */
        @JvmStatic
        @Throws(CastorError.NotPossibleToResolveDID::class)
        internal fun getDIDResolver(did: String, resolvers: Array<DIDResolver>): List<DIDResolver> {
            val parsedDID = parseDID(did)
            return resolvers.filter { it.method == parsedDID.method }
        }

        /**
         * Create [OctetPublicKey] from a [KeyPair].
         *
         * @param keyPair keyPair to be used in creating [OctetPublicKey].
         * @return [OctetPublicKey].
         */
        @JvmStatic
        private fun octetPublicKey(keyPair: KeyPair): OctetPublicKey {
            val curve = when (keyPair::class) {
                Secp256k1KeyPair::class -> {
                    Curve.SECP256K1
                }

                Ed25519KeyPair::class -> {
                    Curve.ED25519
                }

                X25519KeyPair::class -> {
                    Curve.X25519
                }

                else -> {
                    throw CastorError.KeyCurveNotSupported(KeyPair::class.simpleName ?: "")
                }
            }
            return OctetPublicKey(crv = curve.value, x = keyPair.publicKey.getValue().base64UrlEncoded)
        }

        /**
         * createPeerDID creates a DID for a peer (a device or server that acts as a DID subject) using given key agreement
         * and authentication key pairs and a list of services.
         *
         * @param keyPairs The key pair used for key agreement (establishing secure communication between peers) and
         * authentication (verifying the identity of a peer)
         * @param services The list of services offered by the peer
         * @return The [DID] of the peer
         */
        @JvmStatic
        @Throws(CastorError.InvalidKeyError::class)
        internal fun createPeerDID(
            keyPairs: Array<KeyPair>,
            services: Array<DIDDocument.Service>
        ): DID {
            val encryptionKeys: MutableList<VerificationMaterialAgreement> = mutableListOf()
            val signingKeys: MutableList<VerificationMaterialAuthentication> = mutableListOf()

            keyPairs.forEach {
                when (it::class) {
                    X25519KeyPair::class -> {
                        val octetString = Json.encodeToString(octetPublicKey(it))
                        encryptionKeys.add(
                            VerificationMaterialAgreement(
                                format = VerificationMaterialFormatPeerDID.JWK,
                                value = octetString,
                                type = VerificationMethodTypeAgreement.JsonWebKey2020
                            )
                        )
                    }

                    Ed25519KeyPair::class -> {
                        val octetString = Json.encodeToString(octetPublicKey(it))
                        signingKeys.add(
                            VerificationMaterialAuthentication(
                                format = VerificationMaterialFormatPeerDID.JWK,
                                value = octetString,
                                type = VerificationMethodTypeAuthentication.JsonWebKey2020
                            )
                        )
                    }

                    else -> {
                        throw CastorError.InvalidKeyError()
                    }
                }
            }

            if (signingKeys.isEmpty() || encryptionKeys.isEmpty()) {
                throw CastorError.InvalidKeyError()
            }

            val peerDID = createPeerDIDNumalgo2(
                encryptionKeys = encryptionKeys,
                signingKeys = signingKeys,
                service = services.map {
                    val serviceEndpoint = ServiceEndpoint(
                        uri = it.serviceEndpoint.uri,
                        routingKeys = it.serviceEndpoint.routingKeys?.toList() ?: listOf(),
                        accept = it.serviceEndpoint.accept?.asList() ?: listOf()
                    )

                    Json.encodeToString(
                        DIDCommServicePeerDID(
                            id = it.id,
                            type = it.type[0],
                            serviceEndpoint = serviceEndpoint
                        ).toDict().toJsonElement()
                    )
                }.firstOrNull()
            )

            return DIDParser.parse(peerDID)
        }
    }
}
