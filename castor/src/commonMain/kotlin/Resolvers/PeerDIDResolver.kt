package io.iohk.atala.prism.castor

import io.iohk.atala.prism.domain.models.DID
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
    override suspend fun resolve(did: DID): DIDDocument {
        val coreProperties: MutableList<DIDDocumentCoreProperty> = mutableListOf()
        val peerDIDDocument = DIDDocPeerDID.fromJson(resolvePeerDID(did.toString()))
        coreProperties.add(
            DIDDocument.Authentication(
                urls = arrayOf(did.toString()),
                verificationMethods = peerDIDDocument.authentication.map {
                    fromVerificationMethodPeerDID(did.toString(), it)
                }.toTypedArray()
            )
        )
        coreProperties.add(
            DIDDocument.KeyAgreement(
                urls = arrayOf(did.toString()),
                verificationMethods = peerDIDDocument.keyAgreement.map {
                    fromVerificationMethodPeerDID(did.toString(), it)
                }.toTypedArray()
            )
        )
        if (!peerDIDDocument.service.isNullOrEmpty()) {
            coreProperties.add(
                DIDDocument.Services(
                    peerDIDDocument.service!!.filterIsInstance<DIDCommServicePeerDID>().map {
                        DIDDocument.Service(
                            id = it.id,
                            type = arrayOf(it.type),
                            serviceEndpoint = DIDDocument.ServiceEndpoint(
                                uri = it.serviceEndpoint,
                                accept = it.accept.toTypedArray(),
                                routingKeys = it.routingKeys.toTypedArray()
                            )
                        )
                    }.toTypedArray()
                )
            )
        }
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
