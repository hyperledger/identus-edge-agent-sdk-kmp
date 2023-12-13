package io.iohk.atala.prism.walletsdk.apollo.utils

import io.iohk.atala.prism.apollo.base64.base64UrlEncoded
import io.iohk.atala.prism.apollo.utils.KMMECSecp256k1PublicKey
import io.iohk.atala.prism.walletsdk.apollo.config.ECConfig
import io.iohk.atala.prism.walletsdk.domain.models.Curve
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.CurveKey
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.CurvePointXKey
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.CurvePointYKey
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.CustomKey
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.ExportableKey
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.JWK
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.KeyTypes
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.PEMKey
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.PEMKeyType
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.PublicKey
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.StorableKey
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.VerifiableKey

/**
 * Represents a public key in the Secp256k1 elliptic curve algorithm.
 *
 * @param nativeValue The raw byte array representing the public key.
 */
class Secp256k1PublicKey(nativeValue: ByteArray) : PublicKey(), VerifiableKey, StorableKey, ExportableKey {
    override val type: KeyTypes = KeyTypes.EC
    override val keySpecification: MutableMap<String, String> = mutableMapOf()
    override val size: Int
    override val raw: ByteArray = nativeValue

    init {
        size = raw.size
        if (size == ECConfig.PUBLIC_KEY_COMPRESSED_BYTE_SIZE) {
            keySpecification[CustomKey("compressed").property] = "true"
        } else {
            keySpecification[CustomKey("compressed").property] = "false"
        }
        keySpecification[CurveKey().property] = Curve.SECP256K1.value
    }

    /**
     * Verifies the authenticity of a signature using a given message and signature.
     *
     * @param message The message to verify.
     * @param signature The signature data to verify.
     * @return A boolean value indicating whether the signature is valid or not.
     */
    override fun verify(message: ByteArray, signature: ByteArray): Boolean {
        val kmmPublicKey = KMMECSecp256k1PublicKey.secp256k1FromBytes(raw)
        return kmmPublicKey.verify(
            signature = signature,
            data = message
        )
    }

    /**
     * Returns the PEM (Privacy-Enhanced Mail) representation of the public key.
     * The key is encoded in base64 and wrapped with "BEGIN" and "END" markers.
     *
     * @return the PEM representation of the private key as a String
     */
    override fun getPem(): String {
        return PEMKey(
            keyType = PEMKeyType.EC_PUBLIC_KEY,
            keyData = raw
        ).pemEncoded()
    }

    /**
     * Retrieves the JWK (JSON Web Key) representation of the public key.
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
        get() = "secp256k1+pub"

    /**
     * Retrieves the encoded and compressed representation of the public key.
     *
     * @return The encoded and compressed public key as a ByteArray.
     */
    fun getEncodedCompressed(): ByteArray {
        return KMMECSecp256k1PublicKey(raw).getCompressed()
    }
}
