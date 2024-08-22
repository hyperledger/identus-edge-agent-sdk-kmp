package org.hyperledger.identus.walletsdk.pollux.utils

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.*

class BitStringTest {

    @Test
    fun `test BitString with some 0s`() {
        val byteArray = byteArrayOf(0.toByte())
        val bitString = BitString(byteArray)

        for (i in 0 until bitString.size) {
            assertFalse("Index $i should not be revoked", bitString.isRevoked(i))
        }
    }

    @Test
    fun `test BitString with some 1s`() {
        val byteArray = byteArrayOf(128.toByte(), 3.toByte())
        val bitString = BitString(byteArray)

        assertTrue("Index 0 should be revoked", bitString.isRevoked(0))
        assertTrue("Index 8 should be revoked", bitString.isRevoked(14))
        assertTrue("Index 8 should be revoked", bitString.isRevoked(15))
        for (i in 1 until 14) {
            assertFalse("Index $i should not be revoked", bitString.isRevoked(i))
        }
    }
}
