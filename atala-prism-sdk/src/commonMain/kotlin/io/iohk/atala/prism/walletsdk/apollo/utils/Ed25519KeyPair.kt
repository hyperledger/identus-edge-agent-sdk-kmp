package io.iohk.atala.prism.walletsdk.apollo.utils

import io.iohk.atala.prism.apollo.utils.KMMEdKeyPair
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.KeyPair
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.PrivateKey
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.PublicKey

class Ed25519KeyPair(
    override var privateKey: PrivateKey,
    override var publicKey: PublicKey
) : KeyPair() {

    companion object {
        fun generateKeyPair(): Ed25519KeyPair {
            val pair = KMMEdKeyPair.generateKeyPair()

            return Ed25519KeyPair(
                privateKey = Ed25519PrivateKey(nativeValue = pair.privateKey.raw),
                publicKey = Ed25519PublicKey(nativeValue = pair.publicKey.raw)
            )
        }
    }
}
