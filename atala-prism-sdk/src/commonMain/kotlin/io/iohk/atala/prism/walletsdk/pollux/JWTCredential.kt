package io.iohk.atala.prism.walletsdk.pollux

import io.iohk.atala.prism.walletsdk.domain.models.JWTCredentialPayload
import io.iohk.atala.prism.walletsdk.domain.models.JsonString
import io.iohk.atala.prism.walletsdk.domain.models.VerifiableCredential
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@Serializable
data class JWTCredential(val id: String, val json: JsonString) {

    @SerialName("vc")
    private val jwtVerifiableCredential: JWTCredentialPayload = Json.decodeFromString(json)

    fun makeVerifiableCredential(): VerifiableCredential {
        return JWTCredentialPayload(
            iss = jwtVerifiableCredential.iss,
            sub = jwtVerifiableCredential.sub,
            verifiableCredential = jwtVerifiableCredential.verifiableCredential,
            nbf = jwtVerifiableCredential.nbf,
            exp = jwtVerifiableCredential.exp,
            jti = id
        )
    }
}
