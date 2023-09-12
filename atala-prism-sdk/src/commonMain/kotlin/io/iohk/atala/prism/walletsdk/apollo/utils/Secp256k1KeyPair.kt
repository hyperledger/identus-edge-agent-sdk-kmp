package io.iohk.atala.prism.walletsdk.apollo.utils

import io.iohk.atala.prism.apollo.derivation.DerivationPath
import io.iohk.atala.prism.apollo.derivation.KeyDerivation
import io.iohk.atala.prism.apollo.utils.KMMECSecp256k1PrivateKey
import io.iohk.atala.prism.apollo.utils.KMMECSecp256k1PublicKey
import io.iohk.atala.prism.walletsdk.domain.models.KeyCurve
import io.iohk.atala.prism.walletsdk.domain.models.Seed
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.KeyPair
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.PrivateKey
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.PublicKey

class Secp256k1KeyPair(override var privateKey: PrivateKey, override var publicKey: PublicKey) : KeyPair() {
    companion object {
        fun generateKeyPair(seed: Seed?, curve: KeyCurve): Secp256k1KeyPair {
            val derivationPath = DerivationPath.fromPath("m/${curve.index}'/0'/0'")
            if (seed == null) {
                // TODO: Custom error for null seed
                throw Error("Seed cannot be null")
            }
            val extendedKey = KeyDerivation.deriveKey(seed.value, derivationPath)
            val kmmKeyPair = extendedKey.keyPair()
            val privateKey = kmmKeyPair.privateKey as KMMECSecp256k1PrivateKey
            val publicKey = kmmKeyPair.publicKey as KMMECSecp256k1PublicKey
            return Secp256k1KeyPair(
                privateKey = Secp256k1PrivateKey(privateKey.getEncoded()),
                publicKey = Secp256k1PublicKey(publicKey.getEncoded())
            )
        }
    }
}
