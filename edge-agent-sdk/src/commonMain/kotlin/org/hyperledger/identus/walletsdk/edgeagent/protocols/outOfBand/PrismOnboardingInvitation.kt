package org.hyperledger.identus.walletsdk.edgeagent.protocols.outOfBand

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import org.hyperledger.identus.walletsdk.domain.models.DID
import org.hyperledger.identus.walletsdk.edgeagent.FROM

/**
 * Represents an onboarding invitation for PRISM.
 *
 * @param onboardEndpoint The endpoint for onboarding.
 * @property fromString The string representation of the "from" field.
 * @property from The DID object representing the "from" field.
 * @property type The type of the invitation.
 */
@Serializable
data class PrismOnboardingInvitation @JvmOverloads constructor(
    val onboardEndpoint: String,
    @SerialName(FROM)
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
        /**
         * Parses a JSON string into a PrismOnboardingInvitation object.
         *
         * @param string The JSON string to parse.
         * @return A PrismOnboardingInvitation object parsed from the JSON string.
         */
        @JvmStatic
        fun fromJsonString(string: String): PrismOnboardingInvitation {
            return Json.decodeFromString(string)
        }
    }
}
