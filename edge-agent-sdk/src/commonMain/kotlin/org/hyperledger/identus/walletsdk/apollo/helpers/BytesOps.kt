package org.hyperledger.identus.walletsdk.apollo.helpers

/**
 * This represents the conversion from and to [ByteArray] to Hex.
 */
object BytesOps {
    /**
     * List of hex characters
     */
    private val HEX_ARRAY = "0123456789abcdef".toCharArray()

    /**
     * Conversion of [ByteArray] to Hex.
     *
     * @param bytes the bytes to convert to Hex
     * @return Hex String
     */
    @OptIn(ExperimentalUnsignedTypes::class)
    fun bytesToHex(bytes: ByteArray): String {
        val ubytes = bytes.toUByteArray()
        val hexChars = CharArray(bytes.size * 2)
        for (j in ubytes.indices) {
            val v = (ubytes[j] and 0xFF.toUByte()).toInt()

            hexChars[j * 2] = HEX_ARRAY[v ushr 4]
            hexChars[j * 2 + 1] = HEX_ARRAY[v and 0x0F]
        }
        return hexChars.concatToString()
    }

    /**
     * Conversion of Hex to [ByteArray].
     *
     * @param string Hex string.
     * @return [ByteArray].
     */
    @OptIn(ExperimentalUnsignedTypes::class)
    fun hexToBytes(string: String): ByteArray {
        val result = UByteArray(string.length / 2) { UByte.MIN_VALUE }

        for (i in string.indices step 2) {
            val firstIndex = HEX_ARRAY.indexOf(string[i])
            val secondIndex = HEX_ARRAY.indexOf(string[i + 1])

            val octet = firstIndex.shl(4).or(secondIndex)
            result[i.shr(1)] = octet.toUByte()
        }

        return result.toByteArray()
    }
}
