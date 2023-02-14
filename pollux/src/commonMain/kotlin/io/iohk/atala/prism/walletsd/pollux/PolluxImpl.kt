package io.iohk.atala.prism.walletsd.pollux

import io.iohk.atala.prism.apollo.base64.base64UrlDecoded
import io.iohk.atala.prism.walletsdk.domain.buildingBlocks.Castor
import io.iohk.atala.prism.walletsdk.domain.buildingBlocks.Pollux
import io.iohk.atala.prism.walletsdk.domain.models.JsonString
import io.iohk.atala.prism.walletsdk.domain.models.PolluxError
import io.iohk.atala.prism.walletsdk.domain.models.VerifiableCredential
import io.iohk.atala.prism.walletsdk.domain.models.W3CVerifiableCredential
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class PolluxImpl(override val castor: Castor) : Pollux {

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

        val jwtCredential = try {
            JWTCredential(
                id = jwtString,
                json = decodedBase64CredentialJson,
            ).makeVerifiableCredential()
        } catch (e: Throwable) {
            null
        }

        if (jwtCredential != null) {
            return jwtCredential
        }

        val w3cCredential = try {
            Json.decodeFromString<W3CVerifiableCredential>(decodedBase64CredentialJson)
        } catch (e: Throwable) {
            null
        }

        return w3cCredential ?: throw PolluxError.InvalidCredentialError()
    }
}
