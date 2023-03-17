package io.iohk.atala.prism.walletsdk.apollo.helpers

import io.iohk.atala.prism.walletsdk.domain.models.Curve
import io.iohk.atala.prism.walletsdk.domain.models.KeyCurve
import io.iohk.atala.prism.walletsdk.domain.models.KeyPair
import io.iohk.atala.prism.walletsdk.domain.models.PrivateKey
import io.iohk.atala.prism.walletsdk.domain.models.PublicKey
import java.security.KeyPair as JavaKeyPair
import java.security.KeyPairGenerator

/**
 * Ed25519 is a variation of EdDSA
 */
actual object Ed25519 {
    actual fun createKeyPair(): KeyPair {
        val kpg: KeyPairGenerator = KeyPairGenerator.getInstance("Ed25519")
        val javaKeyPair: JavaKeyPair = kpg.generateKeyPair()
        val privateKey = PrivateKey(KeyCurve(Curve.ED25519), javaKeyPair.private.encoded)
        val publicKey = PublicKey(KeyCurve(Curve.ED25519), javaKeyPair.public.encoded)
        return KeyPair(KeyCurve(Curve.ED25519), privateKey, publicKey)
    }
}
