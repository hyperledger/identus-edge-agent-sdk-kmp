package io.iohk.atala.prism.walletsdk.pollux

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class JWTJsonPayload(
    val iss: String? = null,
    val sub: String? = null,
    val nbf: Long? = null,
    val exp: Long? = null,
    val vc: JsonElement
)
