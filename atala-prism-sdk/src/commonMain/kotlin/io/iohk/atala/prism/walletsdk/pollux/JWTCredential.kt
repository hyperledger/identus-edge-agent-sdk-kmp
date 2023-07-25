package io.iohk.atala.prism.walletsdk.pollux

import io.iohk.atala.prism.walletsdk.domain.models.Claim
import io.iohk.atala.prism.walletsdk.domain.models.ClaimType
import io.iohk.atala.prism.walletsdk.domain.models.Credential
import io.iohk.atala.prism.walletsdk.domain.models.JWTPayload
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.util.Base64

data class JWTCredential(val data: String) : Credential {
    private var jwtString: String = data
    var jwtPayload: JWTPayload

    init {
        val jwtParts = jwtString.split(".")
        require(jwtParts.size == 3) { "Invalid JWT string" }
        val credentialString = jwtParts[1]
        val base64Data = Base64.getUrlDecoder().decode(credentialString)
        val jsonString = base64Data.toString(Charsets.UTF_8)
        val dataValue = jsonString.toByteArray(Charsets.UTF_8)

        val json = Json { ignoreUnknownKeys = true }
        this.jwtPayload = json.decodeFromString(dataValue.decodeToString())
    }

    override val id: String
        get() = jwtString

    override val issuer: String
        get() = jwtPayload.iss.toString()

    override val subject: String?
        get() = jwtPayload.sub

    override val claims: Array<Claim>
        get() = jwtPayload.verifiableCredential.credentialSubject.map {
            Claim(key = it.key, value = ClaimType.StringValue(it.value))
        }.toTypedArray()

    override val properties: Map<String, Any?>
        get() {
            val properties = mutableMapOf<String, Any?>()
            properties["nbf"] = jwtPayload.nbf
            properties["jti"] = jwtPayload.jti
            properties["type"] = jwtPayload.verifiableCredential.type
            properties["aud"] = jwtPayload.aud
            properties["id"] = jwtString

            jwtPayload.exp?.let { properties["exp"] = it }
            jwtPayload.verifiableCredential.credentialSchema?.let { properties["schema"] = it.id }
            jwtPayload.verifiableCredential.credentialStatus?.let {
                properties["credentialStatus"] = it.type
            }
            jwtPayload.verifiableCredential.refreshService?.let {
                properties["refreshService"] = it.type
            }
            jwtPayload.verifiableCredential.evidence?.let { properties["evidence"] = it.type }
            jwtPayload.verifiableCredential.termsOfUse?.let { properties["termsOfUse"] = it.type }

            return properties.toMap()
        }
}
