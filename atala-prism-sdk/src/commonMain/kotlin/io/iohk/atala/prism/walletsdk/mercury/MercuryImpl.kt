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
import io.ktor.http.HttpHeaders
import org.didcommx.didcomm.common.Typ
import org.didcommx.didcomm.utils.isDID
import kotlin.jvm.Throws

interface DIDCommProtocol {
    fun packEncrypted(message: Message): String

    fun unpack(message: String): Message
}

/**
 * Mercury is a powerful and flexible library for working with decentralized identifiers and secure communications
 * protocols. Whether you are a developer looking to build a secure and private messaging app or a more complex
 * decentralized system requiring trusted peer-to-peer connections, Mercury provides the tools and features you need to
 * establish, manage, and secure your communications easily.
 */
class MercuryImpl(
    private val castor: Castor,
    private val protocol: DIDCommProtocol,
    private val api: Api
) : Mercury {
    /**
     * Asynchronously packs a given message object into a string representation. This function may throw an error if the
     * message object is invalid.
     *
     * @param message The message object to pack
     * @return The string representation of the packed message
     * @throws [MercuryError.NoDIDReceiverSetError] if DIDReceiver is invalid.
     * @throws [MercuryError.NoDIDSenderSetError] if DIDSender is invalid.
     */
    @Throws(MercuryError.NoDIDReceiverSetError::class, MercuryError.NoDIDSenderSetError::class)
    override fun packMessage(message: Message): String {
        if (message.to !is DID) {
            throw MercuryError.NoDIDReceiverSetError()
        }

        if (message.from !is DID) {
            throw MercuryError.NoDIDSenderSetError()
        }

        return protocol.packEncrypted(message)
    }

    /**
     * Asynchronously unpacks a given string representation of a message into a message object. This
     * function may throw an error if the string is not a valid message representation.
     *
     * @param message The string representation of the message to unpack
     * @return The message object
     */
    override fun unpackMessage(message: String): Message {
        return protocol.unpack(message)
    }

    /**
     * Asynchronously sends a given message and returns the response data.
     *
     * @param message The message to send
     * @return The response data
     * @throws [MercuryError.NoDIDReceiverSetError] if DIDReceiver is invalid.
     * @throws [MercuryError.NoDIDSenderSetError] if DIDSender is invalid.
     */
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

    /**
     * Asynchronously sends a given message and returns the response message object.
     *
     * @param message The message to send
     * @return The response message object or null
     */
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
            body = ForwardMessage.ForwardBody(message.to.toString()),
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
            arrayOf(KeyValue(HttpHeaders.ContentType, Typ.Encrypted.typ)),
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
