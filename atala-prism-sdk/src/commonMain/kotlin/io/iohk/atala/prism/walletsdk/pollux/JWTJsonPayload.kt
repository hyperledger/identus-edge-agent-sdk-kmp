package io.iohk.atala.prism.walletsdk.pollux

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class JWTJsonPayload(
    val iss: String,
    val sub: String,
    val nbf: Long,
    val exp: Long,
    val vc: JsonElement
)
