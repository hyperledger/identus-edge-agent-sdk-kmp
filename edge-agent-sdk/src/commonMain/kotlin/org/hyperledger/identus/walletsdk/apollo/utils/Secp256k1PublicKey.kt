package org.hyperledger.identus.walletsdk.apollo.utils

import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.jce.spec.ECNamedCurveSpec
import org.hyperledger.identus.apollo.base64.base64UrlEncoded
import org.hyperledger.identus.apollo.utils.KMMECSecp256k1PublicKey
import org.hyperledger.identus.apollo.utils.KMMEllipticCurve
import org.hyperledger.identus.walletsdk.apollo.config.ECConfig
import org.hyperledger.identus.walletsdk.domain.models.Curve
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.CurveKey
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.CurvePointXKey
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.CurvePointYKey
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.CustomKey
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.ExportableKey
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.JWK
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.KeyTypes
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.PEMKey
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.PEMKeyType
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.PublicKey
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.StorableKey
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.VerifiableKey
import org.hyperledger.identus.walletsdk.pollux.EC
import java.security.KeyFactory
import java.security.interfaces.ECPublicKey
import java.security.spec.ECParameterSpec
import java.security.spec.ECPublicKeySpec

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
        val point = KMMECSecp256k1PublicKey.secp256k1FromBytes(raw).getCurvePoint()
        keySpecification[CurvePointXKey().property] = point.x.base64UrlEncoded
        keySpecification[CurvePointYKey().property] = point.y.base64UrlEncoded
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
            kty = "EC",
            crv = getProperty(CurveKey().property),
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
            crv = getProperty(CurveKey().property),
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
        get() = "secp256k1+pub"

    /**
     * Retrieves the encoded and compressed representation of the public key.
     *
     * @return The encoded and compressed public key as a ByteArray.
     */
    fun getEncodedCompressed(): ByteArray {
        return KMMECSecp256k1PublicKey(raw).getCompressed()
    }

    override fun jca(): java.security.PublicKey {
        val curveName = KMMEllipticCurve.SECP256k1.value
        val sp = ECNamedCurveTable.getParameterSpec(curveName)
        val params: ECParameterSpec = ECNamedCurveSpec(sp.name, sp.curve, sp.g, sp.n, sp.h)

        val publicKeySpec = ECPublicKeySpec(params.generator, params)
        val keyFactory = KeyFactory.getInstance(EC, BouncyCastleProvider())
        return keyFactory.generatePublic(publicKeySpec) as ECPublicKey
    }
}
