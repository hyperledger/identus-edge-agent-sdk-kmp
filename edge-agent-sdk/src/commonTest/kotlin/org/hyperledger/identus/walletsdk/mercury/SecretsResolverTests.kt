package org.hyperledger.identus.walletsdk.mercury

import org.didcommx.didcomm.common.VerificationMaterialFormat
import org.didcommx.didcomm.common.VerificationMethodType
import org.hyperledger.identus.walletsdk.apollo.utils.X25519PrivateKey
import org.hyperledger.identus.walletsdk.edgeagent.ApolloMock
import org.hyperledger.identus.walletsdk.mercury.resolvers.DIDCommSecretsResolver
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import kotlin.test.assertContains
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class SecretsResolverTests {
    lateinit var plutoMock: PlutoMock
    lateinit var apolloMock: ApolloMock
    lateinit var sut: DIDCommSecretsResolver

    @Before
    fun setup() {
        plutoMock = PlutoMock()
        apolloMock = ApolloMock()
        sut = DIDCommSecretsResolver(plutoMock, apolloMock)
    }

    @Ignore("Ignore this test for now until we can review with the team.")
    @Test
    fun testFindKey() {
        val privateKey = X25519PrivateKey(ByteArray(4))
        val kid = privateKey.getCurve()
        plutoMock.privateKeys.add(privateKey)

        val result = sut.findKey(kid)
        val key = result.get()

        assertNotNull(key)
        assertTrue { key.kid == kid }
        assertTrue { key.type == VerificationMethodType.JSON_WEB_KEY_2020 }
        assertTrue { key.verificationMaterial.format == VerificationMaterialFormat.MULTIBASE }
        assertTrue { key.verificationMaterial.value == privateKey.raw.toString() }
    }

    @Test
    fun testFindKeys_kidMatches_isReturned() {
        val privateKey = X25519PrivateKey(ByteArray(32))
        plutoMock.privateKeys.add(privateKey)

        val kids = listOf(privateKey.getCurve())
        val result = sut.findKeys(kids)

        assertTrue { result.size == 1 }
        assertContains(result, privateKey.getCurve())
    }

    @Test
    fun testFindKeys_kidNoMatch_emptySetReturned() {
        val privateKey = X25519PrivateKey(ByteArray(32))
        val kids = listOf(privateKey.getCurve())
        val result = sut.findKeys(kids)

        assertTrue { result.isEmpty() }
    }
}
