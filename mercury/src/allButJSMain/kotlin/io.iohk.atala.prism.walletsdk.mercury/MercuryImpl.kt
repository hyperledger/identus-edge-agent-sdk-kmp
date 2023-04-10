package io.iohk.atala.prism.walletsdk.mercury

import io.iohk.atala.prism.walletsdk.domain.buildingBlocks.Castor
import io.iohk.atala.prism.walletsdk.domain.buildingBlocks.Mercury
import io.iohk.atala.prism.walletsdk.domain.models.DID
import io.iohk.atala.prism.walletsdk.domain.models.DIDDocument
import io.iohk.atala.prism.walletsdk.domain.models.MercuryError
import io.iohk.atala.prism.walletsdk.domain.models.Message
import io.iohk.atala.prism.walletsdk.mercury.forward.ForwardMessage
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class MercuryImpl constructor(
    val castor: Castor,
    val protocol: DIDCommProtocol,
    val api: Api
) : Mercury {
    @Throws(MercuryError.NoDIDReceiverSetError::class)
    override fun packMessage(message: Message): String {
        if (message.to !is DID) throw MercuryError.NoDIDReceiverSetError()

        if (message.from !is DID) throw MercuryError.NoDIDSenderSetError()

        return protocol.packEncrypted(message)
    }

    override fun unpackMessage(message: String): Message {
        return protocol.unpack(message)
    }

    @Throws(MercuryError.NoDIDReceiverSetError::class)
    override suspend fun sendMessage(message: Message): ByteArray? {
        if (message.to !is DID) throw MercuryError.NoDIDReceiverSetError()

        if (message.from !is DID) throw MercuryError.NoDIDSenderSetError()

        val document = castor.resolveDID(message.to.toString())
        val packedMessage = packMessage(message)
        val service = document.services.find { it.type.contains("DIDCommMessaging") }

        getMediatorDID(service)?.let { mediatorDid ->
            val mediatorDocument = castor.resolveDID(mediatorDid.toString())
            val mediatorUri =
                mediatorDocument.services.find { it.type.contains("DIDCommMessaging") }?.serviceEndpoint?.uri
            val forwardMsg = prepareForwardMessage(message, packedMessage, mediatorDid)
            val packedForwardMsg = packMessage(forwardMsg.makeMessage())

            return makeRequest(mediatorUri, packedForwardMsg)
        }

        return makeRequest(service, packedMessage)
    }

    override suspend fun sendMessageParseMessage(message: Message): Message? {
        val responseBody = sendMessage(message)
        val responseJSON = Json.encodeToString(responseBody)
        return unpackMessage(responseJSON)
    }

    private fun prepareForwardMessage(message: Message, encrypted: String, mediatorDid: DID): ForwardMessage {
        return ForwardMessage(
            body = message.to.toString(),
            encryptedMessage = encrypted,
            from = message.from!!,
            to = mediatorDid,
        )
    }

    @Throws(MercuryError.NoValidServiceFoundError::class)
    private fun makeRequest(service: DIDDocument.Service?, message: String): ByteArray? {
        if (service !is DIDDocument.Service) throw MercuryError.NoValidServiceFoundError()

        return api.request("POST", service.serviceEndpoint.uri, message)
    }

    @Throws(MercuryError.NoValidServiceFoundError::class)
    private fun makeRequest(uri: String?, message: String): ByteArray? {
        if (uri !is String) throw MercuryError.NoValidServiceFoundError()

        return api.request("POST", uri, message)
    }

    private fun getMediatorDID(service: DIDDocument.Service?): DID? {
        return service?.serviceEndpoint?.uri?.let { uri -> castor.parseDID(uri) }
    }
}
