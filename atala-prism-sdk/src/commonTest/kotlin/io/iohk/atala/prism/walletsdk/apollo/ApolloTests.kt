package io.iohk.atala.prism.walletsdk.apollo

import io.iohk.atala.prism.apollo.base64.base64UrlEncoded
import io.iohk.atala.prism.apollo.utils.ECConfig
import io.iohk.atala.prism.apollo.utils.Mnemonic
import io.iohk.atala.prism.walletsdk.apollo.derivation.bip39Vectors
import io.iohk.atala.prism.walletsdk.apollo.helpers.BytesOps
import io.iohk.atala.prism.walletsdk.apollo.utils.Ed25519KeyPair
import io.iohk.atala.prism.walletsdk.apollo.utils.Ed25519PrivateKey
import io.iohk.atala.prism.walletsdk.apollo.utils.Ed25519PublicKey
import io.iohk.atala.prism.walletsdk.apollo.utils.Secp256k1KeyPair
import io.iohk.atala.prism.walletsdk.apollo.utils.Secp256k1PrivateKey
import io.iohk.atala.prism.walletsdk.apollo.utils.Secp256k1PublicKey
import io.iohk.atala.prism.walletsdk.apollo.utils.X25519KeyPair
import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Apollo
import io.iohk.atala.prism.walletsdk.domain.models.Curve
import io.iohk.atala.prism.walletsdk.domain.models.KeyCurve
import io.iohk.atala.prism.walletsdk.domain.models.Seed
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.KeyPair
import kotlinx.serialization.json.Json
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ApolloTests {
    lateinit var apollo: Apollo
    lateinit var keyPair: KeyPair

    val testData =
        byteArrayOf(-107, 101, 68, 118, 27, 74, 29, 50, -32, 72, 47, -127, -49, 3, -8, -55, -63, -66, 46, 125)

    @Before
    fun before() {
        apollo = ApolloImpl()
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
        val password = "mnemonicTREZOR"
        val vectors = Json.decodeFromString<List<List<String>>>(bip39Vectors)
        for (v in vectors) {
            val (_, mnemonicPhrase, binarySeedHex, _) = v
            val mnemonicCode = mnemonicPhrase.split(" ").toTypedArray()
            val binarySeed = apollo.createSeed(mnemonicCode, password)

            assertEquals(binarySeedHex, BytesOps.bytesToHex(binarySeed.value))
        }
    }

    @Test
    fun testFailWhenInvalidWordIsUsed() {
        val mnemonicCode = arrayOf("hocus", "pocus", "mnemo", "codus") + Array(24) { "abandon" }
        assertFailsWith<Mnemonic.Companion.InvalidMnemonicCode> {
            apollo.createSeed(mnemonicCode, "")
        }
    }

    @Test
    fun testKeyPairGeneration() {
        keyPair = Secp256k1KeyPair.generateKeyPair(Seed(Mnemonic.createRandomSeed()), KeyCurve(Curve.SECP256K1))
        assertEquals(keyPair.publicKey.raw.size, ECConfig.PUBLIC_KEY_BYTE_SIZE)
        assertEquals(keyPair.privateKey.raw.size, ECConfig.PRIVATE_KEY_BYTE_SIZE)
    }

    @Test
    fun testSignAndVerifyTest() {
        val message = "The quick brown fox jumps over the lazy dog"
        keyPair = Secp256k1KeyPair.generateKeyPair(Seed(Mnemonic.createRandomSeed()), KeyCurve(Curve.SECP256K1))
        val signature = (keyPair.privateKey as Secp256k1PrivateKey).sign(message.toByteArray())

        assertEquals(signature.size <= ECConfig.SIGNATURE_MAX_BYTE_SIZE, true)

        assertTrue((keyPair.publicKey as Secp256k1PublicKey).verify(message.encodeToByteArray(), signature))
    }

    @Test
    fun testCreateKeyPair_whenNoSeedAndKeyCurveEd25519_thenPrivateKeyLengthIsCorrect() {
        val keyPair = Ed25519KeyPair.generateKeyPair()
        val privateKey = keyPair.privateKey
        val publicKey = keyPair.publicKey
        assertEquals(32, privateKey.raw.size)
        assertEquals(32, publicKey.raw.size)
    }

    @Test
    fun testCreateKeyPair_whenNoSeedAndKeyCurveX25519_thenPrivateKeyLengthIsCorrect() {
        val keyPair = X25519KeyPair.generateKeyPair()
        val privateKey = keyPair.privateKey
        val publicKey = keyPair.publicKey
        assertEquals(32, privateKey.raw.size)
        assertEquals(32, publicKey.raw.size)
    }

    @Test
    fun testCreateKeyPair_whenUsingSeedAndMnemonics_thenKeyPairIsCorrect() {
        val mnemonics = arrayOf(
            "blade",
            "multiply",
            "coil",
            "rare",
            "fox",
            "doll",
            "tongue",
            "please",
            "icon",
            "mind",
            "gesture",
            "moral",
            "old",
            "laugh",
            "symptom",
            "assume",
            "burden",
            "appear",
            "always",
            "oil",
            "ticket",
            "vault",
            "return",
            "height"
        )
        val seed = Seed(Mnemonic.createSeed(mnemonics = mnemonics, passphrase = "mnemonic"))

        val expectedPrivateKeyBase64Url = "xURclKhT6as1Tb9vg4AJRRLPAMWb9dYTTthDvXEKjMc"

        assertEquals(
            "7bcb8d37b2d11f998451cc5aec58710c081618b87c3fde1610e0f48d475a276992535a34daef8bcdaa39d1e955df65ef613d2ec7d8d6b815440a9140cf67242b",
            BytesOps.bytesToHex(seed.value)
        )
        val keyPair = Secp256k1KeyPair.generateKeyPair(seed, KeyCurve(Curve.SECP256K1))
        assertEquals(expectedPrivateKeyBase64Url, keyPair.privateKey.raw.base64UrlEncoded)
    }

    @Test
    fun testSignAndVerify_whenSignatureIsCorrect_thenVerifiedCorrectly() {
        val keyPair = Ed25519KeyPair.generateKeyPair()
        val message = "This is a test message"
        val signature = (keyPair.privateKey as Ed25519PrivateKey).sign(message.toByteArray())

        assertTrue((keyPair.publicKey as Ed25519PublicKey).verify(message.toByteArray(), signature))
    }

    @Test
    fun testSignAndVerify_whenSignatureIsIncorrect_thenVerifyFails() {
        val keyPair = Ed25519KeyPair.generateKeyPair()
        val message = "This is a test message"
        var signature = (keyPair.privateKey as Ed25519PrivateKey).sign(message.toByteArray())
        signature[0] = 1
        assertFalse((keyPair.publicKey as Ed25519PublicKey).verify(message.toByteArray(), signature))
    }
}
