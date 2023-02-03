package io.iohk.atala.prism.apollo

import io.iohk.atala.prism.domain.buildingBlocks.Apollo
import io.iohk.atala.prism.domain.models.CompressedPublicKey
import io.iohk.atala.prism.domain.models.Curve
import io.iohk.atala.prism.domain.models.KeyCurve
import io.iohk.atala.prism.domain.models.KeyPair
import io.iohk.atala.prism.domain.models.PrivateKey
import io.iohk.atala.prism.domain.models.PublicKey
import io.iohk.atala.prism.domain.models.Seed
import io.iohk.atala.prism.domain.models.Signature

class ApolloMock : Apollo {
    var createRandomMnemonicsReturn: Array<String> = emptyArray()
    var createSeedReturn: Seed = Seed(ByteArray(0))
    var createRandomSeedReturn: Pair<Array<String>, Seed> = Pair(emptyArray(), Seed(ByteArray(0)))
    var createKeyPairReturn: KeyPair = KeyPair(
        privateKey = PrivateKey(KeyCurve(Curve.ED25519), ByteArray(0)),
        publicKey = PublicKey(KeyCurve(Curve.ED25519), ByteArray(0))
    )
    var compressedPublicKeyReturn: CompressedPublicKey = CompressedPublicKey(
        PublicKey(KeyCurve(Curve.SECP256K1), ByteArray(0)),
        ByteArray(0)
    )
    var publicKeyReturn: PublicKey = PublicKey(KeyCurve(Curve.ED25519), ByteArray(0))
    var signMessageReturn: Signature = Signature(ByteArray(0))
    var verifySignatureReturn: Boolean = true
    var compressedPublicKeyDataReturn: CompressedPublicKey = CompressedPublicKey(
        PublicKey(KeyCurve(Curve.SECP256K1), ByteArray(0)),
        ByteArray(0)
    )
    var signMessageByteArrayReturn: Signature = Signature(ByteArray(0))
    var signMessageStringReturn: Signature = Signature(ByteArray(0))

    override fun createRandomMnemonics(): Array<String> = createRandomMnemonicsReturn

    override fun createSeed(mnemonics: Array<String>, passphrase: String): Seed {
        return createSeedReturn
    }

    override fun createRandomSeed(): Pair<Array<String>, Seed> {
        return createRandomSeedReturn
    }

    override fun createKeyPair(seed: Seed, curve: KeyCurve): KeyPair {
        return createKeyPairReturn
    }

    override fun createKeyPair(seed: Seed, privateKey: PrivateKey): KeyPair {
        return createKeyPairReturn
    }

    override fun compressedPublicKey(publicKey: PublicKey): CompressedPublicKey {
        return compressedPublicKeyReturn
    }
    override fun compressedPublicKey(compressedData: ByteArray): CompressedPublicKey =
        compressedPublicKeyDataReturn

    override fun publicKey(curve: String, x: ByteArray, y: ByteArray): PublicKey =
        publicKeyReturn

    override fun publicKey(curve: String, x: ByteArray): PublicKey =
        publicKeyReturn

    override fun signMessage(privateKey: PrivateKey, message: ByteArray): Signature =
        signMessageByteArrayReturn

    @Throws() // TODO: Add throw classes
    override fun signMessage(privateKey: PrivateKey, message: String): Signature =
        signMessageStringReturn

    override fun verifySignature(
        publicKey: PublicKey,
        challenge: ByteArray,
        signature: Signature
    ): Boolean = verifySignatureReturn
}
