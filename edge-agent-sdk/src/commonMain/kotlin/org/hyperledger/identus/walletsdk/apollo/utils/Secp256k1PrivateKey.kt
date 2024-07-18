package org.hyperledger.identus.walletsdk.apollo.utils

import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.jce.spec.ECNamedCurveSpec
import org.hyperledger.identus.apollo.base64.base64UrlDecodedBytes
import org.hyperledger.identus.apollo.base64.base64UrlEncoded
import org.hyperledger.identus.apollo.derivation.DerivationPath
import org.hyperledger.identus.apollo.derivation.HDKey
import org.hyperledger.identus.apollo.utils.KMMECSecp256k1PrivateKey
import org.hyperledger.identus.apollo.utils.KMMEllipticCurve
import org.hyperledger.identus.walletsdk.domain.models.Curve
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.CurveKey
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.CurvePointXKey
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.CurvePointYKey
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.DerivableKey
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.ExportableKey
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.JWK
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.KeyTypes
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.PEMKey
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.PEMKeyType
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.PrivateKey
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.PublicKey
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.SeedKey
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.SignableKey
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.StorableKey
import org.hyperledger.identus.walletsdk.pollux.EC
import java.math.BigInteger
import java.security.KeyFactory
import java.security.interfaces.ECPrivateKey
import java.security.spec.ECParameterSpec
import java.security.spec.ECPrivateKeySpec

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
        val point = KMMECSecp256k1PrivateKey.secp256k1FromByteArray(raw).getPublicKey().getCurvePoint()
        keySpecification[CurvePointXKey().property] = point.x.base64UrlEncoded
        keySpecification[CurvePointYKey().property] = point.y.base64UrlEncoded
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
            kty = "EC",
            crv = getProperty(CurveKey().property),
            d = raw.base64UrlEncoded,
            x = getProperty(CurvePointXKey().property),
            y = getProperty(CurvePointYKey().property)
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
            kty = "EC",
            kid = kid,
            d = raw.base64UrlEncoded,
            x = getProperty(CurvePointXKey().property),
            y = getProperty(CurvePointYKey().property)
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

        val seedByteArray = seed.base64UrlDecodedBytes

        val hdKey = HDKey(seedByteArray, 0, 0)
        val derivedHdKey = hdKey.derive(derivationPath.toString())
        return Secp256k1PrivateKey(derivedHdKey.getKMMSecp256k1PrivateKey().raw)
    }

    override fun jca(): java.security.PrivateKey {
        val curveName = KMMEllipticCurve.SECP256k1.value
        val sp = ECNamedCurveTable.getParameterSpec(curveName)
        val params: ECParameterSpec = ECNamedCurveSpec(sp.name, sp.curve, sp.g, sp.n, sp.h)
        val privateKeySpec = ECPrivateKeySpec(BigInteger(1, getValue()), params)
        val keyFactory = KeyFactory.getInstance(EC, BouncyCastleProvider())
        return keyFactory.generatePrivate(privateKeySpec) as ECPrivateKey
    }
}
