package io.iohk.atala.prism.walletsdk.castor.shared

import io.iohk.atala.prism.mercury.didpeer.DIDCommServicePeerDID
import io.iohk.atala.prism.mercury.didpeer.VerificationMaterialAgreement
import io.iohk.atala.prism.mercury.didpeer.VerificationMaterialAuthentication
import io.iohk.atala.prism.mercury.didpeer.VerificationMaterialFormatPeerDID
import io.iohk.atala.prism.mercury.didpeer.VerificationMethodTypeAgreement
import io.iohk.atala.prism.mercury.didpeer.VerificationMethodTypeAuthentication
import io.iohk.atala.prism.mercury.didpeer.core.toJsonElement
import io.iohk.atala.prism.mercury.didpeer.createPeerDIDNumalgo2
import io.iohk.atala.prism.walletsdk.castor.did.DIDParser
import io.iohk.atala.prism.walletsdk.domain.models.CastorError
import io.iohk.atala.prism.walletsdk.domain.models.Curve
import io.iohk.atala.prism.walletsdk.domain.models.DID
import io.iohk.atala.prism.walletsdk.domain.models.DIDDocument
import io.iohk.atala.prism.walletsdk.domain.models.KeyPair
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

internal fun CreatePeerDID(
    keyPairs: Array<KeyPair>,
    services: Array<DIDDocument.Service>
): DID {
    val encryptionKeys: MutableList<VerificationMaterialAgreement> = mutableListOf()
    val signingKeys: MutableList<VerificationMaterialAuthentication> = mutableListOf()

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
