package org.hyperledger.identus.walletsdk.apollo.utils

import org.hyperledger.identus.apollo.derivation.HDKey
import org.hyperledger.identus.apollo.securerandom.SecureRandom
import org.hyperledger.identus.walletsdk.domain.models.KeyCurve
import org.hyperledger.identus.walletsdk.domain.models.Seed
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.KeyPair
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.PrivateKey
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.PublicKey

/**
 * Represents a key pair consisting of a private key and public key.
 *
 * @property privateKey The private key of the key pair.
 * @property publicKey The public key of the key pair.
 */
class Secp256k1KeyPair(override var privateKey: PrivateKey, override var publicKey: PublicKey) : KeyPair() {
    companion object {
        @JvmStatic
        fun generateKeyPair(): Secp256k1KeyPair {
            val privateKeyRaw = SecureRandom().nextBytes(32)
            val privateKey = Secp256k1PrivateKey(privateKeyRaw)
            return Secp256k1KeyPair(privateKey, privateKey.publicKey())
        }

        /**
         * Generates a key pair using a given seed and key curve.
         *
         * @param seed The seed used for key generation.
         * @param curve The key curve to use for key generation.
         * @return A key pair consisting of a private key and public key.
         */
        @JvmStatic
        fun generateKeyPair(seed: Seed, curve: KeyCurve): Secp256k1KeyPair {
            val path = "m/${curve.index}'/0'/0'"
            val hdKey = HDKey(seed.value, 0, 0)
            val derivedHdKey = hdKey.derive(path)
            return Secp256k1KeyPair(
                privateKey = Secp256k1PrivateKey(derivedHdKey.getKMMSecp256k1PrivateKey().raw),
                publicKey = Secp256k1PublicKey(derivedHdKey.getKMMSecp256k1PrivateKey().getPublicKey().raw)
            )
        }
    }
}
