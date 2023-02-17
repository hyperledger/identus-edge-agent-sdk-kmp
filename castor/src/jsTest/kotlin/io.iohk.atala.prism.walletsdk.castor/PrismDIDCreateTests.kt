package io.iohk.atala.prism.walletsdk.castor

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.await
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class PrismDIDCreateTests {

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun it_should_create_PrismDIDs() = runTest {
        val mock = ApolloMock()
        val didExample =
            "did:prism:a7bacdc91c264066f5858ae3c2e8a159982e8292dc4bf94e58ef8dd982ea9f38:ChwKGhIYCgdtYXN0ZXIwEAFKCwoJc2VjcDI1Nmsx"

        val castor = CastorImpl(mock)
        val resolvedDID = castor.resolveDID(didExample).await()
        var masterPublicKey = mock.compressedPublicKey(
            ByteArray(2),
        ).uncompressed

        val createdDID = castor.resolveDID(castor.createPrismDID(masterPublicKey).toString()).await()
        assertEquals(resolvedDID.id.toString(), createdDID.id.toString())
    }
}
