package org.hyperledger.identus.walletsdk.castor

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import kotlinx.coroutines.test.runTest
import org.didcommx.didcomm.common.Typ
import org.hyperledger.identus.walletsdk.castor.resolvers.PrismDIDApiResolver
import org.hyperledger.identus.walletsdk.domain.buildingblocks.Apollo
import org.hyperledger.identus.walletsdk.domain.models.Api
import org.hyperledger.identus.walletsdk.domain.models.DIDDocument
import org.hyperledger.identus.walletsdk.domain.models.HttpResponse
import org.hyperledger.identus.walletsdk.domain.models.KeyValue
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.kotlin.mock
import kotlin.test.assertEquals

class PrismDIDApiResolverTest {

    @Test
    fun testPrismDidApiResolver_whenResolveDid_thenDidDocumentCorrect() = runTest {
        val mockApollo = mock<Apollo>()
        val mockApi = mock<Api>()
        val json = getDidDocumentJson()

        val did = "did:prism:604ba1764ab89993f9a74625cc4f3e04737919639293eb382cc7adc53767f550"

        `when`(
            mockApi.request(
                HttpMethod.Get.value,
                "/dids/$did",
                emptyArray(),
                arrayOf(
                    KeyValue(HttpHeaders.ContentType, Typ.Encrypted.typ),
                    KeyValue(HttpHeaders.Accept, "*/*"),
                ),
                null
            )
        ).thenReturn(
            HttpResponse(
                200,
                json
            )
        )

        val prismDidApiResolver = PrismDIDApiResolver(
            apollo = mockApollo,
            cloudAgentUrl = "",
            api = mockApi
        )

        val didDoc = prismDidApiResolver.resolve(did)
        assertEquals("did:prism:607fffff16d079c8896e246613f9d10ab25f7c233830ff8a5c5fae2881c1c374", didDoc.id.toString())
        assertEquals(2, didDoc.coreProperties.size)
        assertEquals(DIDDocument.Authentication::class, didDoc.coreProperties[0]::class)
        assertEquals(DIDDocument.AssertionMethod::class, didDoc.coreProperties[1]::class)
    }

    fun getDidDocumentJson(): String {
        return """
            {
                "@context": "https://w3id.org/did-resolution/v1",
                "didDocument": {
                    "@context": [
                        "https://www.w3.org/ns/did/v1",
                        "https://w3id.org/security/suites/jws-2020/v1"
                    ],
                    "id": "did:prism:607fffff16d079c8896e246613f9d10ab25f7c233830ff8a5c5fae2881c1c374",
                    "controller": "did:prism:607fffff16d079c8896e246613f9d10ab25f7c233830ff8a5c5fae2881c1c374",
                    "verificationMethod": [
                        {
                            "id": "did:prism:607fffff16d079c8896e246613f9d10ab25f7c233830ff8a5c5fae2881c1c374#auth-1",
                            "type": "Ed25519",
                            "controller": "did:prism:607fffff16d079c8896e246613f9d10ab25f7c233830ff8a5c5fae2881c1c374",
                            "publicKeyJwk": {
                                "crv": "secp256k1",
                                "x": "4r3o5WTLuNKmcLW6pOL_32QIoWja6BdI0lf4nJSCRzM",
                                "y": "Ftj13x29r6oi8p7iy_4dY4u1U50PVFPnLuSputu3r9A",
                                "kty": "EC"
                            }
                        },
                        {
                            "id": "did:prism:607fffff16d079c8896e246613f9d10ab25f7c233830ff8a5c5fae2881c1c374#issue-1",
                            "type": "JsonWebKey2020",
                            "controller": "did:prism:607fffff16d079c8896e246613f9d10ab25f7c233830ff8a5c5fae2881c1c374",
                            "publicKeyJwk": {
                                "crv": "secp256k1",
                                "x": "-kTW3jhiqFwVHVarQ3i6M-jky1Q495xDe1pRbUgKXtc",
                                "y": "KDu2pjv2OopYvS30X2mPxnAYoAcivZ0y_iQu_SbAasQ",
                                "kty": "EC"
                            }
                        }
                    ],
                    "authentication": [
                        "did:prism:607fffff16d079c8896e246613f9d10ab25f7c233830ff8a5c5fae2881c1c374#auth-1"
                    ],
                    "assertionMethod": [
                        "did:prism:607fffff16d079c8896e246613f9d10ab25f7c233830ff8a5c5fae2881c1c374#issue-1"
                    ],
                    "keyAgreement": [],
                    "capabilityInvocation": [],
                    "capabilityDelegation": [],
                    "service": []
                },
                "didDocumentMetadata": {
                    "deactivated": false,
                    "versionId": "607fffff16d079c8896e246613f9d10ab25f7c233830ff8a5c5fae2881c1c374",
                    "created": "2024-03-13T20:05:15Z",
                    "updated": "2024-03-13T20:05:15Z"
                },
                "didResolutionMetadata": {
                    "contentType": "application/ld+json; profile=https://w3id.org/did-resolution"
                }
            }
        """.trimIndent()
    }
}
