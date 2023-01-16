package io.iohk.atala.prism.domain.buildingBlocks

import io.iohk.atala.prism.domain.models.DID
import io.iohk.atala.prism.domain.models.Message
import io.iohk.atala.prism.domain.models.PrivateKey
import io.iohk.atala.prism.domain.models.VerifiableCredential
import kotlinx.coroutines.flow.Flow

interface Pluto {

    fun storePrismDID(
        did: DID,
        keyPairIndex: Int,
        alias: String?
    ): Flow<Unit, Error>

    fun storePeerDID(did: DID, privateKeys: Array<PrivateKey>): Flow<Unit, Error>

    fun storeDIDPair(holder: DID, other: DID, name: String): Flow<Unit, Error>

    fun storeMessage(message: Message, direction: Message.Direction): Flow<Unit, Error>

    fun storeMessages(messages: Map<Message, Message.Direction>): Flow<Unit, Error>

    fun storeMediator(peer: DID, routingDID: DID, mediatorDID: DID): Flow<Unit, Error>

    fun storeCredential(credential: VerifiableCredential): Flow<Unit, Error>

    fun getAllPrismDIDs(): Flow<Array<Triple<DID, Int, String?>>, Error>

    fun getPrismDIDInfo(did: DID): Flow<Triple<DID, Int, String?>?, Error>

    fun getPrismDIDInfo(alias: String): Flow<Map<DID, Int>, Error>

    fun getPrismDIDKeyPairIndex(did: DID): Flow<Int?, Error>

    fun getPrismLastKeyPairIndex(): Flow<Int, Error>

    fun getAllPeerDIDs(): Flow<Map<DID, Array<PrivateKey>>, Error>

    fun getPeerDIDInfo(did: DID): Flow<Pair<DID, Array<PrivateKey>>?, Error>

    fun getPeerDIDPrivateKeys(did: DID): Flow<Array<PrivateKey>?, Error>

    fun getAllDidPairs(): Flow<Array<Triple<DID, DID, String?>>, Error>

    fun getPair(did: DID): Flow<Array<Triple<DID, DID, String?>>?, Error>

    fun getPair(name: String): Flow<Triple<DID, DID, String?>?, Error>

    fun getAllMessages(): Flow<Array<Message>, Error>

    fun getAllMessages(did: DID): Flow<Array<Message>, Error>

    fun getAllMessagesSent(): Flow<Array<Message>, Error>

    fun getAllMessagesReceived(): Flow<Array<Message>, Error>

    fun getAllMessagesSentTo(did: DID): Flow<Array<Message>, Error>

    fun getAllMessagesReceivedFrom(did: DID): Flow<Array<Message>, Error>

    fun getAllMessagesOfType(type: String, relatedWithDID: DID?): Flow<Array<Message>, Error>

    fun getAllMessages(from: DID, to: DID): Flow<Array<Message>, Error>

    fun getMessage(id: String): Flow<Message?, Error>

    fun getAllMediators(): Flow<Array<Triple<DID, DID, DID>>, Error>

    fun getAllCredentials(): Flow<Array<VerifiableCredential>, Error>

}