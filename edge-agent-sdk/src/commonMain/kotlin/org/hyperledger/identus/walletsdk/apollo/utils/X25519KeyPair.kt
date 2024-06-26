package org.hyperledger.identus.walletsdk.apollo.utils

import org.hyperledger.identus.apollo.utils.KMMX25519KeyPair
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.KeyPair
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.PrivateKey
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.PublicKey

/**
 * Represents a pair of X25519 private and public keys.
 *
 * @property privateKey The X25519 private key.
 * @property publicKey The X25519 public key.
 */
class X25519KeyPair(override var privateKey: PrivateKey, override var publicKey: PublicKey) : KeyPair() {

    companion object {
        /**
         * Generates a pair of X25519 private and public keys.
         *
         * @return The generated X25519 key pair.
         */
        @JvmStatic
        fun generateKeyPair(): X25519KeyPair {
            val pair = KMMX25519KeyPair.generateKeyPair()

            return X25519KeyPair(
                privateKey = X25519PrivateKey(pair.privateKey.raw),
                publicKey = X25519PublicKey(pair.publicKey.raw)
            )
        }
    }
}
