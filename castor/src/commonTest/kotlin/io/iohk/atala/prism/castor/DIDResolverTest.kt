import io.iohk.atala.prism.apollo.ApolloMock
import io.iohk.atala.prism.castor.CastorImpl
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
        val didExample =
            "did:prism:b6c0c33d701ac1b9a262a14454d1bbde3d127d697a76950963c5fd930605:Cj8KPRI7CgdtYXN0ZXIwEAFKLgoJc2VmsxEiECSTjyV7sUfCr_ArpN9rvCwR9fRMAhcsr_S7ZRiJk4p5k"
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
}
