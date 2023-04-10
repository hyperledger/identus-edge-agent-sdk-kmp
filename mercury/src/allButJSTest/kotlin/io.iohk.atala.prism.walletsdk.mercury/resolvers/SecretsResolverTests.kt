package io.iohk.atala.prism.walletsdk.mercury.resolvers

import io.iohk.atala.prism.walletsdk.domain.models.Curve
import io.iohk.atala.prism.walletsdk.domain.models.KeyCurve
import io.iohk.atala.prism.walletsdk.domain.models.PrivateKey
import io.iohk.atala.prism.walletsdk.mercury.PlutoMock
import org.didcommx.didcomm.common.VerificationMaterialFormat
import org.didcommx.didcomm.common.VerificationMethodType
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class SecretsResolverTests {
    lateinit var plutoMock: PlutoMock
    lateinit var sut: DIDCommSecretsResolver

    @BeforeTest
    fun setup() {
        plutoMock = PlutoMock()
        sut = DIDCommSecretsResolver(plutoMock)
    }

    @Ignore("Ignore this test for now until we can review with the team.")
    @Test
    fun testFindKey() {
        val privateKey = PrivateKey(KeyCurve(Curve.X25519), ByteArray(4))
        val kid = privateKey.keyCurve.curve.value
        plutoMock.privateKeys.add(privateKey)

        val result = sut.findKey(kid)
        val key = result.get()

        assertNotNull(key)
        assertTrue { key.kid == kid }
        assertTrue { key.type == VerificationMethodType.JSON_WEB_KEY_2020 }
        assertTrue { key.verificationMaterial.format == VerificationMaterialFormat.MULTIBASE }
        assertTrue { key.verificationMaterial.value == privateKey.value.toString() }
    }

    @Test
    fun testFindKeys_kidMatches_isReturned() {
        val privateKey = PrivateKey(KeyCurve(Curve.X25519), ByteArray(0))
        plutoMock.privateKeys.add(privateKey)

        val kids = listOf(privateKey.keyCurve.curve.value)
        val result = sut.findKeys(kids)

        assertTrue { result.size == 1 }
        assertContains(result, privateKey.keyCurve.curve.value)
    }

    @Test
    fun testFindKeys_kidNoMatch_emptySetReturned() {
        val privateKey = PrivateKey(KeyCurve(Curve.X25519), ByteArray(0))
        val kids = listOf(privateKey.keyCurve.curve.value)
        val result = sut.findKeys(kids)

        assertTrue { result.isEmpty() }
    }
}
