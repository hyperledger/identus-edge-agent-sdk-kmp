package io.iohk.atala.prism.walletsdk.apollo.utils

import io.iohk.atala.prism.apollo.utils.KMMX25519KeyPair
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.KeyPair
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.PrivateKey
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.PublicKey

class X25519KeyPair(override var privateKey: PrivateKey, override var publicKey: PublicKey) : KeyPair() {

    companion object {
        fun generateKeyPair(): X25519KeyPair {
            val pair = KMMX25519KeyPair.generateKeyPair()

            return X25519KeyPair(
                privateKey = X25519PrivateKey(pair.privateKey.raw),
                publicKey = X25519PublicKey(pair.publicKey.raw)
            )
        }
    }
}
