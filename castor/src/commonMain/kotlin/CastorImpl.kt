package io.iohk.atala.prism.castor

import io.iohk.atala.prism.apollo.ApolloImpl
import io.iohk.atala.prism.apollo.base64.base64UrlEncoded
import io.iohk.atala.prism.apollo.hashing.SHA256
import io.iohk.atala.prism.apollo.hashing.internal.toHexString
import io.iohk.atala.prism.castor.did.DIDParser
import io.iohk.atala.prism.castor.did.prismdid.PrismDIDMethodId
import io.iohk.atala.prism.castor.did.prismdid.PrismDIDPublicKey
import io.iohk.atala.prism.castor.did.prismdid.defaultId
import io.iohk.atala.prism.castor.io.iohk.atala.prism.castor.resolvers.LongFormPrismDIDResolver
import io.iohk.atala.prism.castor.io.iohk.atala.prism.castor.resolvers.PeerDIDResolver
import io.iohk.atala.prism.domain.buildingBlocks.Apollo
import io.iohk.atala.prism.domain.buildingBlocks.Castor
import io.iohk.atala.prism.domain.models.CastorError
import io.iohk.atala.prism.domain.models.Curve
import io.iohk.atala.prism.domain.models.DID
import io.iohk.atala.prism.domain.models.DIDDocument
import io.iohk.atala.prism.domain.models.DIDDocument.VerificationMethod.Companion.getCurveByType
import io.iohk.atala.prism.domain.models.DIDResolver
import io.iohk.atala.prism.domain.models.KeyCurve
import io.iohk.atala.prism.domain.models.KeyPair
import io.iohk.atala.prism.domain.models.PublicKey
import io.iohk.atala.prism.domain.models.Signature
import io.iohk.atala.prism.mercury.didpeer.DIDCommServicePeerDID
import io.iohk.atala.prism.mercury.didpeer.VerificationMaterialAgreement
import io.iohk.atala.prism.mercury.didpeer.VerificationMaterialAuthentication
import io.iohk.atala.prism.mercury.didpeer.VerificationMaterialFormatPeerDID
import io.iohk.atala.prism.mercury.didpeer.VerificationMethodTypeAgreement
import io.iohk.atala.prism.mercury.didpeer.VerificationMethodTypeAuthentication
import io.iohk.atala.prism.mercury.didpeer.core.toJsonElement
import io.iohk.atala.prism.mercury.didpeer.createPeerDIDNumalgo2
import io.iohk.atala.prism.protos.AtalaOperation
import io.iohk.atala.prism.protos.CreateDIDOperation
import io.iohk.atala.prism.protos.Service
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import pbandk.encodeToByteArray

open class CastorImpl : Castor {
    private val apollo: Apollo
    private var resolvers: Array<DIDResolver>

    constructor(
        apollo: Apollo? = null,
    ) {
        this.apollo = apollo ?: ApolloImpl()
        this.resolvers = arrayOf(
            PeerDIDResolver(),
            LongFormPrismDIDResolver(this.apollo)
        )
    }

    override fun parseDID(did: String): DID {
        return DIDParser.parse(did)
    }

    override fun createPrismDID(masterPublicKey: PublicKey, services: Array<DIDDocument.Service>?): DID {

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
            schema = "did",
            method = "prism",
            methodId = methodSpecificId.toString()
        )
    }

    @Throws(CastorError.InvalidKeyError::class)
    override fun createPeerDID(
        keyPairs: Array<KeyPair>,
        services: Array<DIDDocument.Service>
    ): DID {
        var encryptionKeys: MutableList<VerificationMaterialAgreement> = mutableListOf()
        var signingKeys: MutableList<VerificationMaterialAuthentication> = mutableListOf()

        keyPairs.forEach {
            if (it.keyCurve == null) {
                throw CastorError.InvalidKeyError()
            } else {
                when (it.keyCurve!!.curve) {
                    Curve.X25519 -> {
                        encryptionKeys.add(
                            VerificationMaterialAgreement(
                                format = VerificationMaterialFormatPeerDID.MULTIBASE,
                                value = it.publicKey.value.decodeToString(),
                                type = VerificationMethodTypeAgreement.X25519_KEY_AGREEMENT_KEY_2020
                            )
                        )
                    }

                    Curve.ED25519 -> {
                        signingKeys.add(
                            VerificationMaterialAuthentication(
                                format = VerificationMaterialFormatPeerDID.MULTIBASE,
                                value = it.publicKey.value.decodeToString(),
                                type = VerificationMethodTypeAuthentication.ED25519_VERIFICATION_KEY_2020
                            )
                        )
                    }

                    else -> {
                        throw CastorError.InvalidKeyError()
                    }
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
                        accept = it.serviceEndpoint.accept?.asList() ?: listOf()
                    ).toDict().toJsonElement()
                )
            }.first()
        )

        return DIDParser.parse(peerDID)
    }

    @Throws(CastorError.NotPossibleToResolveDID::class)
    override suspend fun resolveDID(did: String): DIDDocument {
        val parsedDID = DIDParser.parse(did)
        val resolver = resolvers.find { it.method == parsedDID.method } ?: throw CastorError.NotPossibleToResolveDID()
        return resolver.resolve(did)
    }

    override suspend fun verifySignature(did: DID, challenge: ByteArray, signature: ByteArray): Boolean {
        val document = resolveDID(did.toString())
        val keyPairs: MutableList<PublicKey> = mutableListOf()

        document.coreProperties
            .filterIsInstance<DIDDocument.Authentication>()
            .flatMap { it.verificationMethods.toList() }
            .mapNotNull {
                it.publicKeyMultibase?.let { publicKey ->
                    keyPairs.add(
                        PublicKey(
                            curve = KeyCurve(getCurveByType(it.type)),
                            value = publicKey.encodeToByteArray()
                        )
                    )
                }
            }

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
