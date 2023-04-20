package io.iohk.atala.prism.walletsdk.mercury

import io.iohk.atala.prism.walletsdk.domain.models.Curve
import io.iohk.atala.prism.walletsdk.domain.models.DID
import io.iohk.atala.prism.walletsdk.domain.models.DIDDocument
import io.iohk.atala.prism.walletsdk.domain.models.DIDUrl
import io.iohk.atala.prism.walletsdk.mercury.resolvers.DIDCommDIDResolver
import kotlinx.coroutines.test.runTest
import org.didcommx.didcomm.common.VerificationMaterial
import org.didcommx.didcomm.common.VerificationMaterialFormat
import org.didcommx.didcomm.common.VerificationMethodType
import org.didcommx.didcomm.diddoc.DIDCommService
import org.didcommx.didcomm.diddoc.VerificationMethod
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
            type = arrayOf("DIDCommMessaging"),
            serviceEndpoint = DIDDocument.ServiceEndpoint(
                uri = "localhost:8082",
                accept = seAccept,
                routingKeys = seRoutingKeys,
            ),
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
            type = "type-auth",
            publicKeyJwk = mapOf("crv" to Curve.ED25519.value)
        )

        val vmKeyAgreement = DIDDocument.VerificationMethod(
            id = DIDUrl(DID("3", "1", "0")),
            controller = DID("3", "2", "0"),
            type = "type-keyAgree",
            publicKeyJwk = mapOf("crv" to Curve.X25519.value)
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
        assertContains(
            didDoc.verificationMethods,
            VerificationMethod(
                id = vmAuthentication.id.string(),
                controller = vmAuthentication.controller.toString(),
                type = VerificationMethodType.JSON_WEB_KEY_2020,
                verificationMaterial = VerificationMaterial(
                    VerificationMaterialFormat.JWK,
                    vmAuthentication.publicKeyJwk.toString()
                )
            )
        )

        assertContains(didDoc.keyAgreements, vmKeyAgreement.id.string())
        assertContains(
            didDoc.verificationMethods,
            VerificationMethod(
                id = vmKeyAgreement.id.string(),
                controller = vmKeyAgreement.controller.toString(),
                type = VerificationMethodType.JSON_WEB_KEY_2020,
                verificationMaterial = VerificationMaterial(
                    VerificationMaterialFormat.JWK,
                    vmKeyAgreement.publicKeyJwk.toString()
                )
            )
        )
    }
}
