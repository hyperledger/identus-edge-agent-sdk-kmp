package org.hyperledger.identus.walletsdk.apollo.utils

import org.hyperledger.identus.apollo.utils.KMMEdKeyPair
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.KeyPair
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.PrivateKey
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.PublicKey

/**
 * Represents a pair of Ed25519 private and public keys.
 */
class Ed25519KeyPair(
    override var privateKey: PrivateKey,
    override var publicKey: PublicKey
) : KeyPair() {

    companion object {
        /**
         * Generates a pair of Ed25519 private and public keys.
         *
         * @return The generated Ed25519KeyPair.
         */
        @JvmStatic
        fun generateKeyPair(): Ed25519KeyPair {
            val pair = KMMEdKeyPair.generateKeyPair()

            return Ed25519KeyPair(
                privateKey = Ed25519PrivateKey(nativeValue = pair.privateKey.raw),
                publicKey = Ed25519PublicKey(nativeValue = pair.publicKey.raw)
            )
        }
    }
}
