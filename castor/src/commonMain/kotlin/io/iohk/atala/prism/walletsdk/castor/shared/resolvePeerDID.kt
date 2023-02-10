package io.iohk.atala.prism.walletsdk.castor.shared

import io.iohk.atala.prism.mercury.didpeer.DIDCommServicePeerDID
import io.iohk.atala.prism.mercury.didpeer.DIDDocPeerDID
import io.iohk.atala.prism.mercury.didpeer.MalformedPeerDIDException
import io.iohk.atala.prism.mercury.didpeer.VerificationMaterialFormatPeerDID
import io.iohk.atala.prism.mercury.didpeer.VerificationMethodPeerDID
import io.iohk.atala.prism.mercury.didpeer.VerificationMethodTypeAgreement
import io.iohk.atala.prism.mercury.didpeer.VerificationMethodTypeAuthentication
import io.iohk.atala.prism.mercury.didpeer.resolvePeerDID
import io.iohk.atala.prism.walletsdk.castor.did.DIDParser
import io.iohk.atala.prism.walletsdk.castor.did.DIDUrlParser
import io.iohk.atala.prism.walletsdk.domain.models.CastorError
import io.iohk.atala.prism.walletsdk.domain.models.Curve
import io.iohk.atala.prism.walletsdk.domain.models.DIDDocument
import io.iohk.atala.prism.walletsdk.domain.models.DIDDocumentCoreProperty
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

suspend fun resolvePeerDID(didString: String): DIDDocument {
    val peerDIDDocument = try {
        DIDDocPeerDID.fromJson(resolvePeerDID(didString))
    } catch (e: MalformedPeerDIDException) {
        throw CastorError.InvalidPeerDIDError()
    } catch (e: Throwable) {
        throw CastorError.NotPossibleToResolveDID()
    }

    val coreProperties: MutableList<DIDDocumentCoreProperty> = mutableListOf()

    coreProperties.add(
        DIDDocument.Authentication(
            urls = arrayOf(didString),
            verificationMethods = peerDIDDocument.authentication.map {
                fromVerificationMethodPeerDID(didString, it)
            }.toTypedArray()
        )
    )
    coreProperties.add(
        DIDDocument.KeyAgreement(
            urls = arrayOf(didString),
            verificationMethods = peerDIDDocument.keyAgreement.map {
                fromVerificationMethodPeerDID(didString, it)
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
                            uri = service.serviceEndpoint,
                            accept = service.accept.toTypedArray(),
                            routingKeys = service.routingKeys.toTypedArray()
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

internal fun fromVerificationMethodPeerDID(
    did: String,
    verificationMethod: VerificationMethodPeerDID
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
