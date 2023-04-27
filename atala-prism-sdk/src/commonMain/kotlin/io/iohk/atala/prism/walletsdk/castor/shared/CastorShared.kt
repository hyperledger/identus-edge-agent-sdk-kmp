package io.iohk.atala.prism.walletsdk.castor.shared

import io.iohk.atala.prism.apollo.base64.base64Encoded
import io.iohk.atala.prism.apollo.base64.base64UrlDecodedBytes
import io.iohk.atala.prism.apollo.base64.base64UrlEncoded
import io.iohk.atala.prism.apollo.hashing.SHA256
import io.iohk.atala.prism.apollo.hashing.internal.toHexString
import io.iohk.atala.prism.didcomm.didpeer.DIDCommServicePeerDID
import io.iohk.atala.prism.didcomm.didpeer.DIDDocPeerDID
import io.iohk.atala.prism.didcomm.didpeer.MalformedPeerDIDException
import io.iohk.atala.prism.didcomm.didpeer.VerificationMaterialAgreement
import io.iohk.atala.prism.didcomm.didpeer.VerificationMaterialAuthentication
import io.iohk.atala.prism.didcomm.didpeer.VerificationMaterialFormatPeerDID
import io.iohk.atala.prism.didcomm.didpeer.VerificationMethodPeerDID
import io.iohk.atala.prism.didcomm.didpeer.VerificationMethodTypeAgreement
import io.iohk.atala.prism.didcomm.didpeer.VerificationMethodTypeAuthentication
import io.iohk.atala.prism.didcomm.didpeer.core.toJsonElement
import io.iohk.atala.prism.didcomm.didpeer.createPeerDIDNumalgo2
import io.iohk.atala.prism.protos.AtalaOperation
import io.iohk.atala.prism.protos.CreateDIDOperation
import io.iohk.atala.prism.protos.Service
import io.iohk.atala.prism.walletsdk.castor.did.DIDParser
import io.iohk.atala.prism.walletsdk.castor.did.DIDUrlParser
import io.iohk.atala.prism.walletsdk.castor.did.prismdid.LongFormPrismDID
import io.iohk.atala.prism.walletsdk.castor.did.prismdid.PrismDIDMethodId
import io.iohk.atala.prism.walletsdk.castor.did.prismdid.PrismDIDPublicKey
import io.iohk.atala.prism.walletsdk.castor.did.prismdid.defaultId
import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Apollo
import io.iohk.atala.prism.walletsdk.domain.models.CastorError
import io.iohk.atala.prism.walletsdk.domain.models.Curve
import io.iohk.atala.prism.walletsdk.domain.models.DID
import io.iohk.atala.prism.walletsdk.domain.models.DIDDocument
import io.iohk.atala.prism.walletsdk.domain.models.DIDDocumentCoreProperty
import io.iohk.atala.prism.walletsdk.domain.models.DIDResolver
import io.iohk.atala.prism.walletsdk.domain.models.DIDUrl
import io.iohk.atala.prism.walletsdk.domain.models.KeyCurve
import io.iohk.atala.prism.walletsdk.domain.models.KeyPair
import io.iohk.atala.prism.walletsdk.domain.models.OctetPublicKey
import io.iohk.atala.prism.walletsdk.domain.models.PublicKey
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import pbandk.decodeFromByteArray
import pbandk.encodeToByteArray
import kotlin.jvm.Throws
import io.iohk.atala.prism.didcomm.didpeer.resolvePeerDID as mercuryPeerDIDResolve

