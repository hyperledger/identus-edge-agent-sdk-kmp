package org.hyperledger.identus.walletsdk.pollux.utils

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.*

class BitStringTest {

    @Test
    fun `test BitString with some 0s`() {
        val byteArray = byteArrayOf(0.toByte())
        val bitSet = BitSet.valueOf(byteArray)
        val bitString = BitString(bitSet, byteArray.size * 8)

        for (i in 0 until bitString.size) {
            assertFalse("Index $i should not be revoked", bitString.isRevoked(i))
        }
    }

    @Test
    fun `test BitString with some 1s`() {
        val byteArray = byteArrayOf(128.toByte(), 1.toByte())
        val bitSet = BitSet.valueOf(byteArray)
        val bitString = BitString(bitSet, byteArray.size * 8)

        assertTrue("Index 7 should be revoked", bitString.isRevoked(7))
        assertTrue("Index 8 should be revoked", bitString.isRevoked(8))
        for (i in 0 until 7) {
            assertFalse("Index $i should not be revoked", bitString.isRevoked(i))
        }
        for (i in 9 until bitString.size) {
            assertFalse("Index $i should not be revoked", bitString.isRevoked(i))
        }
    }
}
