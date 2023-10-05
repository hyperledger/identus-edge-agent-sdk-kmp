package io.iohk.atala.prism.walletsdk.apollo.helpers

fun ByteArray.padStart(length: Int, padValue: Byte): ByteArray {
    return if (size >= length) {
        this
    } else {
        val result = ByteArray(length) { padValue }
        copyInto(result, length - size)
        result
    }
}
