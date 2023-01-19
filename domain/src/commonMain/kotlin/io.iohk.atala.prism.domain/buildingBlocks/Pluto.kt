package io.iohk.atala.prism.domain.buildingBlocks

import io.iohk.atala.prism.domain.models.DID
import io.iohk.atala.prism.domain.models.PrivateKey
import io.iohk.atala.prism.domain.models.Message
import io.iohk.atala.prism.domain.models.VerifiableCredential
import io.iohk.atala.prism.domain.models.PrismDIDInfo
import io.iohk.atala.prism.domain.models.PeerDID
import io.iohk.atala.prism.domain.models.DIDPair
import io.iohk.atala.prism.domain.models.MediatorDID
import kotlinx.coroutines.flow.Flow

interface Pluto {

    fun storePrismDID(
        did: DID,
        keyPairIndex: Int,
        alias: String?
    )

    fun storePeerDID(did: DID, privateKeys: Array<PrivateKey>)

    fun storeDIDPair(holder: DID, other: DID, name: String)

    fun storeMessage(message: Message, direction: Message.Direction)

    fun storeMessages(messages: Map<Message, Message.Direction>)

    fun storeMediator(peer: DID, routingDID: DID, mediatorDID: DID)

    fun storeCredential(credential: VerifiableCredential)

    fun getAllPrismDIDs(): Flow<Array<PrismDIDInfo>>

    fun getPrismDIDInfo(did: DID): Flow<PrismDIDInfo?>

    fun getPrismDIDInfo(alias: String): Flow<Array<PrismDIDInfo>>

    fun getPrismDIDKeyPairIndex(did: DID): Flow<Int?>

    fun getPrismLastKeyPairIndex(): Flow<Int>

    fun getAllPeerDIDs(): Flow<Array<PeerDID>>

    fun getPeerDIDInfo(did: DID): Flow<PeerDID?>

    fun getPeerDIDPrivateKeys(did: DID): Flow<Array<PrivateKey>?>

    fun getAllDidPairs(): Flow<Array<DIDPair>>

    fun getPair(did: DID): Flow<Array<DIDPair>?>

    fun getPair(name: String): Flow<DIDPair?>

    fun getAllMessages(): Flow<Array<Message>>

    fun getAllMessages(did: DID): Flow<Array<Message>>

    fun getAllMessagesSent(): Flow<Array<Message>>

    fun getAllMessagesReceived(): Flow<Array<Message>>

    fun getAllMessagesSentTo(did: DID): Flow<Array<Message>>

    fun getAllMessagesReceivedFrom(did: DID): Flow<Array<Message>>

    fun getAllMessagesOfType(type: String, relatedWithDID: DID?): Flow<Array<Message>>

    fun getAllMessages(from: DID, to: DID): Flow<Array<Message>>

    fun getMessage(id: String): Flow<Message?>

    fun getAllMediators(): Flow<Array<MediatorDID>>

    fun getAllCredentials(): Flow<Array<VerifiableCredential>>
}
