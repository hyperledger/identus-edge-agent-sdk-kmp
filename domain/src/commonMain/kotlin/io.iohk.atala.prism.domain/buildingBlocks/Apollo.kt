package io.iohk.atala.prism.domain.buildingBlocks

import io.iohk.atala.prism.domain.models.CompressedPublicKey
import io.iohk.atala.prism.domain.models.KeyCurve
import io.iohk.atala.prism.domain.models.KeyPair
import io.iohk.atala.prism.domain.models.PrivateKey
import io.iohk.atala.prism.domain.models.PublicKey
import io.iohk.atala.prism.domain.models.Seed
import io.iohk.atala.prism.domain.models.Signature

interface Apollo {
    fun createRandomMnemonics(): Array<String>

    @Throws() // TODO: Add throw classes
    fun createSeed(mnemonics: Array<String>, passphrase: String): Seed

    fun createRandomSeed(): Pair<Array<String>, Seed>

    fun createKeyPair(seed: Seed, curve: KeyCurve): KeyPair

    @Throws() // TODO: Add throw classes
    fun createKeyPair(seed: Seed, privateKey: PrivateKey): KeyPair

    fun compressedPublicKey(publicKey: PublicKey): CompressedPublicKey

    fun compressedPublicKey(compressedData: ByteArray): CompressedPublicKey

    fun publicKey(curve: String, x: ByteArray, y: ByteArray): PublicKey
    fun publicKey(curve: String, x: ByteArray): PublicKey

    fun signMessage(privateKey: PrivateKey, message: ByteArray): Signature

    @Throws() // TODO: Add throw classes
    fun signMessage(privateKey: PrivateKey, message: String): Signature

    fun verifySignature(publicKey: PublicKey, challenge: ByteArray, signature: Signature): Boolean
}
