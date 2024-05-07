package org.hyperledger.identus.walletsdk.apollo.helpers

import java.math.BigInteger

/**
 * Converts a Kotlin BigInteger to a Java BigInteger.
 *
 * @return the converted Java BigInteger.
 */
fun com.ionspin.kotlin.bignum.integer.BigInteger.toJavaBigInteger(): BigInteger {
    return BigInteger(this.signum(), this.toByteArray())
}
