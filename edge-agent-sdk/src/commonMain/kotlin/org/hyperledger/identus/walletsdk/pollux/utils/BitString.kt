package org.hyperledger.identus.walletsdk.pollux.utils

import java.util.BitSet

/**
 * The `BitString` class represents a sequence of bits using a `BitSet`.
 * It provides utility functions to work with individual bits within the sequence.
 *
 * @property bitSet The `BitSet` that stores the bits.
 * @property size The number of bits in the sequence.
 */
class BitString(val bitSet: BitSet, val size: Int) {

    /**
     * Checks if the bit at the specified index is set (i.e., if the bit is "revoked").
     *
     * @param index The index of the bit to check.
     * @return `true` if the bit at the specified index is set, `false` otherwise.
     * @throws Exception if the index is greater than or equal to the size of the bit sequence.
     */
    fun isRevoked(index: Int): Boolean {
        if (index >= size) {
            throw Exception("BitString error: index cannot be bigger than the size.")
        }
        return bitSet.get(index)
    }

    companion object {
        /**
         * Reverses the bits in an 8-bit integer.
         *
         * @param b The integer whose bits need to be reversed.
         * @return The integer with its bits reversed.
         */
        fun reverseBits(b: Int): Int {
            var result = 0
            for (i in 0 until 8) {
                val bit = (b shr i) and 1
                result = (result shl 1) or bit
            }
            return result
        }
    }
}
