package io.iohk.atala.prism.walletsdk.apollo

import io.iohk.atala.prism.walletsdk.domain.models.Curve
import io.iohk.atala.prism.walletsdk.domain.models.KeyCurve
import io.iohk.atala.prism.walletsdk.domain.models.PrivateKey
import io.iohk.atala.prism.walletsdk.domain.models.PublicKey
import io.iohk.atala.prism.walletsdk.domain.models.Signature
import org.bouncycastle.crypto.generators.Ed25519KeyPairGenerator
import org.bouncycastle.crypto.params.Ed25519KeyGenerationParameters
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters
import org.bouncycastle.crypto.signers.Ed25519Signer
import java.security.SecureRandom
import java.util.*
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

    actual fun sign(privateKey: PrivateKey, message: ByteArray): ByteArray {
        val edPrivateKey = Ed25519PrivateKeyParameters(privateKey.value, 0)

        val signer = Ed25519Signer()
        signer.init(true, edPrivateKey)
        signer.update(message, 0, message.size)
        return signer.generateSignature()
    }

    actual fun verify(publicKey: PublicKey, signature: Signature, message: ByteArray): Boolean {
        val edPublicKey = Ed25519PublicKeyParameters(publicKey.value, 0)
        val verifier = Ed25519Signer()
        verifier.init(false, edPublicKey)
        verifier.update(message, 0, message.size)
        return verifier.verifySignature(signature.value)
    }
}
