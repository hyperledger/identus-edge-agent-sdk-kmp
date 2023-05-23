package io.iohk.atala.prism.walletsdk.apollo

import io.iohk.atala.prism.walletsdk.domain.models.Curve
import io.iohk.atala.prism.walletsdk.domain.models.KeyCurve
import io.iohk.atala.prism.walletsdk.domain.models.KeyPair
import io.iohk.atala.prism.walletsdk.domain.models.PrivateKey
import io.iohk.atala.prism.walletsdk.domain.models.PublicKey
import io.iohk.atala.prism.walletsdk.domain.models.Signature
import org.bouncycastle.crypto.generators.Ed25519KeyPairGenerator
import org.bouncycastle.crypto.params.Ed25519KeyGenerationParameters
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters
import org.bouncycastle.crypto.signers.Ed25519Signer
import java.security.SecureRandom

/**
 * Ed25519 is a variation of EdDSA
 */
actual object Ed25519 {

    /**
     * Create keypair for [Ed25519].
     *
     * @return [KeyPair] for [Ed25519].
     */
    actual fun createKeyPair(): KeyPair {
        val generator = Ed25519KeyPairGenerator()
        generator.init(Ed25519KeyGenerationParameters(SecureRandom()))
        val keyPair = generator.generateKeyPair()

        return KeyPair(
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

    /**
     * Sign the message with [Ed25519].
     *
     * @param privateKey key to be used in signing.
     * @param message message to sign in [ByteArray] form.
     * @return [ByteArray] representing the signature.
     */
    actual fun sign(privateKey: PrivateKey, message: ByteArray): ByteArray {
        val edPrivateKey = Ed25519PrivateKeyParameters(privateKey.value, 0)

        val signer = Ed25519Signer()
        signer.init(true, edPrivateKey)
        signer.update(message, 0, message.size)
        return signer.generateSignature()
    }

    /**
     * Verify the signature against [Ed25519].
     *
     * @param publicKey key used in verifying.
     * @param signature the signature that we need to verify.
     * @param message the message that resulted in the provided signature.
     * @return [Boolean] whether this verifying was valid or not.
     */
    actual fun verify(publicKey: PublicKey, signature: Signature, message: ByteArray): Boolean {
        val edPublicKey = Ed25519PublicKeyParameters(publicKey.value, 0)
        val verifier = Ed25519Signer()
        verifier.init(false, edPublicKey)
        verifier.update(message, 0, message.size)
        return verifier.verifySignature(signature.value)
    }
}
