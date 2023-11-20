package io.iohk.atala.prism.walletsdk.domain.models.keyManagement

import io.iohk.atala.prism.apollo.base64.base64PadEncoded
import io.iohk.atala.prism.apollo.base64.base64UrlDecodedBytes
import kotlinx.serialization.Serializable

interface ExportableKey {
    fun getPem(): String
    fun getJwk(): JWK

    fun jwkWithKid(kid: String): JWK
}

interface ImportableKey {
    @Throws(Exception::class)
    fun initializeFromPem(pem: String)

    @Throws(Exception::class)
    fun initializeFromJwk(jwk: JWK)
}

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

data class PEMKey(val keyType: PEMKeyType, val keyData: ByteArray) {
    constructor(keyType: PEMKeyType, keyData: String) : this(keyType, keyData.base64UrlDecodedBytes)

    fun pemEncoded(): String {
        val base64Data = keyData.base64PadEncoded
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

enum class PEMKeyType(val value: Pair<String, String>) {
    EC_PRIVATE_KEY(Pair("-----BEGIN EC PRIVATE KEY-----", "-----END EC PRIVATE KEY-----")),
    EC_PUBLIC_KEY(Pair("-----BEGIN EC PUBLIC KEY-----", "-----END EC PUBLIC KEY-----"));

    companion object {
        fun fromString(value: String): PEMKeyType? {
            return values().firstOrNull { it.value.first == value || it.value.second == value }
        }
    }
}
