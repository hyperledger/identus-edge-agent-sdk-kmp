package io.iohk.atala.prism.walletsdk.castor

import io.iohk.atala.prism.protos.PublicKey
import io.iohk.atala.prism.walletsdk.castor.did.prismdid.PrismDIDPublicKey
import io.iohk.atala.prism.walletsdk.castor.did.prismdid.id
import io.iohk.atala.prism.walletsdk.domain.models.Curve
import io.iohk.atala.prism.walletsdk.domain.models.KeyCurve
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class PrismDIDPublicKeyTests {

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun it_should_parse_proto_toPrismDIDOublicKey() = runTest {
        val apollo = ApolloMock()
        val seed = apollo.createRandomSeed().seed
        val keyPair = apollo.createKeyPair(
            seed = seed,
            curve = KeyCurve(Curve.SECP256K1),
        )
        val publicKey = PrismDIDPublicKey(
            apollo = ApolloMock(),
            id = PrismDIDPublicKey.Usage.MASTER_KEY.id(0),
            usage = PrismDIDPublicKey.Usage.MASTER_KEY,
            keyData = keyPair.publicKey,
        )
        val protoData = publicKey.toProto()
        val proto = PublicKey(
            id = protoData.id,
            usage = protoData.usage,
            keyData = protoData.keyData,
        )
        val parsedPublicKey = PrismDIDPublicKey(
            apollo = apollo,
            proto = proto,
        )
        assertEquals(parsedPublicKey.id, "master0")
        assertContentEquals(parsedPublicKey.keyData.raw, publicKey.keyData.raw)
        assertEquals(parsedPublicKey.usage, publicKey.usage)
    }
}
