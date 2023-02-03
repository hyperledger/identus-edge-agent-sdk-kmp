package io.iohk.atala.prism.domain.buildingBlocks

import io.iohk.atala.prism.domain.models.DID
import io.iohk.atala.prism.domain.models.DIDPair
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

    fun storePeerDID(did: DID, privateKeys: Array<PrivateKey>)

    fun storeDIDPair(host: DID, receiver: DID, name: String)

    fun storeMessage(message: Message)

    fun storeMessages(messages: Array<Message>)

    fun storePrivateKeys(privateKey: PrivateKey, did: DID, keyPathIndex: Int)

    fun storeMediator(mediator: DID, host: DID, routing: DID)

    fun storeCredential(credential: VerifiableCredential)

    fun getAllPrismDIDs(): Flow<Array<PrismDIDInfo>>

    fun getDIDInfoByDID(did: DID): Flow<PrismDIDInfo?>

    fun getDIDInfoByAlias(alias: String): Flow<Array<PrismDIDInfo>>

    fun getPrismDIDKeyPathIndex(did: DID): Flow<Int?>

    fun getPrismLastKeyPathIndex(): Flow<Int>

    fun getAllPeerDIDs(): Flow<Array<PeerDID>>

    fun getDIDPrivateKeysByDID(did: DID): Flow<Array<PrivateKey>?>

    fun getAllDidPairs(): Flow<Array<DIDPair>>

    fun getPairByDID(did: DID): Flow<Array<DIDPair>?>

    fun getPairByName(name: String): Flow<DIDPair?>

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
