package org.hyperledger.identus.walletsdk.apollo

import org.hyperledger.identus.apollo.base64.base64UrlDecodedBytes
import org.hyperledger.identus.apollo.derivation.DerivationPath
import org.hyperledger.identus.apollo.derivation.EdHDKey
import org.hyperledger.identus.apollo.derivation.HDKey
import org.hyperledger.identus.apollo.derivation.MnemonicHelper
import org.hyperledger.identus.apollo.derivation.MnemonicLengthException
import org.hyperledger.identus.apollo.utils.KMMECSecp256k1PublicKey
import org.hyperledger.identus.apollo.utils.KMMEdPrivateKey
import org.hyperledger.identus.walletsdk.apollo.utils.Ed25519KeyPair
import org.hyperledger.identus.walletsdk.apollo.utils.Ed25519PrivateKey
import org.hyperledger.identus.walletsdk.apollo.utils.Ed25519PublicKey
import org.hyperledger.identus.walletsdk.apollo.utils.KeyUsage
import org.hyperledger.identus.walletsdk.apollo.utils.PrismDerivationPath
import org.hyperledger.identus.walletsdk.apollo.utils.Secp256k1KeyPair
import org.hyperledger.identus.walletsdk.apollo.utils.Secp256k1PrivateKey
import org.hyperledger.identus.walletsdk.apollo.utils.Secp256k1PublicKey
import org.hyperledger.identus.walletsdk.apollo.utils.X25519KeyPair
import org.hyperledger.identus.walletsdk.apollo.utils.X25519PrivateKey
import org.hyperledger.identus.walletsdk.apollo.utils.X25519PublicKey
import org.hyperledger.identus.walletsdk.domain.buildingblocks.Apollo
import org.hyperledger.identus.walletsdk.domain.models.ApolloError
import org.hyperledger.identus.walletsdk.domain.models.Curve
import org.hyperledger.identus.walletsdk.domain.models.PlutoError
import org.hyperledger.identus.walletsdk.domain.models.Seed
import org.hyperledger.identus.walletsdk.domain.models.SeedWords
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.CurveKey
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.CurvePointXKey
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.CurvePointYKey
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.DerivationPathKey
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.IndexKey
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.JWK
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.Key
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.KeyTypes
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.PrivateKey
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.PublicKey
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.RawKey
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.SeedKey
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.StorableKey
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.TypeKey
import org.hyperledger.identus.walletsdk.logger.LogComponent
import org.hyperledger.identus.walletsdk.logger.LogLevel
import org.hyperledger.identus.walletsdk.logger.Logger
import org.hyperledger.identus.walletsdk.logger.LoggerImpl

/**
 * Apollo defines the set of cryptographic operations that are used in the Atala PRISM.
 */
