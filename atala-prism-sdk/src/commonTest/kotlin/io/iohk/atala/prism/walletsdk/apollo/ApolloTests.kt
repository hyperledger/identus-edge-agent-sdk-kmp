package io.iohk.atala.prism.walletsdk.apollo

import io.iohk.atala.prism.apollo.derivation.MnemonicChecksumException
import io.iohk.atala.prism.apollo.derivation.MnemonicLengthException
import io.iohk.atala.prism.apollo.utils.ECConfig
import io.iohk.atala.prism.walletsdk.apollo.derivation.bip39Vectors
import io.iohk.atala.prism.walletsdk.apollo.helpers.BytesOps
import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Apollo
import io.iohk.atala.prism.walletsdk.domain.models.Curve
import io.iohk.atala.prism.walletsdk.domain.models.KeyCurve
import io.iohk.atala.prism.walletsdk.domain.models.KeyPair
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ApolloTests {
    lateinit var apollo: Apollo
    lateinit var keyPair: KeyPair

    val testData =
        byteArrayOf(-107, 101, 68, 118, 27, 74, 29, 50, -32, 72, 47, -127, -49, 3, -8, -55, -63, -66, 46, 125)

    @BeforeTest
    fun before() {
        apollo = ApolloImpl()
        keyPair = apollo.createKeyPair(apollo.createRandomSeed().seed, KeyCurve(Curve.SECP256K1))
    }

    @Test
    fun testRandomMnemonicCode() {
        for (i in 1..10) {
            assertEquals(24, apollo.createRandomMnemonics().size)
        }
    }

    @Test
    fun testGenerateRandomMnemonics() {
        val seenWords = mutableSetOf<String>()
        for (i in 1..300) {
            seenWords.addAll(apollo.createRandomMnemonics())
        }
        // with great probability we'll see at least 75% of words after 3600 draws from 2048 possible
        assertTrue(2048 - seenWords.size < 512)
    }

    @Test
    fun testComputeRightBinarySeed() {
        val password = "TREZOR"
        val vectors = Json.decodeFromString<List<List<String>>>(bip39Vectors)
        for (v in vectors) {
            val (_, mnemonicPhrase, binarySeedHex, _) = v
            val mnemonicCode = mnemonicPhrase.split(" ").toTypedArray()
            val binarySeed = apollo.createSeed(mnemonicCode, password)

            assertEquals(binarySeedHex, BytesOps.bytesToHex(binarySeed.value))
        }
    }

    @Test
    fun testFailWhenChecksumIsIncorrect() {
        val mnemonicCode = Array(24) { "abandon" }
        assertFailsWith<MnemonicChecksumException> {
            apollo.createSeed(mnemonicCode, "")
        }
    }

    @Test
    fun testFailWhenInvalidWordIsUsed() {
        val mnemonicCode = arrayOf("hocus", "pocus", "mnemo", "codus") + Array(24) { "abandon" }
        assertFailsWith<MnemonicLengthException> {
            apollo.createSeed(mnemonicCode, "")
        }
    }

    @Test
    fun testFailWhenWrongLength() {
        assertFailsWith<MnemonicLengthException> {
            apollo.createSeed(arrayOf("abandon"), "")
        }
    }

    @Test
    fun testKeyPairGeneration() {
        assertEquals(keyPair.publicKey.value.size, ECConfig.PUBLIC_KEY_BYTE_SIZE)
        assertEquals(keyPair.privateKey.value.size, ECConfig.PRIVATE_KEY_BYTE_SIZE)
    }

    @Test
    fun testSignAndVerifyTest() {
        val text = "The quick brown fox jumps over the lazy dog"
        val signature = apollo.signMessage(keyPair.privateKey, text)

        assertEquals(signature.value.size <= ECConfig.SIGNATURE_MAX_BYTE_SIZE, true)

        assertTrue(apollo.verifySignature(keyPair.publicKey, text.encodeToByteArray(), signature))
    }

    @Test
    fun testCreateKeyPair_whenNoSeedAndKeyCurveEd25519_thenPrivateKeyLengthIsCorrect() {
        val keyPair = apollo.createKeyPair(curve = KeyCurve(Curve.ED25519))
        val privateKey = keyPair.privateKey
        val publicKey = keyPair.publicKey
        assertEquals(32, privateKey.value.size)
        assertEquals(32, publicKey.value.size)
    }

    @Test
    fun testCreateKeyPair_whenNoSeedAndKeyCurveX25519_thenPrivateKeyLengthIsCorrect() {
        val keyPair = apollo.createKeyPair(curve = KeyCurve(Curve.X25519))
        val privateKey = keyPair.privateKey
        val publicKey = keyPair.publicKey
        assertEquals(32, privateKey.value.size)
        assertEquals(32, publicKey.value.size)
    }
}
