package io.iohk.atala.prism.walletsdk.mercury.resolvers

import io.iohk.atala.prism.walletsdk.domain.buildingBlocks.Castor
import io.iohk.atala.prism.walletsdk.domain.models.Curve
import io.iohk.atala.prism.walletsdk.domain.models.DIDDocument
import kotlinx.coroutines.runBlocking
import org.didcommx.didcomm.common.VerificationMaterial
import org.didcommx.didcomm.common.VerificationMaterialFormat
import org.didcommx.didcomm.common.VerificationMethodType
import org.didcommx.didcomm.diddoc.DIDCommService
import org.didcommx.didcomm.diddoc.DIDDoc
import org.didcommx.didcomm.diddoc.DIDDocResolver
import org.didcommx.didcomm.diddoc.VerificationMethod
import java.util.*

class DIDCommDIDResolver(val castor: Castor): DIDDocResolver {
    override fun resolve(did: String): Optional<DIDDoc> {
        return runBlocking {
            val doc = castor.resolveDID(did)
            val authentications = mutableListOf<String>()
            val keyAgreements = mutableListOf<String>()
            val services = mutableListOf<DIDCommService>()
            val verificationMethods = mutableListOf<VerificationMethod>()

            doc.coreProperties.forEach { coreProperty ->
                val methods = when(coreProperty) {
                    is DIDDocument.Authentication -> coreProperty.verificationMethods
                    is DIDDocument.AssertionMethod -> coreProperty.verificationMethods
                    is DIDDocument.KeyAgreement -> coreProperty.verificationMethods
                    is DIDDocument.CapabilityInvocation -> coreProperty.verificationMethods
                    is DIDDocument.CapabilityDelegation -> coreProperty.verificationMethods
                    else -> emptyArray()
                }

                methods.forEach { method ->
                    val curve = DIDDocument.VerificationMethod.getCurveByType(method.publicKeyJwk?.get("crv")!!)

                    if(curve === Curve.ED25519) {
                        authentications.add(method.id.string())
                    }

                    if(curve === Curve.X25519) {
                        keyAgreements.add(method.id.string())
                    }

                    verificationMethods.add(
                        VerificationMethod(
                            id = method.id.string(),
                            controller = method.controller.toString(),
                            type = VerificationMethodType.JSON_WEB_KEY_2020,
                            verificationMaterial = VerificationMaterial(VerificationMaterialFormat.JWK, method.publicKeyJwk.toString() ?: "")
                        )
                    )
                }

                if(coreProperty is DIDDocument.Service && coreProperty.type.contains("DIDCommMessaging")) {
                    services.add(
                        DIDCommService(
                            id = coreProperty.id,
                            serviceEndpoint = coreProperty.serviceEndpoint.uri,
                            routingKeys = coreProperty.serviceEndpoint.routingKeys?.toList() ?: emptyList(),
                            accept = coreProperty.serviceEndpoint.accept?.toList() ?: emptyList()
                        )
                    )
                }
            }

            Optional.of(DIDDoc(
                doc.id.toString(),
                keyAgreements,
                authentications,
                verificationMethods,
                services
            ))
        }
    }
}