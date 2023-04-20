package io.iohk.atala.prism.walletsdk.apollo

import io.iohk.atala.prism.apollo.derivation.DerivationPath
import io.iohk.atala.prism.apollo.derivation.KeyDerivation
import io.iohk.atala.prism.apollo.derivation.MnemonicCode
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

class ApolloImpl : Apollo {
    override fun createRandomMnemonics(): Array<String> {
        return KeyDerivation.randomMnemonicCode().words.toTypedArray()
    }

    override fun createSeed(mnemonics: Array<String>, passphrase: String): Seed {
        val mnemonicCode = MnemonicCode(mnemonics.toList())
        return Seed(
            value = KeyDerivation.binarySeed(
                seed = mnemonicCode,
                passphrase = passphrase,
            ),
        )
    }

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

    override fun createKeyPair(seed: Seed, curve: KeyCurve): KeyPair {
        return when (curve.curve) {
            Curve.SECP256K1 -> {
                val derivationPath = DerivationPath.fromPath("m/${curve.index}'/0'/0'")
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

    override fun createKeyPair(seed: Seed, privateKey: PrivateKey): KeyPair {
        return when (privateKey.keyCurve.curve) {
            Curve.SECP256K1 -> {
                val derivationPath = DerivationPath.fromPath("m/${privateKey.keyCurve.index}'/0'/0'")
                val extendedKey = KeyDerivation.deriveKey(seed.value, derivationPath)
                val kmmKeyPair = extendedKey.keyPair()
                val mPrivateKey = kmmKeyPair.privateKey as KMMECSecp256k1PrivateKey
                val mPublicKey = kmmKeyPair.publicKey as KMMECSecp256k1PublicKey
                KeyPair(
                    keyCurve = privateKey.keyCurve,
                    privateKey = PrivateKey(
                        keyCurve = privateKey.keyCurve,
                        value = mPrivateKey.getEncoded(),
                    ),
                    publicKey = PublicKey(
                        curve = privateKey.keyCurve,
                        value = mPublicKey.getEncoded(),
                    ),
                )
            }
            Curve.ED25519 -> TODO()
            Curve.X25519 -> TODO()
        }
    }

    override fun compressedPublicKey(publicKey: PublicKey): CompressedPublicKey {
        val kmmPublicKey = KMMECSecp256k1PublicKey.secp256k1FromBytes(publicKey.value)
        val kmmCompressed = kmmPublicKey.getEncodedCompressed()
        return CompressedPublicKey(
            uncompressed = publicKey,
            value = kmmCompressed,
        )
    }

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
            else -> {
                TODO()
            }
        }
    }

    override fun signMessage(privateKey: PrivateKey, message: String): Signature {
        return when (privateKey.keyCurve.curve) {
            Curve.SECP256K1 -> {
                signMessage(privateKey, message.encodeToByteArray())
            }
            else -> {
                TODO()
            }
        }
    }

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
            else -> {
                TODO()
            }
        }
    }
}
