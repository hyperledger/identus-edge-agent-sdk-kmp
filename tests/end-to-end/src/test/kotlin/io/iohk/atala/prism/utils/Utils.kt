package io.iohk.atala.prism.utils

import java.security.SecureRandom

object Utils {
    /**
    static generateNonce(length: number): string {
    let result = ""
    while (result.length < length) {
    const byte = crypto.randomBytes(1)[0]
    if (byte >= 250) {
    continue
    }
    const randomDigit = byte % 10
    result += randomDigit.toString()
    }
    return result
    }
     */
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
