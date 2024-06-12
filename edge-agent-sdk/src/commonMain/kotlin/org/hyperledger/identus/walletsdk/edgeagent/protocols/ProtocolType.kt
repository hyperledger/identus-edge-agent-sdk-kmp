package org.hyperledger.identus.walletsdk.edgeagent.protocols

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.hyperledger.identus.walletsdk.edgeagent.EdgeAgentError
import org.hyperledger.identus.walletsdk.edgeagent.PROTOCOL_TYPE

@Serializable(with = ProtocolTypeSerializer::class)
enum class ProtocolType(val value: String) {
    DidcommMediationRequest("https://didcomm.org/coordinate-mediation/2.0/mediate-request"),
    DidcommMediationGrant("https://didcomm.org/coordinate-mediation/2.0/mediate-grant"),
    DidcommMediationDeny("https://didcomm.org/coordinate-mediation/2.0/mediate-deny"),
    DidcommMediationKeysUpdate("https://didcomm.org/coordinate-mediation/2.0/keylist-update"),
    DidcommPresentation("https://didcomm.atalaprism.io/present-proof/3.0/presentation"),
    DidcommRequestPresentation("https://didcomm.atalaprism.io/present-proof/3.0/request-presentation"),
    DidcommProposePresentation("https://didcomm.atalaprism.io/present-proof/3.0/propose-presentation"),
    DidcommCredentialPreview("https://didcomm.org/issue-credential/3.0/credential-preview"),
    DidcommIssueCredential("https://didcomm.org/issue-credential/3.0/issue-credential"),
    DidcommOfferCredential("https://didcomm.org/issue-credential/3.0/offer-credential"),
    DidcommProposeCredential("https://didcomm.org/issue-credential/3.0/propose-credential"),
    DidcommRequestCredential("https://didcomm.org/issue-credential/3.0/request-credential"),
    DidcommconnectionRequest("https://atalaprism.io/mercury/connections/1.0/request"),
    DidcommconnectionResponse("https://atalaprism.io/mercury/connections/1.0/response"),
    Didcomminvitation("https://didcomm.org/out-of-band/2.0/invitation"),
    PrismOnboarding("https://atalaprism.io/did-request"),
    PickupRequest("https://didcomm.org/messagepickup/3.0/delivery-request"),
    PickupDelivery("https://didcomm.org/messagepickup/3.0/delivery"),
    PickupStatus("https://didcomm.org/messagepickup/3.0/status"),
    PickupReceived("https://didcomm.org/messagepickup/3.0/messages-received"),
    LiveDeliveryChange("https://didcomm.org/messagepickup/3.0/live-delivery-change"),
    PrismRevocation("https://atalaprism.io/revocation_notification/1.0/revoke"),
    ProblemReport("https://didcomm.org/report-problem/2.0/problem-report"),
    BasicMessage("https://didcomm.org/basicmessage/2.0/message"),
    None("");

    companion object {
        @JvmStatic
        fun findProtocolType(type: String, default: ProtocolType): ProtocolType {
            return entries.find { it.value == type } ?: default
        }
    }
}

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = ProtocolType::class)
object ProtocolTypeSerializer : KSerializer<ProtocolType> {
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor(PROTOCOL_TYPE, PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: ProtocolType) {
        encoder.encodeString(value.value)
    }

    override fun deserialize(decoder: Decoder): ProtocolType {
        val protocolType = decoder.decodeString()
        return ProtocolType.findProtocolType(protocolType, ProtocolType.None)
    }
}

/**
 * Finds the ProtocolType based on the given string value.
 *
 * @param string The string value to match with the ProtocolType's value.
 * @return The matched ProtocolType.
 * @throws EdgeAgentError.UnknownInvitationTypeError If the type of the invitation is not supported.
 */
@Throws(EdgeAgentError.UnknownInvitationTypeError::class)
fun findProtocolTypeByValue(string: String): ProtocolType {
    val it = ProtocolType.entries.iterator()
    while (it.hasNext()) {
        val internalType = it.next()
        if (internalType.value == string) {
            return internalType
        }
    }
    throw EdgeAgentError.UnknownInvitationTypeError(string)
}
