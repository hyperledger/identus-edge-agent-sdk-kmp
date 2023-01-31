package io.iohk.atala.prism.apollo

import io.iohk.atala.prism.domain.buildingBlocks.Apollo
import io.iohk.atala.prism.domain.models.CompressedPublicKey
import io.iohk.atala.prism.domain.models.KeyCurve
import io.iohk.atala.prism.domain.models.KeyPair
import io.iohk.atala.prism.domain.models.PrivateKey
import io.iohk.atala.prism.domain.models.PublicKey
import io.iohk.atala.prism.domain.models.Seed
import io.iohk.atala.prism.domain.models.Signature

class ApolloImpl: Apollo {
    override fun createRandomMnemonics(): Array<String> {
        TODO("Not yet implemented")
    }

    override fun createSeed(mnemonics: Array<String>, passphrase: String): Seed {
        TODO("Not yet implemented")
    }

    override fun createRandomSeed(): Pair<Array<String>, Seed> {
        TODO("Not yet implemented")
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