package io.iohk.atala.prism.walletsdk.prismagent

import io.iohk.atala.prism.walletsdk.apollo.utils.Ed25519KeyPair
import io.iohk.atala.prism.walletsdk.apollo.utils.Ed25519PrivateKey
import io.iohk.atala.prism.walletsdk.apollo.utils.Ed25519PublicKey
import io.iohk.atala.prism.walletsdk.apollo.utils.Secp256k1PrivateKey
import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Apollo
import io.iohk.atala.prism.walletsdk.domain.models.Seed
import io.iohk.atala.prism.walletsdk.domain.models.SeedWords
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.KeyPair
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.PrivateKey
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.PublicKey
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.StorableKey

class ApolloMock : Apollo {
    var createRandomMnemonicsReturn: Array<String> = emptyArray()
    var createSeedReturn: Seed = Seed(ByteArray(0))
    var createRandomSeedReturn: SeedWords = SeedWords(emptyArray(), Seed(ByteArray(0)))
    var createKeyPairReturn: KeyPair = Ed25519KeyPair(
        privateKey = Ed25519PrivateKey(ByteArray(0)),
        publicKey = Ed25519PublicKey(ByteArray(0))
    )
    var createPrivateKey: PrivateKey? = null

    override fun createRandomMnemonics(): Array<String> = createRandomMnemonicsReturn

    override fun createSeed(mnemonics: Array<String>, passphrase: String): Seed {
        return createSeedReturn
    }

    override fun createRandomSeed(passphrase: String?): SeedWords {
        return createRandomSeedReturn
    }

    override fun createPrivateKey(properties: Map<String, Any>): PrivateKey {
        return createPrivateKey ?: Secp256k1PrivateKey(ByteArray(0))
    }

    override fun isPrivateKeyData(identifier: String, data: ByteArray): Boolean {
        TODO("Not yet implemented")
    }

    override fun isPublicKeyData(identifier: String, data: ByteArray): Boolean {
        TODO("Not yet implemented")
    }

    override fun restorePrivateKey(key: StorableKey): PrivateKey {
        TODO("Not yet implemented")
    }

    override fun restorePublicKey(key: StorableKey): PublicKey {
        TODO("Not yet implemented")
    }
}
