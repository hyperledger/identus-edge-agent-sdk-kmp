package io.iohk.atala.prism.utils

import java.security.SecureRandom

object Utils {
    fun generateNonce(length: Int): String {
        var result = ""
        val secureRandom = SecureRandom()
        while (result.length < length) {
            val byte = ByteArray(1)
            secureRandom.nextBytes(byte)
            val int = byte[0].toInt() and 0xFF
            if (int > 250) {
                continue
            }
            val digit = int % 10
            result += digit
        }
        return result
    }
}
