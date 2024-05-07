package org.hyperledger.identus.walletsdk.domain.buildingblocks

import org.hyperledger.identus.walletsdk.domain.models.MercuryError
import org.hyperledger.identus.walletsdk.domain.models.Message
import kotlin.jvm.Throws

/**
 * Mercury is a powerful and flexible library for working with decentralized identifiers and secure communications
 * protocols. Whether you are a developer looking to build a secure and private messaging app or a more complex
 * decentralized system requiring trusted peer-to-peer connections, Mercury provides the tools and features you need to
 * establish, manage, and secure your communications easily.
 */
interface Mercury {

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
    fun packMessage(message: Message): String

    /**
     * Asynchronously unpacks a given string representation of a message into a message object. This
     * function may throw an error if the string is not a valid message representation.
     *
     * @param message The string representation of the message to unpack
     * @return The message object
     */
    fun unpackMessage(message: String): Message

    /**
     * Asynchronously sends a given message and returns the response data.
     *
     * @param message The message to send
     * @return The response data
     * @throws [MercuryError.NoDIDReceiverSetError] if DIDReceiver is invalid.
     * @throws [MercuryError.NoDIDSenderSetError] if DIDSender is invalid.
     */
    @Throws(MercuryError.NoDIDReceiverSetError::class, MercuryError.NoDIDSenderSetError::class)
    suspend fun sendMessage(message: Message): ByteArray?

    /**
     * Asynchronously sends a given message and returns the response message object.
     *
     * @param message The message to send
     * @return The response message object or null
     */
    suspend fun sendMessageParseResponse(message: Message): Message?
}
