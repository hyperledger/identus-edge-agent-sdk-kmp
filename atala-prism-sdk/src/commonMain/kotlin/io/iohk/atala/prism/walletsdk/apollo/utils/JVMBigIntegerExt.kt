package io.iohk.atala.prism.walletsdk.apollo.utils

import com.ionspin.kotlin.bignum.integer.Sign
import java.math.BigInteger

fun BigInteger.toUnsignedByteArray(): ByteArray {
    return toByteArray().dropWhile { it == 0.toByte() }.toByteArray()
}

fun BigInteger.toKotlinBigInteger(): com.ionspin.kotlin.bignum.integer.BigInteger {
    val sign = when (this.signum()) {
        -1 -> Sign.NEGATIVE
        0 -> Sign.ZERO
        1 -> Sign.POSITIVE
        else -> throw IllegalStateException("Illegal BigInteger sign")
    }
    return com.ionspin.kotlin.bignum.integer.BigInteger.fromByteArray(this.toUnsignedByteArray(), sign)
}
