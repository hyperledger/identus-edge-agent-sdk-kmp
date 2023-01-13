package io.iohk.atala.prism.domain.buildingBlocks

interface Pluto {

    fun storePrismDID(
        did: DID,
        keyPairIndex: Int,
        alias: String?
    ): Flow<Void, Error>

    fun storePeerDID(did: DID, privateKeys: Array<PrivateKey>): Flow<Void, Error>

    fun storeDIDPair(holder: DID, other: DID, name: String): Flow<Void, Error>

    fun storeMessage(message: Message, direction: Message.Direction): Flow<Void, Error>

    fun storeMessages(messages: Map<Message, Message.Direction>): Flow<Void, Error>

    fun storeMediator(peer: DID, routingDID: DID, mediatorDID: DID): Flow<Void, Error>

    fun storeCredential(credential: VerifiableCredential): Flow<Void, Error>

    fun getAllPrismDIDs(): Flow<Array<Triple<DID, Int, String?>>, Error>

    fun getPrismDIDInfo(did: DID): Flow<(did: DID, keyPairIndex: Int, alias: String?)?, Error>

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

    fun getAllMediators(): Flow<Array<Triple<(DID, DID, DID>>, Error>

    fun getAllCredentials(): Flow<Array<VerifiableCredential>, Error>

}