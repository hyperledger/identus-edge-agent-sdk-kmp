package io.iohk.atala.prism.walletsdk.domain.models.keyManagement

import io.iohk.atala.prism.apollo.base64.base64UrlDecoded
import io.iohk.atala.prism.apollo.base64.base64UrlDecodedBytes
import io.iohk.atala.prism.apollo.base64.base64UrlEncoded
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

data class PEMKey(val keyType: String, val keyData: ByteArray) {
    constructor(keyType: String, keyData: String) : this(keyType, keyData.base64UrlDecodedBytes)

    fun pemEncoded(): String {
        val base64Data = keyData.base64UrlEncoded
        val beginMarker = "-----BEGIN $keyType-----"
        val endMarker = "-----END $keyType-----"

        return "$beginMarker\n$base64Data$endMarker"
    }

    companion object {
        fun fromPemEncoded(pemString: String): PEMKey? {
            val lines = pemString.split("\n")
            if (lines.size < 3) {
                return null
            }

            val beginMarker = lines[0]
            val endMarker = lines[lines.size - 1]

            if (!beginMarker.startsWith("-----BEGIN ") || !beginMarker.endsWith("-----") ||
                !endMarker.startsWith("-----END ") || !endMarker.endsWith("-----")
            ) {
                return null
            }

            val keyType = beginMarker.substring(11, beginMarker.length - 5)
            val base64Data = lines.subList(1, lines.size - 1).joinToString("")
            val keyData = base64Data.base64UrlDecoded

            return PEMKey(keyType = keyType, keyData = keyData)
        }
    }
}
