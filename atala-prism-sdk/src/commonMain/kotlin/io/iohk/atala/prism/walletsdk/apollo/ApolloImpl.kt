package io.iohk.atala.prism.walletsdk.apollo

import io.iohk.atala.prism.apollo.derivation.KeyDerivation
import io.iohk.atala.prism.apollo.derivation.MnemonicCode
import io.iohk.atala.prism.apollo.derivation.MnemonicLengthException
import io.iohk.atala.prism.apollo.utils.KMMECSecp256k1PrivateKey
import io.iohk.atala.prism.apollo.utils.KMMECSecp256k1PublicKey
import io.iohk.atala.prism.walletsdk.apollo.utils.Ed25519KeyPair
import io.iohk.atala.prism.walletsdk.apollo.utils.Ed25519PrivateKey
import io.iohk.atala.prism.walletsdk.apollo.utils.Ed25519PublicKey
import io.iohk.atala.prism.walletsdk.apollo.utils.Secp256k1KeyPair
import io.iohk.atala.prism.walletsdk.apollo.utils.Secp256k1PrivateKey
import io.iohk.atala.prism.walletsdk.apollo.utils.Secp256k1PublicKey
import io.iohk.atala.prism.walletsdk.apollo.utils.X25519KeyPair
import io.iohk.atala.prism.walletsdk.apollo.utils.X25519PrivateKey
import io.iohk.atala.prism.walletsdk.apollo.utils.X25519PublicKey
import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Apollo
import io.iohk.atala.prism.walletsdk.domain.models.ApolloError
import io.iohk.atala.prism.walletsdk.domain.models.CompressedPublicKey
import io.iohk.atala.prism.walletsdk.domain.models.Curve
import io.iohk.atala.prism.walletsdk.domain.models.KeyCurve
import io.iohk.atala.prism.walletsdk.domain.models.Seed
import io.iohk.atala.prism.walletsdk.domain.models.SeedWords
import io.iohk.atala.prism.walletsdk.domain.models.Signature
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.KeyPair
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.PrivateKey
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.PublicKey
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import org.bouncycastle.crypto.params.X25519PrivateKeyParameters

/**
 * Apollo defines the set of cryptographic operations that are used in the Atala PRISM.
 */
class ApolloImpl : Apollo {

    /**
     * Creates a random set of mnemonic phrases that can be used as a seed for generating a private key.
     *
     * @return An array of mnemonic phrases.
     */
    override fun createRandomMnemonics(): Array<String> {
        return KeyDerivation.randomMnemonicCode().words.toTypedArray()
    }

    /**
     * Takes in a set of mnemonics and a passphrase, and returns a seed object used to generate a private key.
     *
     * @param mnemonics An array of mnemonic phrases.
     * @param passphrase A passphrase used to enhance the security of the seed.
     * @return A seed object.
     * @throws [MnemonicLengthException] if the mnemonics or passphrase are invalid.
     */
    @Throws(MnemonicLengthException::class)
    override fun createSeed(mnemonics: Array<String>, passphrase: String): Seed {
        val mnemonicCode = MnemonicCode(mnemonics.toList())
        return Seed(
            value = KeyDerivation.binarySeed(
                seed = mnemonicCode,
                passphrase = passphrase,
            ),
        )
    }

    /**
     * Creates a random seed and a corresponding set of mnemonic phrases.
     *
     * @param passphrase A passphrase used to enhance the security of the seed.
     * @return [SeedWords].
     */
    override fun createRandomSeed(passphrase: String?): SeedWords {
        val mnemonics = createRandomMnemonics()
        val mnemonicCode = MnemonicCode(mnemonics.toList())
        return SeedWords(
            mnemonics,
            Seed(
                value = KeyDerivation.binarySeed(
                    seed = mnemonicCode,
                    passphrase = passphrase ?: "",
                ),
            ),
        )
    }

    /**
     * Creates a key pair (a private and public key) using a given seed and key curve.
     *
     * @param seed A seed object used to generate the key pair.
     * @param curve The key curve to use for generating the key pair.
     * @return A key pair object containing a private and public key.
     */
    override fun createKeyPair(seed: Seed?, curve: KeyCurve): KeyPair {
        return when (curve.curve) {
            Curve.SECP256K1 -> {
                Secp256k1KeyPair.generateKeyPair(seed, curve)
            }

            Curve.ED25519 -> {
                Ed25519KeyPair.generateKeyPair()
            }

            Curve.X25519 -> {
                X25519KeyPair.generateKeyPair()
            }
        }
    }

