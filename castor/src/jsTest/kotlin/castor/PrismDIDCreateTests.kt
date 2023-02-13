package io.iohk.atala.prism.walletsdk.castor

import io.iohk.atala.prism.walletsdk.castor.did.prismdid.toProto
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import pbandk.encodeToByteArray
import kotlin.test.Test

class PrismDIDCreateTests {

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun it_should_create_PrismDIDs() = runTest {
        val mock = ApolloMock()

        var masterPublicKey = mock.compressedPublicKey(
            ByteArray(2),
        ).toProto()

        masterPublicKey.encodeToByteArray()
    }
}
