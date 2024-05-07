package org.hyperledger.identus.walletsdk.apollo.utils

import com.ionspin.kotlin.bignum.integer.Sign
import java.math.BigInteger

/**
 * Converts a `BigInteger` to an unsigned byte array.
 *
 * @return The unsigned byte array representation of the `BigInteger`.
 */
fun BigInteger.toUnsignedByteArray(): ByteArray {
    val comparedValue = 0.toByte()
    return toByteArray().dropWhile { it == comparedValue }.toByteArray()
}

/**
 * Converts a `java.math.BigInteger` to a `com.ionspin.kotlin.bignum.integer.BigInteger` object.
 *
 * @return The converted `com.ionspin.kotlin.bignum.integer.BigInteger` object.
 * @throws IllegalStateException if the sign of the original `java.math.BigInteger` is invalid.
 */
fun BigInteger.toKotlinBigInteger(): com.ionspin.kotlin.bignum.integer.BigInteger {
    val sign = when (this.signum()) {
        -1 -> Sign.NEGATIVE
        0 -> Sign.ZERO
        1 -> Sign.POSITIVE
        else -> throw IllegalStateException("Illegal BigInteger sign")
    }
    return com.ionspin.kotlin.bignum.integer.BigInteger.fromByteArray(this.toUnsignedByteArray(), sign)
}
