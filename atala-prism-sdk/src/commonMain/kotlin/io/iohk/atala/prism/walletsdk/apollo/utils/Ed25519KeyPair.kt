package io.iohk.atala.prism.walletsdk.apollo.utils

import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.KeyPair
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.PrivateKey
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.PublicKey
import org.bouncycastle.crypto.generators.Ed25519KeyPairGenerator
import org.bouncycastle.crypto.params.Ed25519KeyGenerationParameters
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters
import java.security.SecureRandom

class Ed25519KeyPair(
    override var privateKey: PrivateKey,
    override var publicKey: PublicKey
) : KeyPair() {

    companion object {
        fun generateKeyPair(): Ed25519KeyPair {
            val generator = Ed25519KeyPairGenerator()
            generator.init(Ed25519KeyGenerationParameters(SecureRandom()))
            val pair = generator.generateKeyPair()
            return Ed25519KeyPair(
                privateKey = Ed25519PrivateKey(nativeValue = (pair.private as Ed25519PrivateKeyParameters).encoded),
                publicKey = Ed25519PublicKey(nativeValue = (pair.public as Ed25519PublicKeyParameters).encoded)
            )
        }
    }
}
