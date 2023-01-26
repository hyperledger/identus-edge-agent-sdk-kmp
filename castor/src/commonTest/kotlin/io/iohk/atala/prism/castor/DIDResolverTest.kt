package io.iohk.atala.prism.castor

import io.iohk.atala.prism.domain.models.DIDDocument
import io.iohk.atala.prism.mercury.didpeer.VerificationMethodTypeAgreement
import io.iohk.atala.prism.mercury.didpeer.VerificationMethodTypeAuthentication
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class DIDResolverTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun it_should_resolve_valid_dids() = runTest {
        val didExample =
            "did:peer:2.Ez6LSci5EK4Ezue5QA72ZX71QUbXY2xr5ygRw7wM1WJigTNnd.Vz6MkqgCXHEGr2wJZANPZGC8WFmeVuS3abAD9uvh7mTXygCFv.SeyJ0IjoiZG0iLCJzIjoibG9jYWxob3N0OjgwODIiLCJyIjpbXSwiYSI6WyJkaWRjb21tL3YyIl19"
        val castor = CastorImpl()
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
                                VerificationMethodTypeAuthentication.ED25519_VERIFICATION_KEY_2020.value
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
                                VerificationMethodTypeAgreement.X25519_KEY_AGREEMENT_KEY_2020.value
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
