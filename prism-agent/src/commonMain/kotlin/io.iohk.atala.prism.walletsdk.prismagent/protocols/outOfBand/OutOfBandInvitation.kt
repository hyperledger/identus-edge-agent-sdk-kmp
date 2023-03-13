package io.iohk.atala.prism.walletsdk.prismagent.protocols.outOfBand

import io.iohk.atala.prism.apollo.uuid.UUID
import io.iohk.atala.prism.walletsdk.prismagent.protocols.ProtocolType
import kotlinx.serialization.Serializable

@Serializable
class OutOfBandInvitation(
    val body: Body,
    val from: String,
    val id: String = UUID.randomUUID4().toString()
) {
    val type = ProtocolType.Didcomminvitation

    @Serializable
    data class Body(
        val goalCode: String?,
        val goal: String?,
        val accept: List<String>?,
    )
}
