package org.hyperledger.identus.walletsdk.castor

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hyperledger.identus.protos.PublicKey
import org.hyperledger.identus.walletsdk.apollo.utils.Ed25519KeyPair
import org.hyperledger.identus.walletsdk.apollo.utils.Ed25519PrivateKey
import org.hyperledger.identus.walletsdk.apollo.utils.Ed25519PublicKey
import org.hyperledger.identus.walletsdk.castor.did.prismdid.PrismDIDPublicKey
import org.hyperledger.identus.walletsdk.castor.did.prismdid.id
import org.junit.Ignore
import org.junit.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class PrismDIDPublicKeyTests {

    @OptIn(ExperimentalCoroutinesApi::class)
    @Ignore("PrismDIDPublicKey requires Secp256k1Lib to be an interface in order to mock its result. Once that is done this test can be added back.")
    @Test
    fun it_should_parse_proto_toPrismDIDPublicKey() = runTest {
        val apollo = ApolloMock()
        val seed = apollo.createRandomSeed(passphrase = "mnemonics").seed
        val keyPair = Ed25519KeyPair(
            privateKey = Ed25519PrivateKey(ByteArray(0)),
            publicKey = Ed25519PublicKey(ByteArray(0))
        )

        val publicKey = PrismDIDPublicKey(
            apollo = ApolloMock(),
            id = PrismDIDPublicKey.Usage.MASTER_KEY.id(0),
            usage = PrismDIDPublicKey.Usage.MASTER_KEY,
            keyData = keyPair.publicKey
        )
        val protoData = publicKey.toProto()
        val proto = PublicKey(
            id = protoData.id,
            usage = protoData.usage,
            keyData = protoData.keyData
        )
        val parsedPublicKey = PrismDIDPublicKey(
            apollo = apollo,
            proto = proto
        )
        assertEquals(parsedPublicKey.id, "master0")
        assertContentEquals(parsedPublicKey.keyData.raw, publicKey.keyData.raw)
        assertEquals(parsedPublicKey.usage, publicKey.usage)
    }
}
