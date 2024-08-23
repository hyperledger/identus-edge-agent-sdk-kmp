package org.hyperledger.identus.walletsdk.edgeagent.protocols.outOfBand

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.hyperledger.identus.walletsdk.domain.models.AttachmentDescriptor
import org.hyperledger.identus.walletsdk.edgeagent.GOAL_CODE
import org.hyperledger.identus.walletsdk.edgeagent.protocols.ProtocolType
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
    val typ: String? = null,
    val attachments: Array<AttachmentDescriptor> = arrayOf(),
    @SerialName("created_time")
    val createdTime: Long = 0,
    @SerialName("expires_time")
    val expiresTime: Long = 0
) : InvitationType() {

    /**
     * Represents the body of the out-of-band invitation message.
     */
    @OptIn(ExperimentalSerializationApi::class)
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
