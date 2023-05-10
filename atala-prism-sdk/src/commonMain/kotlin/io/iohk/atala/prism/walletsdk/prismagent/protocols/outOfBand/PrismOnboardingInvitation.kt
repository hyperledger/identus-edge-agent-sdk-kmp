package io.iohk.atala.prism.walletsdk.prismagent.protocols.outOfBand

import io.iohk.atala.prism.walletsdk.domain.models.DID
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@Serializable
data class PrismOnboardingInvitation @JvmOverloads constructor(
    val onboardEndpoint: String,
    @SerialName("from")
    private val fromString: String,
    @Transient
    var from: DID? = null,
    val type: String
) : InvitationType() {

    init {
        // TODO: Should we check first if a DID instance was based or not?
        from = DID(fromString)
    }

    companion object {
        fun fromJsonString(string: String): PrismOnboardingInvitation {
            return Json.decodeFromString(string)
        }
    }
}
