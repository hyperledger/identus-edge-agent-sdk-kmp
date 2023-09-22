package io.iohk.atala.prism.walletsdk.apollo.utils.ec

import com.ionspin.kotlin.bignum.integer.BigInteger

class KMMECPoint(val x: KMMECCoordinate, val y: KMMECCoordinate) {
    constructor(x: String, y: String) : this(
        KMMECCoordinate(BigInteger.parseString(x)),
        KMMECCoordinate(BigInteger.parseString(y))
    )

    constructor(x: BigInteger, y: BigInteger) : this(
        KMMECCoordinate(x),
        KMMECCoordinate(y)
    )
}
