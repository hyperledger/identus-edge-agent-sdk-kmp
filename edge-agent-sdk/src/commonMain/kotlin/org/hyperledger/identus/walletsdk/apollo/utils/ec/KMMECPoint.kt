package org.hyperledger.identus.walletsdk.apollo.utils.ec

import com.ionspin.kotlin.bignum.integer.BigInteger

/**
 * Represents a point in the elliptic curve cryptography (ECC) system.
 *
 * @property x The x-coordinate of the point.
 * @property y The y-coordinate of the point.
 */
class KMMECPoint(val x: KMMECCoordinate, val y: KMMECCoordinate) {
    /**
     * Represents a point in the elliptic curve cryptography (ECC) system.
     *
     * @property x The x-coordinate of the point.
     * @property y The y-coordinate of the point.
     */
    constructor(x: String, y: String) : this(
        KMMECCoordinate(BigInteger.parseString(x)),
        KMMECCoordinate(BigInteger.parseString(y))
    )

    /**
     * Represents a point in the elliptic curve cryptography (ECC) system.
     *
     * @property x The x-coordinate of the point.
     * @property y The y-coordinate of the point.
     *
     * @constructor Creates a new instance of KMMECPoint with the provided x and y coordinates.
     * @param x The x-coordinate as a BigInteger.
     * @param y The y-coordinate as a BigInteger.
     */
    constructor(x: BigInteger, y: BigInteger) : this(
        KMMECCoordinate(x),
        KMMECCoordinate(y)
    )
}
