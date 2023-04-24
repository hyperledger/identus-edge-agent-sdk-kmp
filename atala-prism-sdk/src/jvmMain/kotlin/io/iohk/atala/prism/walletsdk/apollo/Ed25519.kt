package io.iohk.atala.prism.walletsdk.apollo

import io.iohk.atala.prism.walletsdk.domain.models.Curve
import io.iohk.atala.prism.walletsdk.domain.models.KeyCurve
import io.iohk.atala.prism.walletsdk.domain.models.PrivateKey
import io.iohk.atala.prism.walletsdk.domain.models.PublicKey
import org.bouncycastle.crypto.generators.Ed25519KeyPairGenerator
import org.bouncycastle.crypto.params.Ed25519KeyGenerationParameters
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters
import java.security.SecureRandom
import io.iohk.atala.prism.walletsdk.domain.models.KeyPair as KeyPairModel

/**
 * Ed25519 is a variation of EdDSA
 */
actual object Ed25519 {
    actual fun createKeyPair(): KeyPairModel {
        val generator = Ed25519KeyPairGenerator()
        generator.init(Ed25519KeyGenerationParameters(SecureRandom()))
        val keyPair = generator.generateKeyPair()

        return KeyPairModel(
            KeyCurve(Curve.ED25519),
            PrivateKey(
                KeyCurve(Curve.ED25519),
                (keyPair.private as Ed25519PrivateKeyParameters).encoded
            ),
            PublicKey(
                KeyCurve(Curve.ED25519),
                (keyPair.public as Ed25519PublicKeyParameters).encoded
            )
        )
    }
}
