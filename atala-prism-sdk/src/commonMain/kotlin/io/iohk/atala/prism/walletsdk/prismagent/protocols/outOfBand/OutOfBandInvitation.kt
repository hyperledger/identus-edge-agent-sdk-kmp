package io.iohk.atala.prism.walletsdk.prismagent.protocols.outOfBand

import io.iohk.atala.prism.walletsdk.prismagent.GOAL_CODE
import io.iohk.atala.prism.walletsdk.prismagent.protocols.ProtocolType
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * Represents an out-of-band invitation message in the DIDComm protocol. The [OutOfBandInvitation] class represents an
 * out-of-band invitation message in the DIDComm protocol.
 */
@Serializable
class OutOfBandInvitation
@OptIn(ExperimentalSerializationApi::class)
@JvmOverloads
constructor(
    val id: String = UUID.randomUUID().toString(),
    val body: Body,
    val from: String,
    @EncodeDefault
    val type: ProtocolType = ProtocolType.Didcomminvitation,
    @EncodeDefault
    val typ: String? = null
) : InvitationType() {

    /**
     * Represents the body of the out-of-band invitation message.
     */
    @Serializable
    data class Body(
        @SerialName(GOAL_CODE)
        @EncodeDefault
        val goalCode: String? = null,
        @EncodeDefault
        val goal: String? = null,
        val accept: List<String>?
    )
}
