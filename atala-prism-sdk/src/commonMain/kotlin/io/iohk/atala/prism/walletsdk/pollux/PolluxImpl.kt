package io.iohk.atala.prism.walletsdk.pollux

import io.iohk.atala.prism.apollo.base64.base64UrlDecoded
import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Castor
import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Pollux
import io.iohk.atala.prism.walletsdk.domain.models.CredentialType
import io.iohk.atala.prism.walletsdk.domain.models.JsonString
import io.iohk.atala.prism.walletsdk.domain.models.PolluxError
import io.iohk.atala.prism.walletsdk.domain.models.VerifiableCredential
import io.iohk.atala.prism.walletsdk.domain.models.W3CVerifiableCredential
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlin.jvm.Throws

class PolluxImpl(override val castor: Castor) : Pollux {

    @Throws(PolluxError.InvalidJWTString::class, PolluxError.InvalidCredentialError::class)
    override fun parseVerifiableCredential(jwtString: String): VerifiableCredential {
        val jwtParts = jwtString.split(".")
        if (jwtParts.size != 3) {
            throw PolluxError.InvalidJWTString()
        }
        val decodedBase64CredentialJson: JsonString = try {
            jwtParts.first().base64UrlDecoded
        } catch (e: Throwable) {
            throw PolluxError.InvalidCredentialError(e.message)
        }

        val verifiableCredential = Json.decodeFromString<VerifiableCredential>(decodedBase64CredentialJson)

        return when (verifiableCredential.type.first()) {
            CredentialType.JWT.type -> {
                JWTCredential(
                    id = jwtString,
                    json = decodedBase64CredentialJson,
                ).makeVerifiableCredential()
            }
            CredentialType.W3C.type -> {
                Json.decodeFromString<W3CVerifiableCredential>(decodedBase64CredentialJson)
            }
            else -> throw PolluxError.InvalidCredentialError()
        }
    }
}
