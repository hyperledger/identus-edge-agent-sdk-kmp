package io.iohk.atala.prism.castor.io.iohk.atala.prism.castor.resolvers

import io.iohk.atala.prism.castor.DIDParser
import io.iohk.atala.prism.castor.DIDUrlParser
import io.iohk.atala.prism.domain.models.DIDDocument
import io.iohk.atala.prism.domain.models.DIDDocumentCoreProperty
import io.iohk.atala.prism.domain.models.DIDResolver
import io.iohk.atala.prism.mercury.didpeer.DIDCommServicePeerDID
import io.iohk.atala.prism.mercury.didpeer.DIDDocPeerDID
import io.iohk.atala.prism.mercury.didpeer.VerificationMaterialFormatPeerDID
import io.iohk.atala.prism.mercury.didpeer.VerificationMethodPeerDID
import io.iohk.atala.prism.mercury.didpeer.resolvePeerDID
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class PeerDIDResolver() : DIDResolver {
    override val method: String = "peer"
    override suspend fun resolve(didString: String): DIDDocument {
        val did = DIDParser.parse(didString)
        val coreProperties: MutableList<DIDDocumentCoreProperty> = mutableListOf()
        val peerDIDDocument = DIDDocPeerDID.fromJson(resolvePeerDID(didString))

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

        var peerDIDServices = peerDIDDocument.service ?: listOf()
        var services: MutableList<DIDDocument.Service> = mutableListOf()

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
                // TODO() To add the OtherService and know which attributes we can extract from it.
            }
        }

        coreProperties.add(
            DIDDocument.Services(
                services.toTypedArray()
            )
        )

        return DIDDocument(
            id = did,
            coreProperties = coreProperties.toTypedArray()
        )
    }

    private fun fromVerificationMethodPeerDID(
        did: String,
        verificationMethod: VerificationMethodPeerDID
    ): DIDDocument.VerificationMethod {
        val didUrl = DIDUrlParser.parse(did)
        val controller = DIDParser.parse(verificationMethod.controller)
        val type = verificationMethod.verMaterial.type.value
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
}
