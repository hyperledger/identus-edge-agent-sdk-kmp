package io.iohk.atala.prism.walletsdk.domain.models.keyManagement

import io.iohk.atala.prism.apollo.base64.base64PadEncoded
import io.iohk.atala.prism.apollo.base64.base64UrlDecodedBytes
import kotlinx.serialization.Serializable

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
    val kty: String,
    val alg: String? = null,
    val kid: String? = null,
    val use: String? = null,
    val n: String? = null,
    val e: String? = null,
    val d: String? = null,
    val p: String? = null,
    val q: String? = null,
    val dp: String? = null,
    val dq: String? = null,
    val qi: String? = null,
    val crv: String? = null,
    val x: String? = null,
    val y: String? = null,
    val k: String? = null
)

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

    companion object {
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
        fun fromString(value: String): PEMKeyType? {
            return values().firstOrNull { it.value.first == value || it.value.second == value }
        }
    }
}
