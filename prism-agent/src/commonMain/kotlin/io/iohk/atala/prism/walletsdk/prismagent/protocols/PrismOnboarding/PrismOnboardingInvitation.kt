package io.iohk.atala.prism.walletsdk.prismagent.protocols.PrismOnboarding

import io.iohk.atala.prism.domain.models.PrismAgentError
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class PrismOnboardingInvitation(jsonString: String) {

    data class Body(
        val type: String,
        val onboardEndpoint: String,
        val from: String
    )

    var body: Body

    init {
        body = try {
            Json.decodeFromString<Body>(jsonString)
        } catch (e: Throwable) {
            throw PrismAgentError.invitationIsInvalidError()
        }
    }
}
