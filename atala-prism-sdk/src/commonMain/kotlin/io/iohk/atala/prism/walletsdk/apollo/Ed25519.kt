package io.iohk.atala.prism.walletsdk.apollo

import io.iohk.atala.prism.walletsdk.domain.models.KeyPair
import io.iohk.atala.prism.walletsdk.domain.models.PrivateKey
import io.iohk.atala.prism.walletsdk.domain.models.PublicKey
import io.iohk.atala.prism.walletsdk.domain.models.Signature

/**
 * Ed25519 is a variation of EdDSA
 */
expect object Ed25519 {

    /**
     * Create keypair for [Ed25519].
     *
     * @return [KeyPair] for [Ed25519].
     */
    fun createKeyPair(): KeyPair

    /**
     * Sign the message with [Ed25519].
     *
     * @param privateKey key to be used in signing.
     * @param message message to sign in [ByteArray] form.
     * @return [ByteArray] representing the signature.
     */
    fun sign(privateKey: PrivateKey, message: ByteArray): ByteArray

    /**
     * Verify the signature against [Ed25519].
     *
     * @param publicKey key used in verifying.
     * @param signature the signature that we need to verify.
     * @param message the message that resulted in the provided signature.
     * @return [Boolean] whether this verifying was valid or not.
     */
    fun verify(publicKey: PublicKey, signature: Signature, message: ByteArray): Boolean
}
