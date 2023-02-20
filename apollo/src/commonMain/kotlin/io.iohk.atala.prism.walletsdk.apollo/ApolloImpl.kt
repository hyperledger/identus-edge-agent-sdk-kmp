package io.iohk.atala.prism.walletsdk.apollo

import io.iohk.atala.prism.apollo.derivation.DerivationPath
import io.iohk.atala.prism.apollo.derivation.KeyDerivation
import io.iohk.atala.prism.apollo.derivation.MnemonicCode
import io.iohk.atala.prism.apollo.utils.KMMECPublicKey
import io.iohk.atala.prism.walletsdk.domain.buildingBlocks.Apollo
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
        return when (curve) {
            KeyCurve(Curve.SECP256K1) -> {
                val extendedKey = KeyDerivation.deriveKey(seed.value, DerivationPath.fromPath(curve.index.toString()))
                val kmmKeyPair = extendedKey.keyPair()
                KeyPair(
                    keyCurve = curve,
                    privateKey = PrivateKey(
                        keyCurve = curve,
                        value = kmmKeyPair.privateKey.getEncoded(),
                    ),
                    publicKey = PublicKey(
                        curve = curve,
                        value = kmmKeyPair.publicKey.getEncoded(),
                    ),

                )
            }
            else -> TODO()
        }
    }

    override fun createKeyPair(seed: Seed, privateKey: PrivateKey): KeyPair {
        return when (val curve = privateKey.keyCurve) {
            KeyCurve(Curve.SECP256K1) -> {
                val extendedKey = KeyDerivation.deriveKey(seed.value, DerivationPath.fromPath(curve.index.toString()))
                val kmmKeyPair = extendedKey.keyPair()
                KeyPair(
                    keyCurve = curve,
                    privateKey = PrivateKey(
                        keyCurve = curve,
                        value = kmmKeyPair.privateKey.getEncoded(),
                    ),
                    publicKey = PublicKey(
                        curve = curve,
                        value = kmmKeyPair.publicKey.getEncoded(),
                    ),
                )
            }
            else -> TODO()
        }
    }

    override fun compressedPublicKey(publicKey: PublicKey): CompressedPublicKey {
        val kmmPublicKey = KMMECPublicKey.secp256k1FromBytes(publicKey.value)
        val kmmCompressed = kmmPublicKey.getEncodedCompressed()
        return CompressedPublicKey(
            uncompressed = publicKey,
            value = kmmCompressed,
        )
    }

    override fun compressedPublicKey(compressedData: ByteArray): CompressedPublicKey {
        val kmmPublicKey = KMMECPublicKey.secp256k1FromCompressed(compressedData)
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
        return when (curve) {
            KeyCurve(Curve.SECP256K1) -> {
                val kmmPublicKey = KMMECPublicKey.secp256k1FromByteCoordinates(x, y)
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
        return when (curve) {
            KeyCurve(Curve.SECP256K1) -> {
                val kmmPublicKey = KMMECPublicKey.secp256k1FromBytes(x)
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
        TODO("Not yet implemented")
    }

    override fun signMessage(privateKey: PrivateKey, message: String): Signature {
        TODO("Not yet implemented")
    }

    override fun verifySignature(publicKey: PublicKey, challenge: ByteArray, signature: Signature): Boolean {
        TODO("Not yet implemented")
    }
}
