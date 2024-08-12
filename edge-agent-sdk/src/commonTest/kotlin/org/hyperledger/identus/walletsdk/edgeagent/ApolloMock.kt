package org.hyperledger.identus.walletsdk.edgeagent

import org.hyperledger.identus.walletsdk.apollo.utils.Ed25519KeyPair
import org.hyperledger.identus.walletsdk.apollo.utils.Ed25519PrivateKey
import org.hyperledger.identus.walletsdk.apollo.utils.Ed25519PublicKey
import org.hyperledger.identus.walletsdk.apollo.utils.Secp256k1PrivateKey
import org.hyperledger.identus.walletsdk.domain.buildingblocks.Apollo
import org.hyperledger.identus.walletsdk.domain.models.Seed
import org.hyperledger.identus.walletsdk.domain.models.SeedWords
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.JWK
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.Key
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.KeyPair
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.PrivateKey
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.PublicKey
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.StorableKey
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.StorablePrivateKey

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

    override fun createPublicKey(properties: Map<String, Any>): PublicKey {
        TODO("Not yet implemented")
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

    override fun restorePrivateKey(storablePrivateKey: StorablePrivateKey): PrivateKey {
        TODO("Not yet implemented")
    }

    override fun restorePrivateKey(restorationIdentifier: String, privateKeyData: String): PrivateKey {
        TODO("Not yet implemented")
    }

    override fun restorePublicKey(key: StorableKey): PublicKey {
        TODO("Not yet implemented")
    }

    override fun restoreKey(key: JWK, index: Int?): Key {
        TODO("Not yet implemented")
    }
}
