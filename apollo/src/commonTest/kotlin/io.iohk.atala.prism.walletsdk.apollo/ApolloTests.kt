package io.iohk.atala.prism.walletsdk.apollo

import io.iohk.atala.prism.apollo.derivation.MnemonicChecksumException
import io.iohk.atala.prism.apollo.derivation.MnemonicLengthException
import io.iohk.atala.prism.walletsdk.apollo.derivation.bip39Vectors
import io.iohk.atala.prism.walletsdk.apollo.helpers.BytesOps
import io.iohk.atala.prism.walletsdk.domain.buildingBlocks.Apollo
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

lateinit var apollo: Apollo

class ApolloTests {

    @BeforeTest
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
}
