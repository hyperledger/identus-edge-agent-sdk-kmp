package io.iohk.atala.prism.walletsdk.apollo

import io.iohk.atala.prism.walletsdk.domain.models.Curve
import io.iohk.atala.prism.walletsdk.domain.models.KeyCurve
import io.iohk.atala.prism.walletsdk.domain.models.PrivateKey
import io.iohk.atala.prism.walletsdk.domain.models.PublicKey
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.KeyPair
import java.security.KeyPairGenerator
import io.iohk.atala.prism.walletsdk.domain.models.KeyPair as KeyPairModel

/**
 * Ed25519 is a variation of EdDSA
 * TODO(Future Moussa -> Use Apollo instead)
 */
actual object Ed25519 {
    actual fun createKeyPair(): KeyPairModel {
        val provider = BouncyCastleProvider()
        val generator = KeyPairGenerator.getInstance("Ed25519", provider)
        val javaKeyPair: KeyPair = generator.generateKeyPair()
        return KeyPairModel(
            KeyCurve(Curve.ED25519),
            PrivateKey(
                KeyCurve(Curve.ED25519),
                javaKeyPair.private.encoded
            ),
            PublicKey(
                KeyCurve(Curve.ED25519),
                javaKeyPair.public.encoded
            )
        )
    }
}