class ApolloImpl(
    val logger: Logger = LoggerImpl(LogComponent.APOLLO)
) : Apollo {

    /**
     * Creates a random set of mnemonic phrases that can be used as a seed for generating a private key.
     *
     * @return An array of mnemonic phrases.
     */
    override fun createRandomMnemonics(): Array<String> {
        LogComponent.APOLLO.logLevel = LogLevel.INFO
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
     * @param properties The map of properties used to create the private key.
     *                   The properties should include the following keys:
     *                   - `Type`: The type of the key (e.g., "EC", "Curve25519").
     *                   - `Curve`: The curve of the key (e.g., "ED25519", "SECP256K1", "X25519").
     *                   - `RawKey`: The raw key data (optional).
     *                   - `Seed`: The seed used to derive the key (optional).
     *                   - `DerivationPath`: The derivation path used to derive the key (optional, required if seed is provided).
     *                   - `Index`: The index used in the derivation path (optional, required if seed is provided).
     *
     * @return The created private key.
     *
     * @throws IllegalArgumentException If the provided properties are invalid or insufficient to create the key.
     * @throws ApolloError.InvalidKeyType If the provided key type is invalid or not supported.
     * @throws ApolloError.InvalidKeyCurve If the provided key curve is invalid or not supported.
     * @throws ApolloError.InvalidRawData If the provided raw key data is invalid.
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
                        } ?: run {
                            val seed = properties[SeedKey().property] as String?
                            val derivationParam = properties[DerivationPathKey().property] as String?
                            val index = properties[IndexKey().property] as Int?

                            if (seed != null) {
                                val derivationPath = if (derivationParam != null) {
                                    DerivationPath.fromPath(derivationParam)
                                } else if (index is Int) {
                                    PrismDerivationPath(
                                        keyPurpose = KeyUsage.MASTER_KEY,
                                        keyIndex = index
                                    ).toString().let(DerivationPath::fromPath)
                                } else {
                                    throw IllegalArgumentException("When creating a key from `seed`, `DerivationPath` or `Index` must also be sent")
                                }
                                val seedBytes = seed.base64UrlDecodedBytes
                                val hdKey = EdHDKey.initFromSeed(seedBytes).derive(derivationPath.toString())
                                val key = Ed25519PrivateKey(hdKey.privateKey)
                                return key
                            } else {
                                if (derivationParam.isNullOrBlank() && index == null) {
                                    throw IllegalArgumentException("When creating a key using `DerivationPath` or `Index`, `Seed` must also be sent")
                                }
                                val keyPair = Ed25519KeyPair.generateKeyPair()
                                return keyPair.privateKey
                            }
                        }
                    }

                    Curve.SECP256K1.value -> {
                        keyData?.let {
                            if (it !is ByteArray) {
                                throw Exception("KeyData must be a ByteArray")
                            }
                            return Secp256k1PrivateKey(it)
                        } ?: run {
                            val seed = properties[SeedKey().property] as String?
                            val derivationParam = properties[DerivationPathKey().property] as String?
                            val index = properties[IndexKey().property] as Int?

                            if (seed != null) {
                                val derivationPath = if (derivationParam != null) {
                                    DerivationPath.fromPath(derivationParam)
                                } else if (index is Int) {
                                    PrismDerivationPath(
                                        keyPurpose = KeyUsage.MASTER_KEY,
                                        keyIndex = index
                                    ).toString().let(DerivationPath::fromPath)
                                } else {
                                    throw IllegalArgumentException("When creating a key from `seed`, `DerivationPath` or `Index` must also be sent")
                                }
                                val seedBytes = seed.base64UrlDecodedBytes
                                val hdKey = HDKey(seedBytes, 0, 0).derive(derivationPath.toString())
                                val key = Secp256k1PrivateKey(hdKey.getKMMSecp256k1PrivateKey().raw)
                                return key
                            } else {
                                if (derivationParam.isNullOrBlank() && index == null) {
                                    throw IllegalArgumentException("When creating a key using `DerivationPath` or `Index`, `Seed` must also be sent")
                                }
                                val keyPair = Secp256k1KeyPair.generateKeyPair()
                                return keyPair.privateKey
                            }
                        }
                    }
                }
            }

            KeyTypes.Curve25519 -> {
                when (curve) {
                    Curve.X25519.value -> {
                        keyData?.let {
                            if (it !is ByteArray) {
                                throw ApolloError.InvalidRawData("KeyData must be a ByteArray")
                            }
                            return X25519PrivateKey(it)
                        } ?: run {
                            val seed = properties[SeedKey().property] as String?
                            val derivationParam = properties[DerivationPathKey().property] as String?
                            val index = properties[IndexKey().property] as Int?

                            if (seed != null) {
                                val derivationPath = if (derivationParam != null) {
                                    DerivationPath.fromPath(derivationParam)
                                } else if (index is Int) {
                                    PrismDerivationPath(
                                        keyPurpose = KeyUsage.KEY_AGREEMENT_KEY,
                                        keyIndex = index
                                    ).toString().let(DerivationPath::fromPath)
                                } else {
                                    throw IllegalArgumentException("When creating a key from `seed`, `DerivationPath` or `Index` must also be sent")
                                }
                                val seedBytes = seed.base64UrlDecodedBytes
                                val hdKey = EdHDKey.initFromSeed(seedBytes).derive(derivationPath.toString())
                                val edkey = KMMEdPrivateKey(hdKey.privateKey)
                                val xKey = edkey.x25519PrivateKey()
                                return X25519PrivateKey(xKey.raw)
                            } else {
                                if (derivationParam.isNullOrBlank() && index == null) {
                                    throw IllegalArgumentException("When creating a key using `DerivationPath` or `Index`, `Seed` must also be sent")
                                }
                                val keyPair = X25519KeyPair.generateKeyPair()
                                return keyPair.privateKey
                            }
                        }
                    }
                }
            }
        }
        throw ApolloError.InvalidKeyType(TypeKey().property)
    }

    /**
     * Creates a private key based on the provided properties.
     *
     * @param properties A map of properties used to create the private key. The map should contain the following keys:
     *     - "type" (String): The type of the key ("EC" or "Curve25519").
     *     - "curve" (String): The curve of the key.
     *     - "curvePointX" (String): The x point of the compressed key.
     *     - "curvePointY" (String): The y point of the compressed key.
     *     - "rawKey" (ByteArray): The raw key data (optional).
     *     - "index" (Int): The index for the key (only applicable for EC keys with curve "secp256k1").
     *     - "derivationPath" (String): The derivation path for the key (only applicable for EC keys with curve "secp256k1").
     *
     * @return The created private key.
     *
     * @throws ApolloError.InvalidKeyType If the provided key type is invalid.
     * @throws ApolloError.InvalidKeyCurve If the provided key curve is invalid.
     * @throws ApolloError.InvalidRawData If the provided raw key data is invalid.
     * @throws ApolloError.InvalidIndex If the provided index is invalid.
     * @throws ApolloError.InvalidDerivationPath If the provided derivation path is invalid.
     */
    override fun createPublicKey(properties: Map<String, Any>): PublicKey {
        if (!properties.containsKey(TypeKey().property)) {
            throw ApolloError.InvalidKeyType(TypeKey().property)
        }
        if (!properties.containsKey(CurveKey().property)) {
            throw ApolloError.InvalidKeyCurve(CurveKey().property)
        }

        val keyType = properties[TypeKey().property]
        val curve = properties[CurveKey().property]

        val keyData = properties[RawKey().property]
        val curvePointX = properties[CurvePointXKey().property]
        val curvePointY = properties[CurvePointYKey().property]

        when (keyType) {
            KeyTypes.EC -> {
                when (curve) {
                    Curve.ED25519.value -> {
                        keyData?.let {
                            if (it !is ByteArray) {
                                throw ApolloError.InvalidRawData("KeyData must be a ByteArray")
                            }
                            return Ed25519PublicKey(it)
                        }
                    }

                    Curve.SECP256K1.value -> {
                        if (curvePointX != null && curvePointY != null) {
                            // Compressed key
                            val nativePublicKey = KMMECSecp256k1PublicKey.secp256k1FromByteCoordinates(
                                x = (curvePointX as String).base64UrlDecodedBytes,
                                y = (curvePointY as String).base64UrlDecodedBytes
                            )
                            return Secp256k1PublicKey(nativePublicKey.raw)
                        } else {
                            keyData?.let { data ->
                                if (data !is ByteArray) {
                                    throw Exception("KeyData must be a ByteArray")
                                }
                                return Secp256k1PublicKey(data)
                            }
                        }
                    }
                }
            }

            KeyTypes.Curve25519 -> {
                keyData?.let {
                    if (it !is ByteArray) {
                        throw ApolloError.InvalidRawData("KeyData must be a ByteArray")
                    }
                    return X25519PublicKey(it)
                }
            }
        }
        throw ApolloError.InvalidKeyType(keyType.toString())
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

    /**
     * Restores a key from a JWK (JSON Web Key).
     *
     * @param key The JWK to restore the key from.
     * @param index The index of the key to restore, if it is a key with multiple sub-keys. Default is null.
     * @return The restored Key object.
     */
    override fun restoreKey(key: JWK, index: Int?): Key {
        return when (key.kty) {
            "EC" -> {
                when (key.crv?.lowercase()) {
                    "secp256k1" -> {
                        if (key.d != null) {
                            Secp256k1PrivateKey(key.d.base64UrlDecodedBytes)
                        } else if (key.x != null && key.y != null) {
                            Secp256k1PublicKey(key.x.base64UrlDecodedBytes + key.y.base64UrlDecodedBytes)
                        } else {
                            throw ApolloError.InvalidJWKError()
                        }
                    }

                    else -> {
                        throw ApolloError.InvalidKeyCurve(key.crv ?: "")
                    }
                }
            }

            "OKP" -> {
                when (key.crv?.lowercase()) {
                    "ed25519" -> {
                        if (key.d != null) {
                            Ed25519PrivateKey(key.d.base64UrlDecodedBytes)
                        } else if (key.x != null) {
                            Ed25519PublicKey(key.x.base64UrlDecodedBytes)
                        } else {
                            throw ApolloError.InvalidJWKError()
                        }
                    }

                    "x25519" -> {
                        if (key.d != null) {
                            X25519PrivateKey(key.d.base64UrlDecodedBytes)
                        } else if (key.x != null) {
                            X25519PublicKey(key.x.base64UrlDecodedBytes)
                        } else {
                            throw ApolloError.InvalidJWKError()
                        }
                    }

                    else -> {
                        throw ApolloError.InvalidKeyCurve(key.crv ?: "")
                    }
                }
            }

            else -> {
                throw ApolloError.InvalidKeyType(key.kty)
            }
        }
    }

    /**
     * Restores a private key from StorablePrivateKey.
     *
     * @param restorationIdentifier The restoration identifier to know which type of key it is.
     * @param privateKeyData The private key data encoded in bas64 to restore a private key.
     * @return The restored Key object.
     */
    override fun restorePrivateKey(restorationIdentifier: String, privateKeyData: String): PrivateKey {
        return when (restorationIdentifier) {
            "secp256k1+priv" -> {
                Secp256k1PrivateKey(privateKeyData.base64UrlDecodedBytes)
            }

            "ed25519+priv" -> {
                Ed25519PrivateKey(privateKeyData.base64UrlDecodedBytes)
            }

            "x25519+priv" -> {
                X25519PrivateKey(privateKeyData.base64UrlDecodedBytes)
            }

            else -> {
                throw PlutoError.InvalidRestorationIdentifier()
            }
        }
    }
}
