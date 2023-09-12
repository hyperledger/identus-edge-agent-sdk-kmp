package io.iohk.atala.prism.walletsdk.apollo.utils.ec

import com.ionspin.kotlin.bignum.integer.BigInteger

class _ECPoint(val x: _ECCoordinate, val y: _ECCoordinate) {
    constructor(x: String, y: String) : this(
        _ECCoordinate(BigInteger.parseString(x)),
        _ECCoordinate(BigInteger.parseString(y))
    )

    constructor(x: BigInteger, y: BigInteger) : this(
        _ECCoordinate(x),
        _ECCoordinate(y)
    )
}
