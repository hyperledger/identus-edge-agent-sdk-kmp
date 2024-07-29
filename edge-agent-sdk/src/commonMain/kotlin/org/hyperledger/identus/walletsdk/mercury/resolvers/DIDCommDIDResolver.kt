package org.hyperledger.identus.walletsdk.mercury.resolvers

import io.iohk.atala.prism.didcomm.didpeer.core.fromMulticodec
import io.iohk.atala.prism.didcomm.didpeer.core.toJwk
import io.iohk.atala.prism.didcomm.didpeer.multibase.MultiBase
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
import org.hyperledger.identus.walletsdk.domain.DIDCOMM_MESSAGING
import org.hyperledger.identus.walletsdk.domain.buildingblocks.Castor
import org.hyperledger.identus.walletsdk.domain.models.CastorError
import org.hyperledger.identus.walletsdk.domain.models.Curve
import org.hyperledger.identus.walletsdk.domain.models.DIDDocument
import org.hyperledger.identus.walletsdk.domain.models.OctetPublicKey
import org.hyperledger.identus.walletsdk.mercury.CRV
import org.hyperledger.identus.walletsdk.mercury.MULTIBASE_BYTES_SIZE
import org.hyperledger.identus.walletsdk.mercury.X
import java.util.*

/**
 * A resolver that resolves a Decentralized Identifier (DID) to its corresponding DID Document.
 *
 * @param castor The instance of Castor used to resolve DIDs.
 */
class DIDCommDIDResolver(val castor: Castor) : DIDDocResolver {
    /**
     * Resolves a DID to its corresponding DID Document.
     *
     * @param did The DID to resolve.
     * @return An optional containing the DID Document associated with the DID, or an empty optional if the document cannot be retrieved.
     * @throws [CastorError.InvalidJWKKeysError] if the JWK keys are not in a valid format.
     */
    @Throws(CastorError.InvalidJWKKeysError::class)
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
                        if (keyBytes.size == MULTIBASE_BYTES_SIZE) {
                            keyBytes = fromMulticodec(keyBytes).second
                        }
                        publicKeyJWK = when (curve) {
                            Curve.ED25519 -> {
                                toJwk(
                                    keyBytes,
                                    io.iohk.atala.prism.didcomm.didpeer.VerificationMethodTypeAuthentication.JsonWebKey2020
                                )
                            }

                            Curve.X25519 -> {
                                toJwk(
                                    keyBytes,
                                    io.iohk.atala.prism.didcomm.didpeer.VerificationMethodTypeAgreement.JsonWebKey2020
                                )
                            }

                            else -> throw RuntimeException("")
                        }
                    } else if (method.publicKeyJwk != null) {
                        publicKeyJWK = method.publicKeyJwk
                    } else {
                        throw RuntimeException("")
                    }
                    publicKeyJWK[CRV]?.let { crv ->
                        publicKeyJWK[X]?.let { x ->
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

                if (coreProperty is DIDDocument.Service && coreProperty.type.contains(DIDCOMM_MESSAGING)) {
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
