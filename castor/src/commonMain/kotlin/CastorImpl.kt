package io.iohk.atala.prism.castor

import io.iohk.atala.prism.domain.buildingBlocks.Castor
import io.iohk.atala.prism.domain.models.CastorError
import io.iohk.atala.prism.domain.models.DID
import io.iohk.atala.prism.domain.models.DIDDocument
import io.iohk.atala.prism.domain.models.DIDResolver
import io.iohk.atala.prism.domain.models.KeyCurve
import io.iohk.atala.prism.domain.models.KeyPair
import io.iohk.atala.prism.domain.models.PublicKey
import io.iohk.atala.prism.mercury.didpeer.DIDCommServicePeerDID
import io.iohk.atala.prism.mercury.didpeer.VerificationMaterialAgreement
import io.iohk.atala.prism.mercury.didpeer.VerificationMaterialAuthentication
import io.iohk.atala.prism.mercury.didpeer.VerificationMaterialFormatPeerDID
import io.iohk.atala.prism.mercury.didpeer.VerificationMethodTypeAgreement
import io.iohk.atala.prism.mercury.didpeer.VerificationMethodTypeAuthentication
import io.iohk.atala.prism.mercury.didpeer.core.toJsonElement
import io.iohk.atala.prism.mercury.didpeer.createPeerDIDNumalgo2
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

open class CastorImpl : Castor {
    private var resolvers: Array<DIDResolver> = arrayOf(
        PeerDIDResolver()
    )

    override fun parseDID(did: String): DID {
        return DIDParser.parse(did)
    }

    override fun createPrismDID(masterPublicKey: PublicKey, services: Array<DIDDocument.Service>?): DID {
        TODO("Not yet implemented")
    }

    @Throws(CastorError.InvalidKeyError::class)
    override fun createPeerDID(
        keyPairs: Array<KeyPair>,
        services: Array<DIDDocument.Service>
    ): DID {
        var encryptionKeys: MutableList<VerificationMaterialAgreement> = mutableListOf()
        var signingKeys: MutableList<VerificationMaterialAuthentication> = mutableListOf()

        keyPairs.forEach {
            when (it.curve) {
                KeyCurve.ED25519 -> {
                    encryptionKeys.add(
                        VerificationMaterialAgreement(
                            format = VerificationMaterialFormatPeerDID.MULTIBASE,
                            value = it.publicKey.value.decodeToString(),
                            type = VerificationMethodTypeAgreement.X25519_KEY_AGREEMENT_KEY_2020
                        )
                    )
                }
                KeyCurve.X25519 -> {
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
        TODO("Not yet implemented")
    }
}
