package org.hyperledger.identus.walletsdk.apollo.utils.ec

import com.ionspin.kotlin.bignum.integer.BigInteger
import org.hyperledger.identus.walletsdk.apollo.helpers.padStart

/**
 * Represents a coordinate in the elliptic curve cryptography (ECC) system.
 *
 * @property coordinate The coordinate value represented as a BigInteger.
 */
class KMMECCoordinate(val coordinate: BigInteger) {

    /**
     * Returns a ByteArray representation of the coordinate.
     * The returned ByteArray will be padded with zeroes at the beginning if necessary
     * to match the specified length.
     *
     * @return The ByteArray representation of the coordinate.
     */
    fun bytes(): ByteArray = coordinate.toByteArray().padStart(PRIVATE_KEY_BYTE_SIZE, 0)

    companion object {
        internal const val PRIVATE_KEY_BYTE_SIZE: Int = 32
    }
}
