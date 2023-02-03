package io.iohk.atala.prism.castor

import io.iohk.atala.prism.apollo.ApolloMock
import io.iohk.atala.prism.domain.models.Curve
import io.iohk.atala.prism.domain.models.DIDDocument
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class DIDResolverTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun it_should_resolve_valid_PeerDIDs() = runTest {
        val didExample =
            "did:peer:2.Ez6LSci5EK4Ezue5QA72ZX71QUbXY2xr5ygRw7wM1WJigTNnd.Vz6MkqgCXHEGr2wJZANPZGC8WFmeVuS3abAD9uvh7mTXygCFv.SeyJ0IjoiZG0iLCJzIjoibG9jYWxob3N0OjgwODIiLCJyIjpbXSwiYSI6WyJkaWRjb21tL3YyIl19"
        val castor = CastorImpl(ApolloMock())
        val response = castor.resolveDID(didExample)

        assertEquals(response.id.toString(), didExample)

        response.coreProperties.forEach { coreProperty ->
            when (coreProperty) {
                is DIDDocument.Authentication -> {
                    assertContentEquals(coreProperty.urls, arrayOf(didExample))
                    assertEquals(coreProperty.verificationMethods.size, 1)
                    coreProperty.verificationMethods.forEach { verificationMethod ->
                        run {
                            assertEquals(verificationMethod.id.did.toString(), didExample)
                            assertEquals(
                                verificationMethod.type,
                                Curve.ED25519.value
                            )
                        }
                    }
                }
                is DIDDocument.KeyAgreement -> {
                    assertContentEquals(coreProperty.urls, arrayOf(didExample))
                    assertEquals(coreProperty.verificationMethods.size, 1)
                    coreProperty.verificationMethods.forEach { verificationMethod ->
                        run {
                            assertEquals(verificationMethod.id.did.toString(), didExample)
                            assertEquals(
                                verificationMethod.type,
                                Curve.X25519.value
                            )
                        }
                    }
                }
                is DIDDocument.Services -> {
                    assertEquals(coreProperty.values.size, 1)
                    coreProperty.values.forEach { service ->
                        run {
                            assertContentEquals(service.type, arrayOf<String>("DIDCommMessaging"))
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun it_should_resolve_valid_PrismDIDs() = runTest {
        val mock = ApolloMock()
        val didExample =
            "did:prism:9b5118411248d9663b6ab15128fba8106511230ff654e7514cdcc4ce919bde9b:Cj8KPRI7CgdtYXN0ZXIwEAFKLgoJc2VjcDI1NmsxEiEDHpf-yhIns-LP3tLvA8icC5FJ1ZlBwbllPtIdNZ3q0jU"
        val castor = CastorImpl(mock)
        val response = castor.resolveDID(didExample)

        assertEquals(response.id.toString(), didExample)

        response.coreProperties.forEach { coreProperty ->
            when (coreProperty) {
                is DIDDocument.Authentication -> {
                    assertEquals(1, coreProperty.verificationMethods.size)
                    coreProperty.verificationMethods.forEach { verificationMethod ->
                        run {
                            assertEquals(verificationMethod.id.did.toString(), didExample)
                            assertEquals(
                                verificationMethod.type,
                                mock.createKeyPairReturn.keyCurve!!.curve.value
                            )
                            assertContentEquals(
                                verificationMethod.publicKeyMultibase!!.encodeToByteArray(),
                                mock.createKeyPairReturn.publicKey.value
                            )
                        }
                    }
                }
                is DIDDocument.Services -> {
                    assertEquals(coreProperty.values.size, 0)
                }
            }
        }
    }
}
