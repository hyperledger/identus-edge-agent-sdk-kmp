package io.iohk.atala.prism.walletsdk.apollo.utils

import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.KeyPair
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.PrivateKey
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.PublicKey
import org.bouncycastle.crypto.generators.X25519KeyPairGenerator
import org.bouncycastle.crypto.params.X25519KeyGenerationParameters
import org.bouncycastle.crypto.params.X25519PrivateKeyParameters
import org.bouncycastle.crypto.params.X25519PublicKeyParameters
import java.security.SecureRandom

class X25519KeyPair(override var privateKey: PrivateKey, override var publicKey: PublicKey) : KeyPair() {

    companion object {
        fun generateKeyPair(): X25519KeyPair {
            val generator = X25519KeyPairGenerator()
            generator.init(X25519KeyGenerationParameters(SecureRandom()))
            val keyPair = generator.generateKeyPair()
            return X25519KeyPair(
                privateKey = X25519PrivateKey((keyPair.private as X25519PrivateKeyParameters).encoded),
                publicKey = X25519PublicKey((keyPair.public as X25519PublicKeyParameters).encoded)
            )
        }
    }
}
