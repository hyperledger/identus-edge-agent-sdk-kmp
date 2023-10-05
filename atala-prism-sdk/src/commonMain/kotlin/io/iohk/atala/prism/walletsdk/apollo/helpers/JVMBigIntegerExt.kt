package io.iohk.atala.prism.walletsdk.apollo.helpers

import java.math.BigInteger

fun com.ionspin.kotlin.bignum.integer.BigInteger.toJavaBigInteger(): BigInteger {
    return BigInteger(this.signum(), this.toByteArray())
}
