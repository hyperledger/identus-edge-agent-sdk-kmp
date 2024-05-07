package org.hyperledger.identus.walletsdk.pollux.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Represents a JSON Web Token (JWT) payload as a Kotlin data class.
 *
 * @property iss The issuer of the token. It identifies the entity that issued the token.
 * @property sub The subject of the token. It represents the entity that the token pertains to.
 * @property nbf The "not before" time of the token. It specifies the time before which the token should not be considered valid.
 * @property exp The expiration time of the token. It specifies the time after which the token should not be considered valid.
 * @property vc The JSON payload contained within the token.
 */
@Serializable
data class JWTJsonPayload @JvmOverloads constructor(
    val iss: String? = null,
    val sub: String? = null,
    val nbf: Long? = null,
    val exp: Long? = null,
    val vc: JsonElement
)
