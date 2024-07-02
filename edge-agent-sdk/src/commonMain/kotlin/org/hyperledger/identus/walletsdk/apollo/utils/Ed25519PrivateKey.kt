package org.hyperledger.identus.walletsdk.apollo.utils

import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.hyperledger.identus.apollo.base64.base64UrlEncoded
import org.hyperledger.identus.apollo.utils.KMMEdPrivateKey
import org.hyperledger.identus.walletsdk.domain.models.Curve
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.CurveKey
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.ExportableKey
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.JWK
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.KeyTypes
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.PEMKey
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.PEMKeyType
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.PrivateKey
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.PublicKey
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.SignableKey
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.StorableKey
import java.security.KeyFactory
import java.security.spec.PKCS8EncodedKeySpec

/**
 * Represents a private key for the Ed25519 algorithm.
 *
 * This class extends the abstract class PrivateKey and implements the interfaces SignableKey, StorableKey, and ExportableKey.
 *
 * @param nativeValue The raw byte array representing the private key.
 * @property size The size of the private key in bytes.
 * @property raw The raw byte array representing the private key.
 * @property type The type of the key. Always set to KeyTypes.EC.
 * @property keySpecification A mutable map representing the key specification.
 *
 * @constructor Creates an instance of Ed25519PrivateKey and initializes its properties.
 *
 * @param nativeValue The raw byte array representing the private key.
 */
class Ed25519PrivateKey(nativeValue: ByteArray) : PrivateKey(), SignableKey, StorableKey, ExportableKey {

    override val type: KeyTypes = KeyTypes.EC
    override val keySpecification: MutableMap<String, String> = mutableMapOf()
    override val size: Int
    override val raw: ByteArray = nativeValue

    init {
        size = raw.size
        keySpecification[CurveKey().property] = Curve.ED25519.value
    }

    /**
     * Returns the public key corresponding to this private key.
     * @return the public key as a PublicKey object
     */
    override fun publicKey(): PublicKey {
        val public = KMMEdPrivateKey(raw).publicKey()
        return Ed25519PublicKey(public.raw)
    }

    /**
     * Signs a byte array message using the private key.
     *
     * @param message The message to be signed.
     * @return The signature as a byte array.
     */
    override fun sign(message: ByteArray): ByteArray {
        val private = KMMEdPrivateKey(raw)
        return private.sign(message)
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
            d = raw.base64UrlEncoded,
            x = publicKey().raw.base64UrlEncoded
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
            d = raw.base64UrlEncoded,
            x = publicKey().raw.base64UrlEncoded
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
        get() = "ed25519+priv"

    override fun jca(): java.security.PrivateKey {
        val privateKeyParams = Ed25519PrivateKeyParameters(raw, 0)
        val pkcs8Encoded = privateKeyParams.encoded
        val keyFactory = KeyFactory.getInstance("Ed25519", BouncyCastleProvider())
        return keyFactory.generatePrivate(PKCS8EncodedKeySpec(pkcs8Encoded))
    }
}
