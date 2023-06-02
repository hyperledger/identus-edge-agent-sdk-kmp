package io.iohk.atala.prism.walletsdk.prismagent.protocols.prismOnboarding

import io.iohk.atala.prism.walletsdk.prismagent.PrismAgentError
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class PrismOnboardingInvitation
@Throws(PrismAgentError.InvitationIsInvalidError::class)
constructor(jsonString: String) {
    var body: Body

    init {
        val json = Json {
            ignoreUnknownKeys = true
            prettyPrint = true
            isLenient = true
        }
        body = try {
            json.decodeFromString(jsonString)
        } catch (e: Throwable) {
            throw PrismAgentError.InvitationIsInvalidError()
        }
    }

    @Serializable
    data class Body(
        val type: String,
        val onboardEndpoint: String,
        val from: String
    )
}
