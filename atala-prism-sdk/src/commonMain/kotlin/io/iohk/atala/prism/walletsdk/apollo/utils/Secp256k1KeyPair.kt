package io.iohk.atala.prism.walletsdk.apollo.utils

import io.iohk.atala.prism.apollo.derivation.HDKey
import io.iohk.atala.prism.walletsdk.domain.models.ApolloError
import io.iohk.atala.prism.walletsdk.domain.models.KeyCurve
import io.iohk.atala.prism.walletsdk.domain.models.Seed
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.KeyPair
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.PrivateKey
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.PublicKey

class Secp256k1KeyPair(override var privateKey: PrivateKey, override var publicKey: PublicKey) : KeyPair() {
    companion object {
        fun generateKeyPair(seed: Seed?, curve: KeyCurve): Secp256k1KeyPair {
            val path = "m/${curve.index}'/0'/0'"
            if (seed == null) {
                throw ApolloError.InvalidSeed("Seed cannot be null")
            }
            val hdKey = HDKey(seed.value, 0, 0)
            val derivedHdKey = hdKey.derive(path)
            return Secp256k1KeyPair(
                privateKey = Secp256k1PrivateKey(derivedHdKey.getKMMSecp256k1PrivateKey().raw),
                publicKey = Secp256k1PublicKey(derivedHdKey.getKMMSecp256k1PrivateKey().getPublicKey().raw)
            )
        }
    }
}
