package io.iohk.atala.prism.walletsdk.pollux.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class JWTJsonPayload @JvmOverloads constructor(
    val iss: String? = null,
    val sub: String? = null,
    val nbf: Long? = null,
    val exp: Long? = null,
    val vc: JsonElement
)
