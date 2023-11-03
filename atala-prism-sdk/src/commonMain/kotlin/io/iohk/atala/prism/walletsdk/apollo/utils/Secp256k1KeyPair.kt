package io.iohk.atala.prism.walletsdk.apollo.utils

import io.iohk.atala.prism.apollo.base64.base64UrlEncoded
import io.iohk.atala.prism.apollo.derivation.HDKey
import io.iohk.atala.prism.walletsdk.domain.models.KeyCurve
import io.iohk.atala.prism.walletsdk.domain.models.Seed
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.KeyPair
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.PrivateKey
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.PublicKey

class Secp256k1KeyPair(override var privateKey: PrivateKey, override var publicKey: PublicKey) : KeyPair() {
    companion object {
        fun generateKeyPair(seed: Seed, curve: KeyCurve): Secp256k1KeyPair {
            println("Seed size: ${seed.value.size}")
            println("Seed: ${seed.value.base64UrlEncoded}")
            val path = "m/${curve.index}'/0'/0'"
            val hdKey = HDKey(seed.value, 0, 0)
            println("HDKey private key size: ${hdKey.privateKey?.size}")
            println("Derive path: $path")
            val derivedHdKey = hdKey.derive(path)
            return Secp256k1KeyPair(
                privateKey = Secp256k1PrivateKey(derivedHdKey.getKMMSecp256k1PrivateKey().raw),
                publicKey = Secp256k1PublicKey(derivedHdKey.getKMMSecp256k1PrivateKey().getPublicKey().raw)
            )
        }
    }
}
