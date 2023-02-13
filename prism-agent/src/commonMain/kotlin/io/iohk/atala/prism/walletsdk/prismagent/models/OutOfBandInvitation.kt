package io.iohk.atala.prism.walletsdk.prismagent.models

import io.iohk.atala.prism.apollo.uuid.UUID
import io.iohk.atala.prism.walletsdk.domain.models.DID
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class OutOfBandInvitation(
    val id: String = UUID.randomUUID4().toString(),
    val body: Body,
    @SerialName("from")
    private val fromString: String,
    @Transient
    var from: DID = DID(fromString),
    val type: String
) : InvitationType() {

    @Serializable
    data class Body(
        @SerialName("goal_code")
        val goalCode: String? = null,
        val goal: String? = null,
        val accept: Array<String>? = null
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as Body

            if (goalCode != other.goalCode) return false
            if (goal != other.goal) return false
            if (accept != null) {
                if (other.accept == null) return false
                if (!accept.contentEquals(other.accept)) return false
            } else if (other.accept != null) return false

            return true
        }

        override fun hashCode(): Int {
            var result = goalCode?.hashCode() ?: 0
            result = 31 * result + (goal?.hashCode() ?: 0)
            result = 31 * result + (accept?.contentHashCode() ?: 0)
            return result
        }
    }
}
