package io.iohk.atala.prism.walletsdk.domain.buildingBlocks

import io.iohk.atala.prism.walletsdk.domain.models.DID
import io.iohk.atala.prism.walletsdk.domain.models.DIDPair
import io.iohk.atala.prism.walletsdk.domain.models.Mediator
import io.iohk.atala.prism.walletsdk.domain.models.Message
import io.iohk.atala.prism.walletsdk.domain.models.PeerDID
import io.iohk.atala.prism.walletsdk.domain.models.PrismDIDInfo
import io.iohk.atala.prism.walletsdk.domain.models.PrivateKey
import io.iohk.atala.prism.walletsdk.domain.models.VerifiableCredential
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlin.js.JsName

@OptIn(ExperimentalJsExport::class)
@JsExport
interface Pluto {

    fun storePrismDID(
        did: DID,
        keyPathIndex: Int,
        alias: String?,
    )

    fun storePeerDID(did: DID, privateKeys: Array<PrivateKey>)

    fun storeDIDPair(host: DID, receiver: DID, name: String)

    fun storeMessage(message: Message)

    fun storeMessages(messages: Array<Message>)

    fun storePrivateKeys(privateKey: PrivateKey, did: DID, keyPathIndex: Int, metaId: String? = null)

    fun storeMediator(mediator: DID, host: DID, routing: DID)

    fun storeCredential(credential: VerifiableCredential)

    fun getAllPrismDIDs(): Array<PrismDIDInfo>

    fun getDIDInfoByDID(did: DID): PrismDIDInfo?

    fun getDIDInfoByAlias(alias: String): Array<PrismDIDInfo>

    fun getPrismDIDKeyPathIndex(did: DID): Int?

    fun getPrismLastKeyPathIndex(): Int

    fun getAllPeerDIDs(): Array<PeerDID>

    fun getDIDPrivateKeysByDID(did: DID): Array<PrivateKey>?

    fun getDIDPrivateKeyByID(id: String): PrivateKey?

    fun getAllDidPairs(): Array<DIDPair>

    fun getPairByDID(did: DID): DIDPair?

    fun getPairByName(name: String): DIDPair?

    @JsName("getAllMessages")
    fun getAllMessages(): Array<Message>

    @JsName("getAllMessagesByDID")
    fun getAllMessages(did: DID): Array<Message>

    fun getAllMessagesSent(): Array<Message>

    fun getAllMessagesReceived(): Array<Message>

    fun getAllMessagesSentTo(did: DID): Array<Message>

    fun getAllMessagesReceivedFrom(did: DID): Array<Message>

    fun getAllMessagesOfType(type: String, relatedWithDID: DID?): Array<Message>

    @JsName("getAllMessagesByFromToDID")
    fun getAllMessages(from: DID, to: DID): Array<Message>

    fun getMessage(id: String): Message?

    fun getAllMediators(): Array<Mediator>

    fun getAllCredentials(): Array<VerifiableCredential>
}
