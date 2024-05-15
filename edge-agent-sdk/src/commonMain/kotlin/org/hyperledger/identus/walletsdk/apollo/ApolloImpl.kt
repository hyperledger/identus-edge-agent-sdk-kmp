package org.hyperledger.identus.walletsdk.apollo

import io.iohk.atala.prism.apollo.derivation.HDKey
import io.iohk.atala.prism.apollo.derivation.MnemonicHelper
import io.iohk.atala.prism.apollo.derivation.MnemonicLengthException
import org.hyperledger.identus.walletsdk.apollo.helpers.BytesOps
import org.hyperledger.identus.walletsdk.apollo.utils.Ed25519KeyPair
import org.hyperledger.identus.walletsdk.apollo.utils.Ed25519PrivateKey
import org.hyperledger.identus.walletsdk.apollo.utils.Ed25519PublicKey
import org.hyperledger.identus.walletsdk.apollo.utils.Secp256k1PrivateKey
import org.hyperledger.identus.walletsdk.apollo.utils.Secp256k1PublicKey
import org.hyperledger.identus.walletsdk.apollo.utils.X25519KeyPair
import org.hyperledger.identus.walletsdk.apollo.utils.X25519PrivateKey
import org.hyperledger.identus.walletsdk.apollo.utils.X25519PublicKey
import org.hyperledger.identus.walletsdk.domain.buildingblocks.Apollo
import org.hyperledger.identus.walletsdk.domain.models.ApolloError
import org.hyperledger.identus.walletsdk.domain.models.Curve
import org.hyperledger.identus.walletsdk.domain.models.Seed
import org.hyperledger.identus.walletsdk.domain.models.SeedWords
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.CurveKey
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.DerivationPathKey
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.IndexKey
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.KeyTypes
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.PrivateKey
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.PublicKey
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.RawKey
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.SeedKey
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.StorableKey
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.TypeKey

/**
 * Apollo defines the set of cryptographic operations that are used in the Identus.
 */
class ApolloImpl : Apollo {

    /**
     * Creates a random set of mnemonic phrases that can be used as a seed for generating a private key.
     *
     * @return An array of mnemonic phrases.
     */
    override fun createRandomMnemonics(): Array<String> {
        return MnemonicHelper.createRandomMnemonics().toTypedArray()
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
        return Seed(MnemonicHelper.createSeed(mnemonics.asList(), passphrase))
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
                value = MnemonicHelper.createSeed(
                    mnemonics = mnemonics.asList(),
                    passphrase = passphrase ?: ""
                )
            )
        )
    }

    /**
     * Creates a private key based on the provided properties.
     *
     * @param properties A map of properties used to create the private key. The map should contain the following keys:
     *     - "type" (String): The type of the key ("EC" or "Curve25519").
     *     - "curve" (String): The curve of the key.
     *     - "rawKey" (ByteArray): The raw key data (optional).
     *     - "index" (Int): The index for the key (only applicable for EC keys with curve "secp256k1").
     *     - "derivationPath" (String): The derivation path for the key (only applicable for EC keys with curve "secp256k1").
     *     - "seed" (String): The seed for the key (only applicable for EC keys with curve "secp256k1").
     *
     * @return The created private key.
     *
     * @throws ApolloError.InvalidKeyType If the provided key type is invalid.
     * @throws ApolloError.InvalidKeyCurve If the provided key curve is invalid.
     * @throws ApolloError.InvalidRawData If the provided raw key data is invalid.
     * @throws ApolloError.InvalidIndex If the provided index is invalid.
     * @throws ApolloError.InvalidDerivationPath If the provided derivation path is invalid.
     * @throws ApolloError.InvalidSeed If the provided seed is invalid.
     */
    override fun createPrivateKey(properties: Map<String, Any>): PrivateKey {
        if (!properties.containsKey(TypeKey().property)) {
            throw ApolloError.InvalidKeyType(TypeKey().property)
        }
        if (!properties.containsKey(CurveKey().property)) {
            throw ApolloError.InvalidKeyCurve(CurveKey().property)
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
        throw ApolloError.InvalidKeyType(TypeKey().property)
    }

    /**
     * Checks if the provided data is associated with a private key identified by the given identifier.
     *
     * @param identifier The identifier for the private key.
     * @param data The data to check.
     * @return True if the data is associated with a private key, false otherwise.
     */
    override fun isPrivateKeyData(identifier: String, data: ByteArray): Boolean {
        return identifier.endsWith("priv")
    }

    /**
     * Checks if the provided data is associated with a public key identified by the given identifier.
     *
     * @param identifier The identifier for the public key.
     * @param data The data to check.
     * @return True if the data is associated with a public key, false otherwise.
     */
    override fun isPublicKeyData(identifier: String, data: ByteArray): Boolean {
        return identifier.endsWith("pub")
    }

    /**
     * Restores a private key based on the provided storable key.
     *
     * @param key The storable key to restore the private key from.
     * @return The restored private key.
     * @throws ApolloError.RestorationFailedNoIdentifierOrInvalid If the restoration identifier is missing or invalid.
     */
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

    /**
     * Restores a public key based on the provided storable key.
     *
     * @param key The storable key to restore the public key from.
     * @return The restored public key.
     * @throws ApolloError.RestorationFailedNoIdentifierOrInvalid If the restoration identifier is missing or invalid.
     */
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
