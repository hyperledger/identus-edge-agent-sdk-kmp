package org.hyperledger.identus.walletsdk.pollux.utils

import kotlin.experimental.and
import kotlin.experimental.inv
import kotlin.experimental.or

class BitString(
    buffer: ByteArray,
    private val leftToRightIndexing: Boolean = false
) {
    private val bits: ByteArray = buffer.copyOf()
    val length: Int = buffer.size * 8

    init {
        if (buffer.isEmpty()) {
            throw IllegalArgumentException("Buffer must not be empty.")
        }
    }

    fun set(position: Int, on: Boolean) {
        val (index, bit) = parsePosition(position)
        if (on) {
            bits[index] = bits[index] or bit
        } else {
            bits[index] = bits[index] and bit.inv()
        }
    }

    fun get(position: Int): Boolean {
        val (index, bit) = parsePosition(position)
        return (bits[index] and bit) != 0.toByte()
    }

    private fun parsePosition(position: Int): Pair<Int, Byte> {
        if (position >= length) {
            throw IllegalArgumentException("Position \"$position\" is out of range \"0-${length - 1}\".")
        }
        val index = position / 8
        val rem = position % 8
        val shift = if (leftToRightIndexing) 7 - rem else rem
        val bit = (1 shl shift).toByte()
        return Pair(index, bit)
    }
}
