package io.iohk.atala.prism.walletsdk.prismagent.models

import io.iohk.atala.prism.walletsdk.domain.models.DID
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@Serializable
data class PrismOnboardingInvitation(
    val onboardEndpoint: String,
    @SerialName("from")
    private val fromString: String,
    @Transient
    var from: DID? = null,
    val type: String,
) : InvitationType() {

    init {
        from = DID(fromString)
    }

    companion object {
        fun prismOnboardingInvitationFromJsonString(string: String): PrismOnboardingInvitation {
            return Json.decodeFromString(string)
        }
    }
}
