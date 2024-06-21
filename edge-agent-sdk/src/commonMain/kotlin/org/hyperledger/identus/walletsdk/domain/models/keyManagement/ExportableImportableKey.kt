@file:Suppress("ktlint:standard:import-ordering")

package org.hyperledger.identus.walletsdk.domain.models.keyManagement

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.hyperledger.identus.apollo.base64.base64PadEncoded
import org.hyperledger.identus.apollo.base64.base64UrlDecodedBytes

/**
 * This interface defines what is required for a key to be exportable
 */
interface ExportableKey {
    /**
     * The key exported in PEM (Privacy-Enhanced Mail) format.
     * @return PEM string
     */
    fun getPem(): String

    /**
     * They key exported as a JWK (JSON Web Key)
     * @return JWD instance
     */
    fun getJwk(): JWK

    /**
     * Returns the key as a JWD with a specific kid (key identifier)
     * @return JWK instnace
     */
    fun jwkWithKid(kid: String): JWK
}

/**
 * This interface defines what is required for a key to be importable
 */
interface ImportableKey {
    /**
     * Initializes key from PEM string
     * @param pem string
     */
    @Throws(Exception::class)
    fun initializeFromPem(pem: String)

    /**
     * Initializes key from JWK
     */
    @Throws(Exception::class)
    fun initializeFromJwk(jwk: JWK)
}

/**
 * Representation of a JWK (JSON Web Key)
 */
@Serializable
data class JWK(
    // Key parameters
    val kty: String,
    val alg: String? = null,
    val kid: String? = null,
    val use: String? = null,

    // RSA key parameters
    val n: String? = null,
    val e: String? = null,
    val d: String? = null,
    val p: String? = null,
    val q: String? = null,
    val dp: String? = null,
    val dq: String? = null,
    val qi: String? = null,

    // ED key parameters
    val crv: String? = null,
    val x: String? = null,
    val y: String? = null,

    // Symmetric key parameters
    val k: String? = null
) {
    /**
     * Converts a JWK to a Nimbus JWK.
     *
     * @return The converted Nimbus JWK.
     */
    fun toNimbusJwk(): com.nimbusds.jose.jwk.JWK {
        return com.nimbusds.jose.jwk.JWK.parse(Json.encodeToString(this))
    }
}

/**
 * Representation of a cryptographic key in PEM format.
 */
data class PEMKey(val keyType: PEMKeyType, val keyData: ByteArray) {
    constructor(keyType: PEMKeyType, keyData: String) : this(keyType, keyData.base64UrlDecodedBytes)

    /**
     * Encodes the PEM into base 64
     * @return pem encoded string
     */
    fun pemEncoded(): String {
        val base64Data = keyData.base64PadEncoded.chunked(64).joinToString("\n")
        val beginMarker = "-----BEGIN $keyType-----"
        val endMarker = "-----END $keyType-----"

        return "$beginMarker\n$base64Data\n$endMarker"
    }

    /**
     * Overrides the `equals` method of the `Any` class.
     *
     * This method checks if the current `PEMKey` object is equal to the specified `other` object.
     * Two `PEMKey` objects are considered equal if they have the same `keyType` and `keyData`.
     *
     * @param other The object to compare for equality.
     * @return `true` if the `other` object is equal to the current `PEMKey` object, `false` otherwise.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PEMKey

        if (keyType != other.keyType) return false
        if (!keyData.contentEquals(other.keyData)) return false

        return true
    }

    /**
     * Calculates the hash code for the PEMKey instance.
     * The hash code is calculated based on the keyType and keyData properties of the PEMKey.
     *
     * @return The hash code value for the PEMKey.
     */
    override fun hashCode(): Int {
        var result = keyType.hashCode()
        result = 31 * result + keyData.contentHashCode()
        return result
    }

    companion object {
        /**
         * Decodes a PEM-encoded string into a PEMKey object.
         *
         * @param pemString The PEM-encoded string to decode.
         * @return A PEMKey object if the decoding was successful, or null otherwise.
         */
        @JvmStatic
        fun fromPemEncoded(pemString: String): PEMKey? {
            val lines = pemString.split("\n")
            if (lines.size < 3) {
                return null
            }

            val beginMarker = lines[0]
            val endMarker = lines[lines.size - 1]

            if (beginMarker.startsWith("-----BEGIN ").not() || beginMarker.endsWith("-----").not() ||
                endMarker.startsWith("-----END ").not() || endMarker.endsWith("-----").not()
            ) {
                return null
            }

            val keyType = PEMKeyType.fromString(beginMarker) ?: throw Exception("Unknown PEM Key type")

            val base64Data = lines.subList(1, lines.size - 1).joinToString("")
            val keyData = base64Data.base64PadEncoded

            return PEMKey(keyType = keyType, keyData = keyData)
        }
    }
}

/**
 * Definition of the PEM key types available
 */
enum class PEMKeyType(val value: Pair<String, String>) {
    EC_PRIVATE_KEY(Pair("-----BEGIN EC PRIVATE KEY-----", "-----END EC PRIVATE KEY-----")),
    EC_PUBLIC_KEY(Pair("-----BEGIN EC PUBLIC KEY-----", "-----END EC PUBLIC KEY-----"));

    companion object {
        /**
         * Converts a string value to the corresponding PEMKeyType enum value.
         *
         * @param value The string value to convert.
         * @return The PEMKeyType enum value if the conversion was successful, or null otherwise.
         */
        @JvmStatic
        fun fromString(value: String): PEMKeyType? {
            return entries.firstOrNull { it.value.first == value || it.value.second == value }
        }
    }
}
