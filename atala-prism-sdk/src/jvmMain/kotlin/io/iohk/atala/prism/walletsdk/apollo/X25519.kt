package io.iohk.atala.prism.walletsdk.apollo

import io.iohk.atala.prism.walletsdk.domain.models.Curve
import io.iohk.atala.prism.walletsdk.domain.models.KeyCurve
import io.iohk.atala.prism.walletsdk.domain.models.KeyPair
import io.iohk.atala.prism.walletsdk.domain.models.PrivateKey
import io.iohk.atala.prism.walletsdk.domain.models.PublicKey
import org.bouncycastle.crypto.generators.X25519KeyPairGenerator
import org.bouncycastle.crypto.params.X25519KeyGenerationParameters
import org.bouncycastle.crypto.params.X25519PrivateKeyParameters
import org.bouncycastle.crypto.params.X25519PublicKeyParameters
import java.security.SecureRandom

/**
 * X25519
 */
actual object X25519 {

    /**
     * Create keypair for [X25519].
     *
     * @return [KeyPair] for [X25519].
     */
    actual fun createKeyPair(): KeyPair {
        val generator = X25519KeyPairGenerator()
        generator.init(X25519KeyGenerationParameters(SecureRandom()))
        val keyPair = generator.generateKeyPair()

        return KeyPair(
            KeyCurve(Curve.X25519),
            PrivateKey(
                KeyCurve(Curve.X25519),
                (keyPair.private as X25519PrivateKeyParameters).encoded
            ),
            PublicKey(
                KeyCurve(Curve.X25519),
                (keyPair.public as X25519PublicKeyParameters).encoded
            )
        )
    }
}