    /**
     * Creates a key pair using a given seed and a specified private key.
     *
     * @param seed A seed object used to generate the key pair.
     * @param privateKey The private key to use for generating the key pair.
     * @return A [KeyPair] object containing a private and public key.
     */
    override fun createKeyPair(seed: Seed?, privateKey: PrivateKey): KeyPair {
        return when (privateKey.getCurve()) {
            Curve.SECP256K1.value -> {
                val key = privateKey as Secp256k1PrivateKey
                val kmmPrivateKey = KMMECSecp256k1PrivateKey.secp256k1FromBytes(key.getValue())
                Secp256k1KeyPair(
                    privateKey = Secp256k1PrivateKey(kmmPrivateKey.getEncoded()),
                    publicKey = Secp256k1PublicKey(kmmPrivateKey.getPublicKey().getEncoded())
                )
            }

            Curve.ED25519.value -> {
                val key = privateKey as Ed25519PrivateKey
                val edPrivateKey = Ed25519PrivateKeyParameters(key.getValue(), 0)
                val edPublicKey = edPrivateKey.generatePublicKey()

                Ed25519KeyPair(
                    privateKey = Ed25519PrivateKey(edPrivateKey.encoded),
                    publicKey = Ed25519PublicKey(edPublicKey.encoded)
                )
            }

            Curve.X25519.value -> {
                val key = privateKey as X25519PrivateKey
                val xPrivateKey = X25519PrivateKeyParameters(key.getValue(), 0)
                val xPublicKey = xPrivateKey.generatePublicKey()

                X25519KeyPair(
                    privateKey = X25519PrivateKey(xPrivateKey.encoded),
                    publicKey = X25519PublicKey(xPublicKey.encoded)
                )
            }

            else -> {
                throw ApolloError.InvalidKeyCurve()
            }
        }
    }

    /**
     * Compresses a given public key into a shorter, more efficient form.
     *
     * @param publicKey The public key to compress.
     * @return [CompressedPublicKey]
     */
    override fun compressedPublicKey(publicKey: PublicKey): PublicKey {
        val compressedRaw = (publicKey as Secp256k1PublicKey).getEncodedCompressed()
        return Secp256k1PublicKey(compressedRaw)
    }

    /**
     * Decompresses a given compressed public key into its original form.
     *
     * @param compressedData The compressed public key data.
     * @return [CompressedPublicKey]
     */
    override fun compressedPublicKey(compressedData: ByteArray): PublicKey {
        return Secp256k1PublicKey.secp256k1FromCompressed(compressedData)
    }

    /**
     * Create a public key from byte coordinates.
     *
     * @param curve key curve.
     * @param x x coordinate.
     * @param y y coordinate.
     * @return [PublicKey].
     */
    override fun publicKey(curve: KeyCurve, x: ByteArray, y: ByteArray): PublicKey {
        return when (curve.curve) {
            Curve.SECP256K1 -> {
                val kmmPublicKey = KMMECSecp256k1PublicKey.secp256k1FromByteCoordinates(x, y)
                Secp256k1PublicKey(kmmPublicKey.getEncoded())
            }

            else -> {
                // Only SECP256K1 can be initialised by using byte Coordinates for EC Curve
                throw ApolloError.InvalidKeyCurve()
            }
        }
    }

    /**
     * Create a public key from bytes.
     *
     * @param curve key curve.
     * @param x bytes.
     * @return [PublicKey].
     */
    override fun publicKey(curve: KeyCurve, x: ByteArray): PublicKey {
        return when (curve.curve) {
            Curve.SECP256K1 -> {
                val kmmPublicKey = KMMECSecp256k1PublicKey.secp256k1FromBytes(x)
                Secp256k1PublicKey(kmmPublicKey.getEncoded())
            }

            else -> {
                // Other type of keys are initialised using a ByteArray, for now we just support SECP256K1
                TODO()
            }
        }
    }

    /**
     * Signs a message using a given private key, returning the signature.
     *
     * @param privateKey The private key to use for signing the message.
     * @param message The message to sign, in binary data form.
     * @return The signature of the message.
     */
    override fun signMessage(privateKey: PrivateKey, message: ByteArray): Signature {
        if (privateKey.isSignable()) {
            return when (privateKey.getCurve()) {
                Curve.SECP256K1.value -> {
                    val key = privateKey as Secp256k1PrivateKey
                    val signature = key.sign(message)

                    Signature(
                        value = signature,
                    )
                }

                Curve.ED25519.value -> {
                    val key = privateKey as Ed25519PrivateKey
                    val signature = key.sign(message)

                    Signature(
                        value = signature
                    )
                }

                else -> {
                    TODO()
                }
            }
        } else {
            throw ApolloError.InvalidKeyCurve()
        }
    }

    /**
     * Signs a message using a given private key, returning the signature.
     *
     * @param privateKey The private key to use for signing the message.
     * @param message The message to sign, in string form.
     * @return The signature of the message.
     */
    override fun signMessage(privateKey: PrivateKey, message: String): Signature {
        return signMessage(privateKey, message.encodeToByteArray())
    }

    /**
     * Verifies the authenticity of a signature using the corresponding public key, challenge, and
     * signature. This function returns a boolean value indicating whether the signature is valid or not.
     *
     * @param publicKey The public key associated with the signature.
     * @param challenge The challenge used to generate the signature.
     * @param signature The signature to verify.
     * @return A boolean value indicating whether the signature is valid or not.
     */
    override fun verifySignature(publicKey: PublicKey, challenge: ByteArray, signature: Signature): Boolean {
        if (publicKey.canVerify()) {
            return when (publicKey.getCurve()) {
                Curve.SECP256K1.value -> {
                    val key = publicKey as Secp256k1PublicKey
                    key.verify(challenge, signature.value)
                }

                Curve.ED25519.value -> {
                    val key = publicKey as Ed25519PublicKey
                    key.verify(challenge, signature.value)
                }

                else -> {
                    TODO()
                }
            }
        } else {
            throw ApolloError.InvalidKeyCurve()
        }
    }
}
