package io.iohk.atala.prism.walletsdk.apollo

import io.iohk.atala.prism.apollo.derivation.KeyDerivation
import io.iohk.atala.prism.apollo.derivation.MnemonicCode
import io.iohk.atala.prism.walletsdk.domain.buildingBlocks.Apollo
import io.iohk.atala.prism.walletsdk.domain.models.CompressedPublicKey
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
        TODO("Not yet implemented")
    }

    override fun createKeyPair(seed: Seed, privateKey: PrivateKey): KeyPair {
        TODO("Not yet implemented")
    }

    override fun compressedPublicKey(publicKey: PublicKey): CompressedPublicKey {
        TODO("Not yet implemented")
    }

    override fun compressedPublicKey(compressedData: ByteArray): CompressedPublicKey {
        TODO("Not yet implemented")
    }

    override fun publicKey(curve: String, x: ByteArray, y: ByteArray): PublicKey {
        TODO("Not yet implemented")
    }

    override fun publicKey(curve: String, x: ByteArray): PublicKey {
        TODO("Not yet implemented")
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
