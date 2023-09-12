package io.iohk.atala.prism.walletsdk.apollo.utils.ec

import com.ionspin.kotlin.bignum.integer.BigInteger
import io.iohk.atala.prism.walletsdk.apollo.helpers.padStart

class _ECCoordinate(val coordinate: BigInteger) {

    fun bytes(): ByteArray = coordinate.toByteArray().padStart(PRIVATE_KEY_BYTE_SIZE, 0)

    companion object {
        internal val PRIVATE_KEY_BYTE_SIZE: Int = 32
    }
}
