package io.iohk.atala.prism.walletsdk.apollo

import io.iohk.atala.prism.apollo.derivation.DerivationPath
import io.iohk.atala.prism.apollo.derivation.KeyDerivation
import io.iohk.atala.prism.apollo.derivation.MnemonicCode
import io.iohk.atala.prism.apollo.derivation.MnemonicLengthException
import io.iohk.atala.prism.apollo.ecdsa.ECDSAType
import io.iohk.atala.prism.apollo.ecdsa.KMMECDSA
import io.iohk.atala.prism.apollo.utils.KMMECSecp256k1PrivateKey
import io.iohk.atala.prism.apollo.utils.KMMECSecp256k1PublicKey
import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Apollo
import io.iohk.atala.prism.walletsdk.domain.models.ApolloError
import io.iohk.atala.prism.walletsdk.domain.models.CompressedPublicKey
import io.iohk.atala.prism.walletsdk.domain.models.Curve
import io.iohk.atala.prism.walletsdk.domain.models.KeyCurve
import io.iohk.atala.prism.walletsdk.domain.models.KeyPair
import io.iohk.atala.prism.walletsdk.domain.models.PrivateKey
import io.iohk.atala.prism.walletsdk.domain.models.PublicKey
import io.iohk.atala.prism.walletsdk.domain.models.Seed
import io.iohk.atala.prism.walletsdk.domain.models.SeedWords
import io.iohk.atala.prism.walletsdk.domain.models.Signature
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import org.bouncycastle.crypto.params.X25519PrivateKeyParameters
import kotlin.jvm.Throws

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
                val derivationPath = DerivationPath.fromPath("m/${curve.index}'/0'/0'")
                if (seed == null) {
                    throw ApolloError.InvalidMnemonicWord()
                }
                val extendedKey = KeyDerivation.deriveKey(seed.value, derivationPath)
                val kmmKeyPair = extendedKey.keyPair()
                val privateKey = kmmKeyPair.privateKey as KMMECSecp256k1PrivateKey
                val publicKey = kmmKeyPair.publicKey as KMMECSecp256k1PublicKey
                KeyPair(
                    keyCurve = curve,
                    privateKey = PrivateKey(
                        keyCurve = curve,
                        value = privateKey.getEncoded(),
                    ),
                    publicKey = PublicKey(
                        curve = curve,
                        value = publicKey.getEncoded(),
                    ),
                )
            }

            Curve.ED25519 -> {
                Ed25519.createKeyPair()
            }

            Curve.X25519 -> {
                X25519.createKeyPair()
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
        return when (privateKey.keyCurve.curve) {
            Curve.SECP256K1 -> {
                val kmmPrivateKey = KMMECSecp256k1PrivateKey.secp256k1FromBytes(privateKey.value)

                KeyPair(
                    keyCurve = privateKey.keyCurve,
                    privateKey = PrivateKey(
                        keyCurve = privateKey.keyCurve,
                        value = kmmPrivateKey.getEncoded(),
                    ),
                    publicKey = PublicKey(
                        curve = privateKey.keyCurve,
                        value = kmmPrivateKey.getPublicKey().getEncoded(),
                    ),
                )
            }

            Curve.ED25519 -> {
                val edPrivateKey = Ed25519PrivateKeyParameters(privateKey.value, 0)
                val edPublicKey = edPrivateKey.generatePublicKey()

                KeyPair(
                    keyCurve = privateKey.keyCurve,
                    privateKey = PrivateKey(
                        keyCurve = privateKey.keyCurve,
                        value = edPrivateKey.encoded,
                    ),
                    publicKey = PublicKey(
                        curve = privateKey.keyCurve,
                        value = edPublicKey.encoded,
                    ),
                )
            }

            Curve.X25519 -> {
                val xPrivateKey = X25519PrivateKeyParameters(privateKey.value, 0)
                val xPublicKey = xPrivateKey.generatePublicKey()

                KeyPair(
                    keyCurve = privateKey.keyCurve,
                    privateKey = PrivateKey(
                        keyCurve = privateKey.keyCurve,
                        value = xPrivateKey.encoded,
                    ),
                    publicKey = PublicKey(
                        curve = privateKey.keyCurve,
                        value = xPublicKey.encoded,
                    ),
                )
            }
        }
    }

    /**
     * Compresses a given public key into a shorter, more efficient form.
     *
     * @param publicKey The public key to compress.
     * @return [CompressedPublicKey]
     */
    override fun compressedPublicKey(publicKey: PublicKey): CompressedPublicKey {
        val kmmPublicKey = KMMECSecp256k1PublicKey.secp256k1FromBytes(publicKey.value)
        val kmmCompressed = kmmPublicKey.getEncodedCompressed()
        return CompressedPublicKey(
            uncompressed = publicKey,
            value = kmmCompressed,
        )
    }

    /**
     * Decompresses a given compressed public key into its original form.
     *
     * @param compressedData The compressed public key data.
     * @return [CompressedPublicKey]
     */
    override fun compressedPublicKey(compressedData: ByteArray): CompressedPublicKey {
        val kmmPublicKey = KMMECSecp256k1PublicKey.secp256k1FromCompressed(compressedData)
        val kmmCompressed = kmmPublicKey.getEncodedCompressed()
        val publicKey = PublicKey(
            curve = KeyCurve(Curve.SECP256K1),
            value = kmmPublicKey.getEncoded(),
        )
        return CompressedPublicKey(
            uncompressed = publicKey,
            value = kmmCompressed,
        )
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
                PublicKey(
                    curve = curve,
                    value = kmmPublicKey.getEncoded(),
                )
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
                PublicKey(
                    curve = curve,
                    value = kmmPublicKey.getEncoded(),
                )
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
        return when (privateKey.keyCurve.curve) {
            Curve.SECP256K1 -> {
                val kmmPrivateKey = KMMECSecp256k1PrivateKey.secp256k1FromBytes(privateKey.value)
                val kmmSignature = KMMECDSA.sign(
                    type = ECDSAType.ECDSA_SHA256,
                    data = message,
                    privateKey = kmmPrivateKey,
                )
                Signature(
                    value = kmmSignature,
                )
            }

            Curve.ED25519 -> {
                val signature = Ed25519.sign(
                    privateKey = privateKey,
                    message = message
                )
                Signature(
                    value = signature
                )
            }

            else -> {
                TODO()
            }
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
        return when (privateKey.keyCurve.curve) {
            Curve.SECP256K1 -> {
                signMessage(privateKey, message.encodeToByteArray())
            }

            Curve.ED25519 -> {
                val signature = Ed25519.sign(
                    privateKey = privateKey,
                    message = message.toByteArray()
                )
                Signature(
                    value = signature
                )
            }

            else -> {
                TODO()
            }
        }
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
        return when (publicKey.curve.curve) {
            Curve.SECP256K1 -> {
                val kmmPublicKey = KMMECSecp256k1PublicKey.secp256k1FromBytes(publicKey.value)
                KMMECDSA.verify(
                    type = ECDSAType.ECDSA_SHA256,
                    data = challenge,
                    publicKey = kmmPublicKey,
                    signature = signature.value,
                )
            }
            Curve.ED25519 -> {
                Ed25519.verify(publicKey, signature, challenge)
            }
            else -> {
                TODO()
            }
        }
    }
}
