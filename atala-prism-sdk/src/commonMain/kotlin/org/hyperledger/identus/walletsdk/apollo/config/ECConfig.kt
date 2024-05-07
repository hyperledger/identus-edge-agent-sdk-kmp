package org.hyperledger.identus.walletsdk.apollo.config

import com.ionspin.kotlin.bignum.integer.BigInteger

/**
 * The ECConfig object provides configuration properties and constants related to elliptic curve cryptography (ECC).
 */
object ECConfig {
    const val PRIVATE_KEY_BYTE_SIZE: Int = 32
    internal const val PUBLIC_KEY_COORDINATE_BYTE_SIZE: Int = 32
    internal const val PUBLIC_KEY_COMPRESSED_BYTE_SIZE: Int = PUBLIC_KEY_COORDINATE_BYTE_SIZE + 1
    const val SIGNATURE_MAX_BYTE_SIZE: Int = 72
    const val PUBLIC_KEY_BYTE_SIZE: Int = PUBLIC_KEY_COORDINATE_BYTE_SIZE * 2 + 1

    // Field characteristic p (prime) is equal to 2^256 - 2^32 - 2^9 - 2^8 - 2^7 - 2^6 - 2^4 - 1
    internal val p = BigInteger.parseString("115792089237316195423570985008687907853269984665640564039457584007908834671663", 10)
    internal val b = BigInteger(7)

    // n curve order (The order of secp256k1 is n)
    internal val n = BigInteger.parseString("115792089237316195423570985008687907852837564279074904382605163141518161494337", 10)
}
