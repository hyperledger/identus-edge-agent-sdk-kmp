package io.iohk.atala.prism.walletsdk.apollo

import io.iohk.atala.prism.apollo.derivation.HDKey
import io.iohk.atala.prism.apollo.derivation.Mnemonic
import io.iohk.atala.prism.apollo.derivation.MnemonicLengthException
import io.iohk.atala.prism.walletsdk.apollo.helpers.BytesOps
import io.iohk.atala.prism.walletsdk.apollo.utils.Ed25519KeyPair
import io.iohk.atala.prism.walletsdk.apollo.utils.Ed25519PrivateKey
import io.iohk.atala.prism.walletsdk.apollo.utils.Ed25519PublicKey
import io.iohk.atala.prism.walletsdk.apollo.utils.Secp256k1PrivateKey
import io.iohk.atala.prism.walletsdk.apollo.utils.Secp256k1PublicKey
import io.iohk.atala.prism.walletsdk.apollo.utils.X25519KeyPair
import io.iohk.atala.prism.walletsdk.apollo.utils.X25519PrivateKey
import io.iohk.atala.prism.walletsdk.apollo.utils.X25519PublicKey
import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Apollo
import io.iohk.atala.prism.walletsdk.domain.models.ApolloError
import io.iohk.atala.prism.walletsdk.domain.models.Curve
import io.iohk.atala.prism.walletsdk.domain.models.Seed
import io.iohk.atala.prism.walletsdk.domain.models.SeedWords
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.CurveKey
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.DerivationPathKey
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.IndexKey
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.KeyTypes
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.PrivateKey
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.PublicKey
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.RawKey
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.SeedKey
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.StorableKey
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.TypeKey

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
        return Mnemonic.createRandomMnemonics().toTypedArray()
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
        return Seed(Mnemonic.createSeed(mnemonics.asList(), passphrase))
    }

    /**
     * Creates a random seed and a corresponding set of mnemonic phrases.
     *
     * @param passphrase A passphrase used to enhance the security of the seed.
     * @return [SeedWords].
     */
    override fun createRandomSeed(passphrase: String?): SeedWords {
        val mnemonics = createRandomMnemonics()
        return SeedWords(
            mnemonics,
            Seed(
                value = Mnemonic.createSeed(
                    mnemonics = mnemonics.asList(),
                    passphrase = passphrase ?: ""
                )
            )
        )
    }

    override fun createPrivateKey(properties: Map<String, Any>): PrivateKey {
        if (!properties.containsKey(TypeKey().property)) {
            throw ApolloError.InvalidKeyType(TypeKey().property, KeyTypes.values().map { it.type }.toTypedArray())
        }
        if (!properties.containsKey(CurveKey().property)) {
            throw ApolloError.InvalidKeyCurve(CurveKey().property, Curve.values().map { it.value }.toTypedArray())
        }

        val keyType = properties[TypeKey().property]
        val curve = properties[CurveKey().property]

        val keyData = properties[RawKey().property]

        when (keyType) {
            KeyTypes.EC -> {
                when (curve) {
                    Curve.ED25519.value -> {
                        keyData?.let {
                            if (it !is ByteArray) {
                                throw ApolloError.InvalidRawData("KeyData must be a ByteArray")
                            }
                            return Ed25519PrivateKey(it)
                        }
                        val keyPair = Ed25519KeyPair.generateKeyPair()
                        return keyPair.privateKey
                    }

                    Curve.SECP256K1.value -> {
                        keyData?.let {
                            if (it !is ByteArray) {
                                throw Exception("KeyData must be a ByteArray")
                            }
                            return Secp256k1PrivateKey(it)
                        }
                        val index = properties[IndexKey().property] ?: 0
                        if (index !is Int) {
                            throw ApolloError.InvalidIndex("Index must be an integer")
                        }
                        val derivationPath =
                            if (properties[DerivationPathKey().property] != null && properties[DerivationPathKey().property] !is String) {
                                throw ApolloError.InvalidDerivationPath("Derivation path must be a string")
                            } else {
                                "m/$index'/0'/0'"
                            }

                        val seed = properties[SeedKey().property] ?: throw Exception("Seed must provide a seed")
                        if (seed !is String) {
                            throw ApolloError.InvalidSeed("Seed must be a string")
                        }

                        val seedByteArray = BytesOps.hexToBytes(seed)

                        val hdKey = HDKey(seedByteArray, 0, 0)
                        val derivedHdKey = hdKey.derive(derivationPath)
                        val private = Secp256k1PrivateKey(derivedHdKey.getKMMSecp256k1PrivateKey().raw)
                        private.keySpecification[SeedKey().property] = seed
                        private.keySpecification[DerivationPathKey().property] = derivationPath
                        private.keySpecification[IndexKey().property] = "0"
                        return private
                    }
                }
            }

            KeyTypes.Curve25519 -> {
                keyData?.let {
                    if (it !is ByteArray) {
                        throw ApolloError.InvalidRawData("KeyData must be a ByteArray")
                    }
                    return X25519PrivateKey(it)
                }
                val keyPair = X25519KeyPair.generateKeyPair()
                return keyPair.privateKey
            }
        }
        throw ApolloError.InvalidKeyType(TypeKey().property, KeyTypes.values().map { it.type }.toTypedArray())
    }

    override fun isPrivateKeyData(identifier: String, data: ByteArray): Boolean {
        return identifier.endsWith("priv")
    }

    override fun isPublicKeyData(identifier: String, data: ByteArray): Boolean {
        return identifier.endsWith("pub")
    }

    override fun restorePrivateKey(key: StorableKey): PrivateKey {
        return when (key.restorationIdentifier) {
            "secp256k1+priv" -> {
                Secp256k1PrivateKey(key.storableData)
            }

            "x25519+priv" -> {
                X25519PrivateKey(key.storableData)
            }

            "ed25519+priv" -> {
                Ed25519PrivateKey(key.storableData)
            }

            else -> {
                throw ApolloError.RestorationFailedNoIdentifierOrInvalid()
            }
        }
    }

    override fun restorePublicKey(key: StorableKey): PublicKey {
        return when (key.restorationIdentifier) {
            "secp256k1+pub" -> {
                Secp256k1PublicKey(key.storableData)
            }

            "x25519+pub" -> {
                X25519PublicKey(key.storableData)
            }

            "ed25519+pub" -> {
                Ed25519PublicKey(key.storableData)
            }

            else -> {
                throw ApolloError.RestorationFailedNoIdentifierOrInvalid()
            }
        }
    }
}
