package io.iohk.atala.prism.walletsdk.apollo.helpers

/**
 * Pads the current ByteArray with the specified padValue at the beginning,
 * making it equal to or larger than the specified length.
 *
 * @param length The desired length for the new ByteArray.
 * @param padValue The value used to pad the ByteArray.
 * @return The padded ByteArray with the specified length.
 */
fun ByteArray.padStart(length: Int, padValue: Byte): ByteArray {
    return if (size >= length) {
        this
    } else {
        val result = ByteArray(length) { padValue }
        copyInto(result, length - size)
        result
    }
}
