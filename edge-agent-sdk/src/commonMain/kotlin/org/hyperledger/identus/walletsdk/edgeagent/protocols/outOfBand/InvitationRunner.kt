package org.hyperledger.identus.walletsdk.edgeagent.protocols.outOfBand

import io.ktor.http.Url
import org.hyperledger.identus.walletsdk.domain.buildingblocks.Mercury
import org.hyperledger.identus.walletsdk.domain.models.Message

/**
 * The InvitationRunner class is responsible for running the invitation process by parsing the out-of-band URL,
 * unpacking the message, and returning the unpacked message object.
 *
 * @param mercury The Mercury interface implementation used for packing and unpacking messages.
 * @param url The URL object representing the out-of-band URL.
 */
class InvitationRunner(private val mercury: Mercury, private val url: Url) {
    /**
     * Runs the invitation process by parsing the out-of-band URL, unpacking the message, and returning the unpacked message object.
     *
     * @return The unpacked [Message] object.
     */
    fun run(): Message {
        val messageString = OutOfBandParser().parseMessage(url)
        return mercury.unpackMessage(messageString)
    }
}
