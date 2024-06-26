@file:Suppress("ktlint:standard:import-ordering")

package org.hyperledger.identus.walletsdk.apollo

import kotlinx.serialization.json.Json
import org.hyperledger.identus.apollo.base64.base64UrlEncoded
import org.hyperledger.identus.apollo.derivation.DerivationPath
import org.hyperledger.identus.apollo.derivation.MnemonicHelper
import org.hyperledger.identus.walletsdk.apollo.config.ECConfig
import org.hyperledger.identus.walletsdk.apollo.derivation.bip39Vectors
import org.hyperledger.identus.walletsdk.apollo.helpers.BytesOps
import org.hyperledger.identus.walletsdk.apollo.utils.Ed25519KeyPair
import org.hyperledger.identus.walletsdk.apollo.utils.Ed25519PrivateKey
import org.hyperledger.identus.walletsdk.apollo.utils.Ed25519PublicKey
import org.hyperledger.identus.walletsdk.apollo.utils.Secp256k1KeyPair
import org.hyperledger.identus.walletsdk.apollo.utils.Secp256k1PrivateKey
import org.hyperledger.identus.walletsdk.apollo.utils.Secp256k1PublicKey
import org.hyperledger.identus.walletsdk.apollo.utils.X25519KeyPair
import org.hyperledger.identus.walletsdk.apollo.utils.X25519PrivateKey
import org.hyperledger.identus.walletsdk.apollo.utils.X25519PublicKey
import org.hyperledger.identus.walletsdk.domain.buildingblocks.Apollo
import org.hyperledger.identus.walletsdk.domain.models.Curve
import org.hyperledger.identus.walletsdk.domain.models.KeyCurve
import org.hyperledger.identus.walletsdk.domain.models.Seed
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.CurveKey
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.IndexKey
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.KeyPair
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.KeyTypes
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.SeedKey
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.StorableKey
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.TypeKey
import org.junit.Assert.assertNotEquals
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
        assertFailsWith<MnemonicHelper.Companion.InvalidMnemonicCode> {
            apollo.createSeed(mnemonicCode, "")
        }
    }

    @Test
    fun testKeyPairGeneration() {
        keyPair = Secp256k1KeyPair.generateKeyPair(Seed(MnemonicHelper.createRandomSeed()), KeyCurve(Curve.SECP256K1))
        assertEquals(keyPair.publicKey.raw.size, ECConfig.PUBLIC_KEY_BYTE_SIZE)
        assertEquals(keyPair.privateKey.raw.size, ECConfig.PRIVATE_KEY_BYTE_SIZE)
    }

    @Test
    fun testSignAndVerifyTest() {
        val message = "The quick brown fox jumps over the lazy dog"
        keyPair = Secp256k1KeyPair.generateKeyPair(Seed(MnemonicHelper.createRandomSeed()), KeyCurve(Curve.SECP256K1))
        val signature = (keyPair.privateKey as Secp256k1PrivateKey).sign(message.toByteArray())

        assertEquals(signature.size <= ECConfig.SIGNATURE_MAX_BYTE_SIZE, true)

        assertTrue((keyPair.publicKey as Secp256k1PublicKey).verify(message.encodeToByteArray(), signature))
        assertTrue((keyPair.publicKey as Secp256k1PublicKey).isExportable())
        assertTrue((keyPair.privateKey as Secp256k1PrivateKey).isExportable())
    }

    @Test
    fun testDerivePrivateKey_whenSecp256k1_thenWorksAsExpected() {
        val path = "m/1'/0'/0'"

        val seed = Seed(MnemonicHelper.createRandomSeed())

        val properties: MutableMap<String, Any> = mutableMapOf()
        properties[TypeKey().property] = KeyTypes.EC
        properties[SeedKey().property] = seed.value.base64UrlEncoded
        properties[CurveKey().property] = Curve.SECP256K1.value
        properties[IndexKey().property] = 0

        val privateKey = apollo.createPrivateKey(properties) as Secp256k1PrivateKey
        privateKey.keySpecification[SeedKey().property] = seed.value.base64UrlEncoded
        privateKey.derive(DerivationPath.fromPath(path))
        assertTrue(privateKey.isDerivable())

        val ed25519KeyPair = Ed25519KeyPair.generateKeyPair()
        val ed25519PrivateKey = ed25519KeyPair.privateKey as Ed25519PrivateKey
        assertFalse(ed25519PrivateKey.isDerivable())
    }

    @Test
    fun testCreateKeyPair_whenNoSeedAndKeyCurveEd25519_thenPrivateKeyLengthIsCorrect() {
        val keyPair = Ed25519KeyPair.generateKeyPair()
        val privateKey = keyPair.privateKey
        val publicKey = keyPair.publicKey
        assertEquals(32, privateKey.raw.size)
        assertEquals(32, publicKey.raw.size)
        assertTrue((keyPair.publicKey as Ed25519PublicKey).isExportable())
        assertTrue((keyPair.privateKey as Ed25519PrivateKey).isExportable())
    }

    @Test
    fun testCreateKeyPair_whenNoSeedAndKeyCurveX25519_thenPrivateKeyLengthIsCorrect() {
        val keyPair = X25519KeyPair.generateKeyPair()
        val privateKey = keyPair.privateKey
        val publicKey = keyPair.publicKey
        assertEquals(32, privateKey.raw.size)
        assertEquals(32, publicKey.raw.size)
        assertTrue((keyPair.publicKey as X25519PublicKey).isExportable())
        assertTrue((keyPair.privateKey as X25519PrivateKey).isExportable())
    }

    @Test
    fun testCreateKeyPair_whenUsingSeedAndMnemonics_thenKeyPairIsCorrect() {
        val mnemonics = listOf(
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
        val seed = Seed(MnemonicHelper.createSeed(mnemonics = mnemonics, passphrase = "mnemonic"))

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
        val signature = (keyPair.privateKey as Ed25519PrivateKey).sign(message.toByteArray())
        signature[0] = 1
        assertFalse((keyPair.publicKey as Ed25519PublicKey).verify(message.toByteArray(), signature))
    }

    @Test
    fun testRestorePrivateKey_whenStorableSecp256k1_thenRestoredOk() {
        val keyPairSecp256k1 =
            Secp256k1KeyPair.generateKeyPair(Seed(MnemonicHelper.createRandomSeed()), KeyCurve(Curve.SECP256K1))
        val keyPairEd25519 = Ed25519KeyPair.generateKeyPair()
        val privateKeySecp256k1 = keyPairSecp256k1.privateKey as Secp256k1PrivateKey
        val privateKeyEd25519 = keyPairEd25519.privateKey as Ed25519PrivateKey
        val storableKey = keyPairSecp256k1.privateKey as StorableKey
        val restoredKey = apollo.restorePrivateKey(storableKey)
        assertEquals(privateKeySecp256k1.raw, restoredKey.raw)
        assertNotEquals(privateKeyEd25519.raw, restoredKey.raw)
    }

    @Test
    fun testRestorePublicKey_whenStorableSecp256k1_thenRestoredOk() {
        val keyPairSecp256k1 =
            Secp256k1KeyPair.generateKeyPair(Seed(MnemonicHelper.createRandomSeed()), KeyCurve(Curve.SECP256K1))
        val keyPairEd25519 = Ed25519KeyPair.generateKeyPair()
        val publicKeySecp256k1 = keyPairSecp256k1.publicKey as Secp256k1PublicKey
        val publicKeyEd25519 = keyPairEd25519.publicKey as Ed25519PublicKey
        val storableKey = keyPairSecp256k1.publicKey as StorableKey
        val restoredKey = apollo.restorePublicKey(storableKey)
        assertEquals(publicKeySecp256k1.raw, restoredKey.raw)
        assertNotEquals(publicKeyEd25519.raw, restoredKey.raw)
    }
}
