package io.iohk.atala.prism.walletsdk.domain.buildingblocks

import io.iohk.atala.prism.apollo.derivation.MnemonicLengthException
import io.iohk.atala.prism.walletsdk.domain.models.CompressedPublicKey
import io.iohk.atala.prism.walletsdk.domain.models.KeyCurve
import io.iohk.atala.prism.walletsdk.domain.models.KeyPair
import io.iohk.atala.prism.walletsdk.domain.models.PrivateKey
import io.iohk.atala.prism.walletsdk.domain.models.PublicKey
import io.iohk.atala.prism.walletsdk.domain.models.Seed
import io.iohk.atala.prism.walletsdk.domain.models.SeedWords
import io.iohk.atala.prism.walletsdk.domain.models.Signature
import kotlin.jvm.Throws

/**
 * Apollo defines the set of cryptographic operations that are used in the Atala PRISM.
 */
interface Apollo {

    /**
     * Creates a random set of mnemonic phrases that can be used as a seed for generating
     * a private key.
     *
     * @return An array of mnemonic phrases.
     */
    fun createRandomMnemonics(): Array<String>

    /**
     * Takes in a set of mnemonics and a passphrase, and returns a seed object used to generate a private key.
     *
     * @param mnemonics An array of mnemonic phrases.
     * @param passphrase A passphrase used to enhance the security of the seed.
     * @return A seed object.
     * @throws [MnemonicLengthException] if the mnemonics or passphrase are invalid.
     */
    @Throws(MnemonicLengthException::class)
    fun createSeed(mnemonics: Array<String>, passphrase: String): Seed

    /**
     * Creates a random seed and a corresponding set of mnemonic phrases.
     *
     * @param passphrase A passphrase used to enhance the security of the seed.
     * @return [SeedWords].
     */
    fun createRandomSeed(passphrase: String? = ""): SeedWords

    /**
     * Creates a key pair (a private and public key) using a given seed and key curve.
     *
     * @param seed A seed object used to generate the key pair.
     * @param curve The key curve to use for generating the key pair.
     * @return A key pair object containing a private and public key.
     */
    // @JsName("createKeyPairFromKeyCurve")
    fun createKeyPair(seed: Seed? = null, curve: KeyCurve): KeyPair

    /**
     * Creates a key pair using a given seed and a specified private key.
     *
     * @param seed A seed object used to generate the key pair.
     * @param privateKey The private key to use for generating the key pair.
     * @return A [KeyPair] object containing a private and public key.
     */
    // @JsName("createKeyPairFromPrivateKey")
    fun createKeyPair(seed: Seed? = null, privateKey: PrivateKey): KeyPair

    /**
     * Compresses a given public key into a shorter, more efficient form.
     *
     * @param publicKey The public key to compress.
     * @return [CompressedPublicKey].
     */
    // @JsName("compressedPublicKeyFromPublicKey")
    fun compressedPublicKey(publicKey: PublicKey): CompressedPublicKey

    /**
     * Decompresses a given compressed public key into its original form.
     *
     * @param compressedData The compressed public key data.
     * @return [CompressedPublicKey].
     */
    // @JsName("compressedPublicKeyFromCompressedData")
    fun compressedPublicKey(compressedData: ByteArray): CompressedPublicKey

    /**
     * Create a public key from byte coordinates.
     *
     * @param curve key curve.
     * @param x x coordinate.
     * @param y y coordinate.
     * @return [PublicKey].
     */
    // @JsName("publicKeyFromPoints")
    fun publicKey(curve: KeyCurve, x: ByteArray, y: ByteArray): PublicKey

    /**
     * Create a public key from bytes.
     *
     * @param curve key curve.
     * @param x bytes.
     * @return [PublicKey].
     */
    // @JsName("publicKeyFromPoint")
    fun publicKey(curve: KeyCurve, x: ByteArray): PublicKey

    /**
     * Signs a message using a given private key, returning the signature.
     *
     * @param privateKey The private key to use for signing the message.
     * @param message The message to sign, in binary data form.
     * @return The signature of the message.
     */
    // @JsName("signByteArrayMessage")
    fun signMessage(privateKey: PrivateKey, message: ByteArray): Signature

    /**
     * Signs a message using a given private key, returning the signature.
     *
     * @param privateKey The private key to use for signing the message.
     * @param message The message to sign, in string form.
     * @return The signature of the message.
     */
    // @JsName("signStringMessage")
    fun signMessage(privateKey: PrivateKey, message: String): Signature

    /**
     * Verifies the authenticity of a signature using the corresponding public key, challenge, and
     * signature. This function returns a boolean value indicating whether the signature is valid or not.
     *
     * @param publicKey The public key associated with the signature.
     * @param challenge The challenge used to generate the signature.
     * @param signature The signature to verify.
     * @return A boolean value indicating whether the signature is valid or not.
     */
    fun verifySignature(publicKey: PublicKey, challenge: ByteArray, signature: Signature): Boolean
}
