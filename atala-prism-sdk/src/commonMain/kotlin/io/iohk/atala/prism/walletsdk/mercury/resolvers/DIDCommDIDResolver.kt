package io.iohk.atala.prism.walletsdk.mercury.resolvers

import io.iohk.atala.prism.apollo.multibase.MultiBase
import io.iohk.atala.prism.didcomm.didpeer.core.fromMulticodec
import io.iohk.atala.prism.didcomm.didpeer.core.toJwk
import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Castor
import io.iohk.atala.prism.walletsdk.domain.models.CastorError
import io.iohk.atala.prism.walletsdk.domain.models.Curve
import io.iohk.atala.prism.walletsdk.domain.models.DIDDocument
import io.iohk.atala.prism.walletsdk.domain.models.OctetPublicKey
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.didcommx.didcomm.common.VerificationMaterial
import org.didcommx.didcomm.common.VerificationMaterialFormat
import org.didcommx.didcomm.common.VerificationMethodType
import org.didcommx.didcomm.diddoc.DIDCommService
import org.didcommx.didcomm.diddoc.DIDDoc
import org.didcommx.didcomm.diddoc.DIDDocResolver
import org.didcommx.didcomm.diddoc.VerificationMethod
import java.util.Optional

class DIDCommDIDResolver(val castor: Castor) : DIDDocResolver {
    override fun resolve(did: String): Optional<DIDDoc> {
        return runBlocking {
            val doc = castor.resolveDID(did)
            val authentications = mutableListOf<String>()
            val keyAgreements = mutableListOf<String>()
            val services = mutableListOf<DIDCommService>()
            val verificationMethods = mutableListOf<VerificationMethod>()

            doc.coreProperties.forEach { coreProperty ->
                val methods = when (coreProperty) {
                    is DIDDocument.Authentication -> coreProperty.verificationMethods
                    is DIDDocument.AssertionMethod -> coreProperty.verificationMethods
                    is DIDDocument.KeyAgreement -> coreProperty.verificationMethods
                    is DIDDocument.CapabilityInvocation -> coreProperty.verificationMethods
                    is DIDDocument.CapabilityDelegation -> coreProperty.verificationMethods
                    else -> emptyArray()
                }

                methods.forEach { method ->
                    val curve = DIDDocument.VerificationMethod.getCurveByType(method.type)

                    if (curve === Curve.ED25519) {
                        authentications.add(method.id.string())
                    }

                    if (curve === Curve.X25519) {
                        keyAgreements.add(method.id.string())
                    }

                    // In this method we need to send th key as JWK while sometimes we get it as
                    // MultiBase, therefore the following lines of code convert it
                    val publicKeyJWK: Map<String, String>
                    if (method.publicKeyMultibase != null) {
                        var keyBytes = MultiBase.decode(method.publicKeyMultibase)
                        // In the case of MultiBase make sure to remove the MultiCodec from the ByteArray
                        if (keyBytes.size == 34) {
                            keyBytes = fromMulticodec(keyBytes).second
                        }
                        publicKeyJWK = when (curve) {
                            Curve.ED25519 -> {
                                toJwk(
                                    keyBytes,
                                    io.iohk.atala.prism.didcomm.didpeer.VerificationMethodTypeAuthentication.JSON_WEB_KEY_2020
                                )
                            }

                            Curve.X25519 -> {
                                toJwk(
                                    keyBytes,
                                    io.iohk.atala.prism.didcomm.didpeer.VerificationMethodTypeAgreement.JSON_WEB_KEY_2020
                                )
                            }

                            else -> throw RuntimeException("")
                        }
                    } else if (method.publicKeyJwk != null) {
                        publicKeyJWK = method.publicKeyJwk
                    } else {
                        throw RuntimeException("")
                    }
                    publicKeyJWK["crv"]?.let { crv ->
                        publicKeyJWK["x"]?.let { x ->
                            verificationMethods.add(
                                VerificationMethod(
                                    id = method.id.string(),
                                    controller = method.controller.toString(),
                                    type = VerificationMethodType.JSON_WEB_KEY_2020,
                                    verificationMaterial = VerificationMaterial(
                                        VerificationMaterialFormat.JWK,
                                        Json.encodeToString(
                                            OctetPublicKey(
                                                crv = crv,
                                                x = x
                                            )
                                        )
                                    )
                                )
                            )
                        } ?: throw CastorError.InvalidJWKKeysError()
                    } ?: throw CastorError.InvalidJWKKeysError()
                }

                if (coreProperty is DIDDocument.Service && coreProperty.type.contains("DIDCommMessaging")) {
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

            Optional.of(
                DIDDoc(
                    doc.id.toString(),
                    keyAgreements,
                    authentications,
                    verificationMethods,
                    services
                )
            )
        }
    }
}