internal class CastorShared {
    companion object {
        @JvmStatic
        fun parseDID(did: String): DID {
            return DIDParser.parse(did)
        }

        @JvmStatic
        @Throws(CastorError.InvalidPeerDIDError::class, CastorError.NotPossibleToResolveDID::class)
        suspend fun resolvePeerDID(didString: String): DIDDocument {
            val peerDIDDocument = try {
                DIDDocPeerDID.fromJson(mercuryPeerDIDResolve(didString))
            } catch (e: MalformedPeerDIDException) {
                throw CastorError.InvalidPeerDIDError()
            } catch (e: Throwable) {
                throw CastorError.NotPossibleToResolveDID()
            }

            val coreProperties: MutableList<DIDDocumentCoreProperty> = mutableListOf()

            coreProperties.add(
                DIDDocument.Authentication(
                    urls = arrayOf(),
                    verificationMethods = peerDIDDocument.authentication.map {
                        fromVerificationMethodPeerDID(it.id, it)
                    }.toTypedArray(),
                ),
            )
            coreProperties.add(
                DIDDocument.KeyAgreement(
                    urls = arrayOf(),
                    verificationMethods = peerDIDDocument.keyAgreement.map {
                        fromVerificationMethodPeerDID(it.id, it)
                    }.toTypedArray(),
                ),
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
                                    uri = service.serviceEndpoint,
                                    accept = service.accept.toTypedArray(),
                                    routingKeys = service.routingKeys.toTypedArray(),
                                ),
                            ),
                        )
                    }
                }
            }

            coreProperties.add(
                DIDDocument.Services(
                    services.toTypedArray(),
                ),
            )

            val did = DIDParser.parse(didString)

            return DIDDocument(
                id = did,
                coreProperties = coreProperties.toTypedArray(),
            )
        }

        @JvmStatic
        @Throws(CastorError.InvalidKeyError::class)
        fun fromVerificationMethodPeerDID(
            did: String,
            verificationMethod: VerificationMethodPeerDID,
        ): DIDDocument.VerificationMethod {
            val didUrl = DIDUrlParser.parse(did)
            val controller = DIDParser.parse(verificationMethod.controller)
            val type = when (verificationMethod.verMaterial.type.value) {
                VerificationMethodTypeAuthentication.ED25519_VERIFICATION_KEY_2020.value -> {
                    Curve.ED25519.value
                }

                VerificationMethodTypeAuthentication.ED25519_VERIFICATION_KEY_2018.value -> {
                    Curve.ED25519.value
                }

                VerificationMethodTypeAgreement.X25519_KEY_AGREEMENT_KEY_2020.value -> {
                    Curve.X25519.value
                }

                VerificationMethodTypeAgreement.X25519_KEY_AGREEMENT_KEY_2019.value -> {
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
                        Json.decodeFromString<Map<String, String>>(verificationMethod.verMaterial.value as String),
                    )
                }

                VerificationMaterialFormatPeerDID.BASE58 -> {
                    DIDDocument.VerificationMethod(
                        didUrl,
                        controller,
                        type,
                        null,
                        verificationMethod.verMaterial.value as String,
                    )
                }

                VerificationMaterialFormatPeerDID.MULTIBASE -> {
                    DIDDocument.VerificationMethod(
                        didUrl,
                        controller,
                        type,
                        null,
                        verificationMethod.verMaterial.value as String,
                    )
                }
            }
        }

        @JvmStatic
        @Throws(CastorError.InitialStateOfDIDChanged::class)
        suspend fun resolveLongFormPrismDID(apollo: Apollo, didString: String): DIDDocument {
            val did = DIDParser.parse(didString)
            val prismDID = LongFormPrismDID(did)

            val (verificationMethods, services) = try {
                decodeState(
                    apollo = apollo,
                    did = did,
                    stateHash = prismDID.stateHash,
                    encodedData = prismDID.encodedState.base64UrlDecodedBytes,
                )
            } catch (e: Throwable) {
                // TODO: Add logger here
                throw CastorError.InitialStateOfDIDChanged(e.message)
            }

            val servicesProperty = DIDDocument.Services(services.toTypedArray())
            val verificationMethodsProperty = DIDDocument.VerificationMethods(verificationMethods.values.toTypedArray())
            val coreProperties = mutableListOf<DIDDocumentCoreProperty>()

            val authenticate = verificationMethods.entries.map {
                DIDDocument.Authentication(
                    urls = arrayOf(it.key),
                    verificationMethods = verificationMethods.values.toTypedArray(),
                )
            }

            authenticate.forEach {
                coreProperties.add(it)
            }

            coreProperties.add(servicesProperty)
            coreProperties.add(verificationMethodsProperty)

            return DIDDocument(
                id = did,
                coreProperties = coreProperties.toTypedArray(),
            )
        }

        @JvmStatic
        @Throws(CastorError.InitialStateOfDIDChanged::class, Exception::class)
        fun decodeState(
            apollo: Apollo,
            did: DID,
            stateHash: String,
            encodedData: ByteArray,
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
                    throw e
                }
            } ?: listOf()

            val services = operation.createDid?.didData?.services?.map {
                DIDDocument.Service(
                    it.id,
                    arrayOf(it.type),
                    DIDDocument.ServiceEndpoint(
                        uri = it.serviceEndpoint.first(),
                    ),
                )
            } ?: listOf()

            val verificationMethods =
                publicKeys.fold(emptyMap<String, DIDDocument.VerificationMethod>()) { partialResult, publicKey ->
                    val didUrl = DIDUrl(
                        did = did,
                        fragment = publicKey.id,
                    )
                    val method = DIDDocument.VerificationMethod(
                        id = didUrl,
                        controller = did,
                        type = publicKey.keyData.curve.curve.value,
                        publicKeyMultibase = publicKey.keyData.value.base64Encoded,
                    )
                    partialResult + (didUrl.string() to method)
                }

            return Pair(verificationMethods, services)
        }

        @JvmStatic
        @Throws(CastorError.NotPossibleToResolveDID::class)
        fun getDIDResolver(did: String, resolvers: Array<DIDResolver>): DIDResolver {
            val parsedDID = parseDID(did)
            return resolvers.find { it.method == parsedDID.method } ?: throw CastorError.NotPossibleToResolveDID()
        }

        @JvmStatic
        fun getKeyPairFromCoreProperties(coreProperties: Array<DIDDocumentCoreProperty>): List<PublicKey> {
            return coreProperties
                .filterIsInstance<DIDDocument.Authentication>()
                .flatMap { it.verificationMethods.toList() }
                .mapNotNull {
                    it.publicKeyMultibase?.let { publicKey ->
                        PublicKey(
                            curve = KeyCurve(DIDDocument.VerificationMethod.getCurveByType(it.type)),
                            value = publicKey.encodeToByteArray(),
                        )
                    }
                }
        }

        private fun octetPublicKey(keyPair: KeyPair): OctetPublicKey {
            return OctetPublicKey(crv = keyPair.keyCurve.curve.value, x = keyPair.publicKey.value.base64UrlEncoded)
        }

        @JvmStatic
        @Throws(CastorError.InvalidKeyError::class)
        fun createPeerDID(
            keyPairs: Array<KeyPair>,
            services: Array<DIDDocument.Service>,
        ): DID {
            val encryptionKeys: MutableList<VerificationMaterialAgreement> = mutableListOf()
            val signingKeys: MutableList<VerificationMaterialAuthentication> = mutableListOf()

            keyPairs.forEach {
                when (it.keyCurve.curve) {
                    Curve.X25519 -> {
                        val octetString = Json.encodeToString(octetPublicKey(it))
                        encryptionKeys.add(
                            VerificationMaterialAgreement(
                                format = VerificationMaterialFormatPeerDID.JWK,
                                value = octetString,
//                              value = Json.encodeToString(toJwk(it.publicKey.value, VerificationMethodTypeAgreement.JSON_WEB_KEY_2020)),
                                type = VerificationMethodTypeAgreement.JSON_WEB_KEY_2020,
                            ),
                        )
                    }
                    Curve.ED25519 -> {
                        val octetString = Json.encodeToString(octetPublicKey(it))
                        signingKeys.add(
                            VerificationMaterialAuthentication(
                                format = VerificationMaterialFormatPeerDID.JWK,
                                value = octetString,
//                              value = Json.encodeToString(toJwk(it.publicKey.value, VerificationMethodTypeAuthentication.JSON_WEB_KEY_2020)),
                                type = VerificationMethodTypeAuthentication.JSON_WEB_KEY_2020,
                            ),
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
                    Json.encodeToString(
                        DIDCommServicePeerDID(
                            id = it.id,
                            type = it.type[0],
                            serviceEndpoint = it.serviceEndpoint.uri,
                            routingKeys = listOf(),
                            accept = it.serviceEndpoint.accept?.asList() ?: listOf(),
                        ).toDict().toJsonElement(),
                    )
                }.first(),
            )

            return DIDParser.parse(peerDID)
        }

        @JvmStatic
        fun createPrismDID(
            apollo: Apollo,
            masterPublicKey: PublicKey,
            services: Array<DIDDocument.Service>?,
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
                                    keyData = masterPublicKey,
                                ).toProto(),
                            ),
                            services = services?.map {
                                Service(
                                    id = it.id,
                                    type = it.type.first(),
                                    serviceEndpoint = listOf(it.serviceEndpoint.uri),
                                )
                            } ?: emptyList(),
                        ),
                    ),
                ),
            )

            val encodedState = atalaOperation.encodeToByteArray()
            val stateHash = SHA256().digest(encodedState).toHexString()
            val base64State = encodedState.base64UrlEncoded
            val methodSpecificId = PrismDIDMethodId(
                sections = listOf(
                    stateHash,
                    base64State,
                ),
            )

            return DID(
                schema = "did",
                method = "prism",
                methodId = methodSpecificId.toString(),
            )
        }
    }
}
