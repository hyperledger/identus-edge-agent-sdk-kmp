package io.iohk.atala.prism.walletsdk.prismagent.protocols.mediation

import io.iohk.atala.prism.apollo.uuid.UUID
import io.iohk.atala.prism.walletsdk.domain.models.Message
import io.iohk.atala.prism.walletsdk.prismagent.protocols.ProtocolType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlin.jvm.Throws

sealed class MediationProtocolError : Throwable() {
    class InvalidMediationGrantError : MediationProtocolError()
}

class MediationGrant {
    var id: String
    var type = ProtocolType.DidcommMediationGrant.value
    var body: Body

    constructor(
        id: String = UUID.randomUUID4().toString(),
        body: Body
    ) {
        this.id = id
        this.body = body
    }

    @Throws(MediationProtocolError.InvalidMediationGrantError::class)
    constructor(fromMessage: Message) {
        if (fromMessage.piuri != ProtocolType.DidcommMediationGrant.value) {
            throw MediationProtocolError.InvalidMediationGrantError()
        }
        this.id = fromMessage.id
        this.body = Json.decodeFromString(fromMessage.body)
    }

    @Serializable
    data class Body(@SerialName("routing_did")var routingDid: String)
}
