package io.iohk.atala.prism.domain.buildingBlocks

import io.iohk.atala.prism.domain.models.DID
import io.iohk.atala.prism.domain.models.DIDPair
import io.iohk.atala.prism.domain.models.Direction
import io.iohk.atala.prism.domain.models.MediatorDID
import io.iohk.atala.prism.domain.models.Message
import io.iohk.atala.prism.domain.models.PeerDID
import io.iohk.atala.prism.domain.models.PrismDIDInfo
import io.iohk.atala.prism.domain.models.PrivateKey
import io.iohk.atala.prism.domain.models.VerifiableCredential
import kotlinx.coroutines.flow.Flow

interface Pluto {

    fun storePrismDID(
        did: DID,
        keyPathIndex: Int,
        alias: String?
    )

    fun storePeerDID(did: DID, privateKeys: List<PrivateKey>)

    fun storeDIDPair(host: DID, receiver: DID, name: String)

    fun storeMessage(message: Message)

    fun storeMessages(messages: List<Message>)

    fun storeMediator(mediator: DID, host: DID, routing: DID)

    fun storeCredential(credential: VerifiableCredential)

    fun getAllPrismDIDs(): Flow<List<PrismDIDInfo>>

    fun getDIDInfoByDID(did: DID): Flow<PrismDIDInfo?>

    fun getDIDInfoByAlias(alias: String): Flow<List<PrismDIDInfo>>

    fun getPrismDIDkeyPathIndex(did: DID): Flow<Int?>

    fun getPrismLastkeyPathIndex(): Flow<Int>

    fun getAllPeerDIDs(): Flow<List<PeerDID>>

    fun getDIDPrivateKeysByDID(did: DID): Flow<List<PrivateKey>?>

    fun getAllDidPairs(): Flow<List<DIDPair>>

    fun getPair(did: DID): Flow<List<DIDPair>?>

    fun getPair(name: String): Flow<DIDPair?>

    fun getAllMessages(): Flow<List<Message>>

    fun getAllMessages(did: DID): Flow<List<Message>>

    fun getAllMessagesSent(): Flow<List<Message>>

    fun getAllMessagesReceived(): Flow<List<Message>>

    fun getAllMessagesSentTo(did: DID): Flow<List<Message>>

    fun getAllMessagesReceivedFrom(did: DID): Flow<List<Message>>

    fun getAllMessagesOfType(type: String, relatedWithDID: DID?): Flow<List<Message>>

    fun getAllMessages(from: DID, to: DID): Flow<List<Message>>

    fun getMessage(id: String): Flow<Message?>

    fun getAllMediators(): Flow<List<MediatorDID>>

    fun getAllCredentials(): Flow<List<VerifiableCredential>>
}
