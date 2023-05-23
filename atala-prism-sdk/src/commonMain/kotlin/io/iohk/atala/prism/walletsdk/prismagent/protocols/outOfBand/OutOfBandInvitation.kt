package io.iohk.atala.prism.walletsdk.prismagent.protocols.outOfBand

import io.iohk.atala.prism.apollo.uuid.UUID
import io.iohk.atala.prism.walletsdk.prismagent.protocols.ProtocolType
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents an out-of-band invitation message in the DIDComm protocol. The [OutOfBandInvitation] class represents an
 * out-of-band invitation message in the DIDComm protocol.
 */
@Serializable
class OutOfBandInvitation
@OptIn(ExperimentalSerializationApi::class)
@JvmOverloads
constructor(
    val id: String = UUID.randomUUID4().toString(),
    val body: Body,
    val from: String,
    @EncodeDefault
    val type: ProtocolType = ProtocolType.Didcomminvitation
) : InvitationType() {

    /**
     * Represents the body of the out-of-band invitation message.
     */
    @Serializable
    data class Body(
        @SerialName("goal_code")
        val goalCode: String?,
        val goal: String?,
        val accept: List<String>?
    )
}
