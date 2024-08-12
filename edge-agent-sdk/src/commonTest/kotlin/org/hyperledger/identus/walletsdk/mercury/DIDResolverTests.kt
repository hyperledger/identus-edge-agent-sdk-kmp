package org.hyperledger.identus.walletsdk.mercury

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.didcommx.didcomm.common.VerificationMaterial
import org.didcommx.didcomm.common.VerificationMaterialFormat
import org.didcommx.didcomm.common.VerificationMethodType
import org.didcommx.didcomm.diddoc.DIDCommService
import org.didcommx.didcomm.diddoc.VerificationMethod
import org.hyperledger.identus.walletsdk.domain.DIDCOMM_MESSAGING
import org.hyperledger.identus.walletsdk.domain.models.Curve
import org.hyperledger.identus.walletsdk.domain.models.DID
import org.hyperledger.identus.walletsdk.domain.models.DIDDocument
import org.hyperledger.identus.walletsdk.domain.models.DIDUrl
import org.hyperledger.identus.walletsdk.domain.models.OctetPublicKey
import org.hyperledger.identus.walletsdk.mercury.resolvers.DIDCommDIDResolver
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class DIDResolverTests {
    lateinit var castorMock: CastorMock
    lateinit var sut: DIDCommDIDResolver

    @BeforeTest
    fun setup() {
        castorMock = CastorMock()
        sut = DIDCommDIDResolver(castorMock)
    }

    @Test
    fun testResolve_shouldTransformDomainDIDDOcumentToDIDCommDIDDoc() = runTest {
        val idDid = DID("did", "prism", "123")
        val allAuthentication = DIDDocument.Authentication(arrayOf(), arrayOf())
        val resolveDIDReturn = DIDDocument(idDid, arrayOf(allAuthentication))
        castorMock.resolveDIDReturn = resolveDIDReturn

        val result = sut.resolve("didString")
        val didDoc = result.get()

        assertNotNull(didDoc)
        assertEquals(didDoc.did, resolveDIDReturn.id.toString())
    }

    @Test
    fun testResolve_shouldTransformServices() = runTest {
        val idDid = DID("did", "prism", "2")
        val seAccept = arrayOf("someAccepts")
        val seRoutingKeys = arrayOf("someRoutingKey")
        val service = DIDDocument.Service(
            id = "DIDCommV2",
            type = arrayOf(DIDCOMM_MESSAGING),
            serviceEndpoint = DIDDocument.ServiceEndpoint(
                uri = "localhost:8082",
                accept = seAccept,
                routingKeys = seRoutingKeys
            )
        )

        val allAuthentication = DIDDocument.Authentication(arrayOf(), arrayOf())
        val resolveDIDReturn = DIDDocument(idDid, arrayOf(service, allAuthentication))
        castorMock.resolveDIDReturn = resolveDIDReturn

        val result = sut.resolve("didString")
        val didDoc = result.get()

        assertContains(
            didDoc.didCommServices,
            DIDCommService(
                id = service.id,
                serviceEndpoint = service.serviceEndpoint.uri,
                routingKeys = seRoutingKeys.toList(),
                accept = seAccept.toList()
            )
        )
    }

    @Test
    fun testResolve_shouldTransformVerificationMethods() = runTest {
        val idDid = DID("did", "prism", "123")

        val vmAuthentication = DIDDocument.VerificationMethod(
            id = DIDUrl(DID("2", "1", "0")),
            controller = DID("2", "2", "0"),
            type = Curve.ED25519.value,
            publicKeyJwk = mapOf("crv" to Curve.ED25519.value, "x" to "")
        )

        val vmKeyAgreement = DIDDocument.VerificationMethod(
            id = DIDUrl(DID("3", "1", "0")),
            controller = DID("3", "2", "0"),
            type = Curve.X25519.value,
            publicKeyJwk = mapOf("crv" to Curve.X25519.value, "x" to "")
        )

        val allAuthentication = DIDDocument.Authentication(
            arrayOf(),
            arrayOf(vmAuthentication, vmKeyAgreement)
        )
        val resolveDIDReturn = DIDDocument(idDid, arrayOf(allAuthentication))
        castorMock.resolveDIDReturn = resolveDIDReturn

        val result = sut.resolve("didString")
        val didDoc = result.get()

        assertContains(didDoc.authentications, vmAuthentication.id.string())
        var publicJwk = vmAuthentication.publicKeyJwk
        assertNotNull(publicJwk)
        var publicJwkCrv = publicJwk[CRV]
        var publicJwkX = publicJwk[X]
        assertNotNull(publicJwkCrv)
        assertNotNull(publicJwkX)
        assertContains(
            didDoc.verificationMethods,
            VerificationMethod(
                id = vmAuthentication.id.string(),
                controller = vmAuthentication.controller.toString(),
                type = VerificationMethodType.JSON_WEB_KEY_2020,
                verificationMaterial = VerificationMaterial(
                    VerificationMaterialFormat.JWK,
                    Json.encodeToString(OctetPublicKey(crv = publicJwkCrv, x = publicJwkX))
                )
            )
        )

        assertContains(didDoc.keyAgreements, vmKeyAgreement.id.string())
        publicJwk = vmKeyAgreement.publicKeyJwk
        assertNotNull(publicJwk)
        publicJwkCrv = publicJwk[CRV]
        publicJwkX = publicJwk[X]
        assertNotNull(publicJwkCrv)
        assertNotNull(publicJwkX)
        assertContains(
            didDoc.verificationMethods,
            VerificationMethod(
                id = vmKeyAgreement.id.string(),
                controller = vmKeyAgreement.controller.toString(),
                type = VerificationMethodType.JSON_WEB_KEY_2020,
                verificationMaterial = VerificationMaterial(
                    VerificationMaterialFormat.JWK,
                    Json.encodeToString(
                        OctetPublicKey(
                            crv = publicJwkCrv,
                            x = publicJwkX
                        )
                    )
                )
            )
        )
    }
}
