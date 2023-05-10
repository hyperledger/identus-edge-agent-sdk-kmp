package io.iohk.atala.prism.walletsdk.prismagent.protocols.outOfBand

import io.iohk.atala.prism.apollo.uuid.UUID
import io.iohk.atala.prism.walletsdk.prismagent.protocols.ProtocolType
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
class OutOfBandInvitation
@OptIn(ExperimentalSerializationApi::class)
@JvmOverloads
constructor(
    val id: String = UUID.randomUUID4().toString(),
    val body: Body,
    val from: String,
    @EncodeDefault val type: ProtocolType = ProtocolType.Didcomminvitation
) : InvitationType() {

    @Serializable
    data class Body(
        @SerialName("goal_code") val goalCode: String?,
        val goal: String?,
        val accept: List<String>?
    )
}
