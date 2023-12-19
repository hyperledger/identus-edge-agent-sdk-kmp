package io.iohk.atala.prism.walletsdk.apollo.utils

import io.iohk.atala.prism.apollo.base64.base64UrlEncoded
import io.iohk.atala.prism.apollo.derivation.DerivationPath
import io.iohk.atala.prism.apollo.derivation.HDKey
import io.iohk.atala.prism.apollo.utils.KMMECSecp256k1PrivateKey
import io.iohk.atala.prism.walletsdk.apollo.helpers.BytesOps
import io.iohk.atala.prism.walletsdk.domain.models.Curve
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.CurveKey
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.CurvePointXKey
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.CurvePointYKey
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.DerivableKey
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.ExportableKey
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.JWK
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.KeyTypes
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.PEMKey
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.PEMKeyType
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.PrivateKey
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.PublicKey
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.SeedKey
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.SignableKey
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.StorableKey

/**
 * The `Secp256k1PrivateKey` class represents a private key that uses the secp256k1 elliptic curve.
 * It extends the `PrivateKey` class and implements the `SignableKey`, `StorableKey`, `ExportableKey`, and `DerivableKey` interfaces.
 *
 * @param nativeValue The raw byte array value of the private key.
 * @property type The type of the key, which is set to `KeyTypes.EC`.
 * @property keySpecification A mutable map that contains additional key specifications.
 * @property size The size of the private key in bytes.
 * @property raw The raw byte array value of the private key.
 *
 * @constructor Creates a `Secp256k1PrivateKey` object with the specified `nativeValue`.
 *
 * @param nativeValue The raw byte array value of the private key.
 *
 *
 * @see PrivateKey
 * @see SignableKey
 * @see StorableKey
 * @see ExportableKey
 * @see DerivableKey
 */
class Secp256k1PrivateKey(nativeValue: ByteArray) :
    PrivateKey(),
    SignableKey,
    StorableKey,
    ExportableKey,
    DerivableKey {
    override val type: KeyTypes = KeyTypes.EC
    override val keySpecification: MutableMap<String, String> = mutableMapOf()
    override val size: Int
    override val raw: ByteArray = nativeValue

    init {
        size = raw.size
        keySpecification[CurveKey().property] = Curve.SECP256K1.value
    }

    /**
     * Returns the public key corresponding to this private key.
     * @return the public key as a PublicKey object
     */
    override fun publicKey(): PublicKey {
        return Secp256k1PublicKey(KMMECSecp256k1PrivateKey.secp256k1FromByteArray(raw).getPublicKey().raw)
    }

    /**
     * Signs a byte array message using the private key.
     *
     * @param message The message to be signed.
     * @return The signature as a byte array.
     */
    override fun sign(message: ByteArray): ByteArray {
        val kmmPrivateKey = KMMECSecp256k1PrivateKey.secp256k1FromByteArray(raw)
        return kmmPrivateKey.sign(data = message)
    }

    /**
     * Returns the PEM (Privacy-Enhanced Mail) representation of the private key.
     * The key is encoded in base64 and wrapped with "BEGIN" and "END" markers.
     *
     * @return the PEM representation of the private key as a String
     */
    override fun getPem(): String {
        return PEMKey(
            keyType = PEMKeyType.EC_PRIVATE_KEY,
            keyData = raw
        ).pemEncoded()
    }

    /**
     * Retrieves the JWK (JSON Web Key) representation of the private key.
     *
     * @return The JWK instance representing the private key.
     */
    override fun getJwk(): JWK {
        return JWK(
            kty = "OKP",
            crv = getProperty(CurveKey().property),
            x = getProperty(CurvePointXKey().property).base64UrlEncoded,
            y = getProperty(CurvePointYKey().property).base64UrlEncoded
        )
    }

    /**
     * Retrieves the JWK (JSON Web Key) representation of the private key with the specified key identifier (kid).
     *
     * @param kid The key identifier to be associated with the JWK.
     * @return The JWK object representing the private key.
     */
    override fun jwkWithKid(kid: String): JWK {
        return JWK(
            kty = "OKP",
            kid = kid,
            crv = getProperty(CurveKey().property),
            x = getProperty(CurvePointXKey().property).base64UrlEncoded,
            y = getProperty(CurvePointYKey().property).base64UrlEncoded
        )
    }

    /**
     * Represents the storable data of a key.
     *
     * @property storableData The byte array representing the storable data.
     * @see StorableKey
     */
    override val storableData: ByteArray
        get() = raw

    /**
     * This variable represents the restoration identifier for a key.
     * It is a unique identifier used for restoring the key from storage.
     *
     * @property restorationIdentifier The restoration identifier for the key.
     * @see StorableKey
     */
    override val restorationIdentifier: String
        get() = "secp256k1+priv"

    /**
     * Derives a private key using the given derivation path.
     *
     * @param derivationPath the derivation path used to derive the key
     * @return the derived private key
     * @throws Exception if the key specification does not contain the required properties
     */
    override fun derive(derivationPath: DerivationPath): PrivateKey {
        val seed = getProperty(SeedKey().property)

        val seedByteArray = BytesOps.hexToBytes(seed)

        val hdKey = HDKey(seedByteArray, 0, 0)
        val derivedHdKey = hdKey.derive(derivationPath.toString())
        return Secp256k1PrivateKey(derivedHdKey.getKMMSecp256k1PrivateKey().raw)
    }
}
