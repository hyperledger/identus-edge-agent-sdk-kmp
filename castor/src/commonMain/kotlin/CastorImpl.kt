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

    override fun createPeerDID(
        keyAgreementKeyPair: KeyPair,
        authenticationKeyPair: KeyPair,
        services: Array<DIDDocument.Service>
    ): DID {
        if (keyAgreementKeyPair.curve != KeyCurve.ED25519 ||
            authenticationKeyPair.curve != KeyCurve.X25519 ||
            keyAgreementKeyPair.publicKey.curve != KeyCurve.ED25519 ||
            authenticationKeyPair.publicKey.curve != KeyCurve.X25519 ||
            keyAgreementKeyPair.privateKey.curve != KeyCurve.ED25519 ||
            authenticationKeyPair.privateKey.curve != KeyCurve.X25519
        ) {
            throw CastorError.InvalidKeyError()
        }
        val peerDID = createPeerDIDNumalgo2(
            encryptionKeys = listOf(
                VerificationMaterialAgreement(
                    format = VerificationMaterialFormatPeerDID.MULTIBASE,
                    value = keyAgreementKeyPair.publicKey.value,
                    type = VerificationMethodTypeAgreement.X25519_KEY_AGREEMENT_KEY_2020
                )
            ),
            signingKeys = listOf(
                VerificationMaterialAuthentication(
                    format = VerificationMaterialFormatPeerDID.MULTIBASE,
                    value = authenticationKeyPair.publicKey.value,
                    type = VerificationMethodTypeAuthentication.ED25519_VERIFICATION_KEY_2020
                )
            ),
            service = if (services.isNotEmpty()) {
                Json.encodeToString(
                    DIDCommServicePeerDID(
                        id = services[0].id,
                        type = services[0].type[0],
                        serviceEndpoint = services[0].serviceEndpoint.uri,
                        routingKeys = listOf(),
                        accept = services[0].serviceEndpoint.accept?.asList() ?: listOf()
                    ).toDict().toJsonElement()
                )
            } else null
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

    override fun getEcnumbasis(did: DID, keyPair: KeyPair): String {
        TODO("Not yet implemented")
    }
}
