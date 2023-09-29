package io.iohk.atala.prism.walletsdk.castor

import io.iohk.atala.prism.walletsdk.apollo.utils.Ed25519KeyPair
import io.iohk.atala.prism.walletsdk.apollo.utils.Ed25519PrivateKey
import io.iohk.atala.prism.walletsdk.apollo.utils.Ed25519PublicKey
import io.iohk.atala.prism.walletsdk.apollo.utils.Secp256k1PublicKey
import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Apollo
import io.iohk.atala.prism.walletsdk.domain.models.Seed
import io.iohk.atala.prism.walletsdk.domain.models.SeedWords
import io.iohk.atala.prism.walletsdk.domain.models.Signature
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.KeyPair
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.PrivateKey
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.PublicKey

class ApolloMock : Apollo {
    var createRandomMnemonicsReturn: Array<String> = emptyArray()
    var createSeedReturn: Seed = Seed(ByteArray(0))
    var createRandomSeedReturn: SeedWords = SeedWords(emptyArray(), Seed(ByteArray(0)))
    var createKeyPairReturn: KeyPair = Ed25519KeyPair(
        privateKey = Ed25519PrivateKey(ByteArray(0)),
        publicKey = Ed25519PublicKey(ByteArray(0))
    )
    var compressedPublicKeyReturn: PublicKey = Secp256k1PublicKey(ByteArray(0))
    var publicKeyReturn: PublicKey = Ed25519PublicKey(ByteArray(0))
    var signMessageReturn: Signature = Signature(ByteArray(0))
    var verifySignatureReturn: Boolean = true
    var compressedPublicKeyDataReturn: PublicKey = Secp256k1PublicKey(ByteArray(0))
    var signMessageByteArrayReturn: Signature = Signature(ByteArray(0))
    var signMessageStringReturn: Signature = Signature(ByteArray(0))

    override fun createRandomMnemonics(): Array<String> = createRandomMnemonicsReturn

    override fun createSeed(mnemonics: Array<String>, passphrase: String): Seed {
        return createSeedReturn
    }

    override fun createRandomSeed(passphrase: String?): SeedWords {
        return createRandomSeedReturn
    }

    override fun createPrivateKey(properties: Map<String, Any>): PrivateKey {
        TODO("Not yet implemented")
    }
}
