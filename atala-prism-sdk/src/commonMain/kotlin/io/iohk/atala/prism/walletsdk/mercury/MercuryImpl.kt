package io.iohk.atala.prism.walletsdk.mercury

import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Castor
import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Mercury
import io.iohk.atala.prism.walletsdk.domain.models.Api
import io.iohk.atala.prism.walletsdk.domain.models.DID
import io.iohk.atala.prism.walletsdk.domain.models.DIDDocument
import io.iohk.atala.prism.walletsdk.domain.models.MercuryError
import io.iohk.atala.prism.walletsdk.domain.models.Message
import io.iohk.atala.prism.walletsdk.mercury.forward.ForwardMessage
import io.iohk.atala.prism.walletsdk.prismagent.shared.KeyValue
import org.didcommx.didcomm.utils.isDID
import kotlin.jvm.Throws

interface DIDCommProtocol {
    fun packEncrypted(message: Message): String

    fun unpack(message: String): Message
}

class MercuryImpl(
    private val castor: Castor,
    private val protocol: DIDCommProtocol,
    private val api: Api
) : Mercury {
    override fun packMessage(message: Message): String {
        if (message.to !is DID) {
            throw MercuryError.NoDIDReceiverSetError()
        }

        if (message.from !is DID) {
            throw MercuryError.NoDIDSenderSetError()
        }

        return protocol.packEncrypted(message)
    }

    override fun unpackMessage(message: String): Message {
        return protocol.unpack(message)
    }

    @Throws(MercuryError.NoDIDReceiverSetError::class, MercuryError.NoDIDSenderSetError::class)
    override suspend fun sendMessage(message: Message): ByteArray? {
        if (message.to !is DID) {
            throw MercuryError.NoDIDReceiverSetError()
        }

        if (message.from !is DID) {
            throw MercuryError.NoDIDSenderSetError()
        }

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

    override suspend fun sendMessageParseResponse(message: Message): Message? {
        val msg = sendMessage(message)
        msg?.let {
            val msgString = String(msg)
            if (msgString != "null" && msgString != "") {
                return unpackMessage(msgString)
            }
        }
        return null
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
    private suspend fun makeRequest(service: DIDDocument.Service?, message: String): ByteArray? {
        if (service !is DIDDocument.Service) {
            throw MercuryError.NoValidServiceFoundError()
        }

        val result = api.request(
            "POST",
            service.serviceEndpoint.uri,
            emptyArray(),
            arrayOf(KeyValue("Content-type", "application/didcomm-encrypted+json")),
            message
        )
        return result.jsonString.toByteArray()
    }

    @Throws(MercuryError.NoValidServiceFoundError::class)
    private suspend fun makeRequest(uri: String?, message: String): ByteArray? {
        if (uri !is String) {
            throw MercuryError.NoValidServiceFoundError()
        }

        val result = api.request("POST", uri, emptyArray(), emptyArray(), message)
        return result.jsonString.toByteArray()
    }

    private fun getMediatorDID(service: DIDDocument.Service?): DID? {
        // TODO: Handle when service endpoint uri is HTTP or HTTPS
        return service?.serviceEndpoint?.uri?.let { uri ->
            if (isDID(uri)) {
                castor.parseDID(uri)
            } else {
                null
            }
        }
    }
}
