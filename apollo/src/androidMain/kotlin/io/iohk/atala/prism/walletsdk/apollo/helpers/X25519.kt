package io.iohk.atala.prism.walletsdk.apollo.helpers

import io.iohk.atala.prism.walletsdk.domain.models.Curve
import io.iohk.atala.prism.walletsdk.domain.models.KeyCurve
import io.iohk.atala.prism.walletsdk.domain.models.KeyPair
import io.iohk.atala.prism.walletsdk.domain.models.PrivateKey
import io.iohk.atala.prism.walletsdk.domain.models.PublicKey
import org.bouncycastle.jcajce.spec.XDHParameterSpec
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.KeyPairGenerator

/**
 * X25519
 */
actual object X25519 {

    actual fun createKeyPair(): KeyPair {
        val provider = BouncyCastleProvider()
        val kpg = KeyPairGenerator.getInstance("X25519", provider)
        kpg.initialize(XDHParameterSpec(XDHParameterSpec.X25519))
        val javaKeyPair = kpg.generateKeyPair()
        return KeyPair(
            KeyCurve(Curve.X25519),
            PrivateKey(
                KeyCurve(Curve.X25519),
                javaKeyPair.private.encoded
            ),
            PublicKey(
                KeyCurve(Curve.X25519),
                javaKeyPair.public.encoded
            )
        )
    }
}
