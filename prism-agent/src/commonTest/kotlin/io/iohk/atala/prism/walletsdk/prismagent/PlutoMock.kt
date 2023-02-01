package io.iohk.atala.prism.walletsdk.prismagent

import io.iohk.atala.prism.domain.buildingBlocks.Pluto
import io.iohk.atala.prism.domain.models.DID
import io.iohk.atala.prism.domain.models.DIDPair
import io.iohk.atala.prism.domain.models.MediatorDID
import io.iohk.atala.prism.domain.models.Message
import io.iohk.atala.prism.domain.models.PeerDID
import io.iohk.atala.prism.domain.models.PrismDIDInfo
import io.iohk.atala.prism.domain.models.PrivateKey
import io.iohk.atala.prism.domain.models.VerifiableCredential
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class PlutoMock : Pluto {
    var storedPrismDID: Array<DID> = emptyArray()
    var storedPeerDID: Array<DID> = emptyArray()
    var getAllPrismDIDsReturn: Flow<Array<PrismDIDInfo>> = flow { emit(emptyArray()) }
    var getDIDInfoByDIDReturn: Flow<PrismDIDInfo?> = flow { emit(null) }
    var getPrismDIDKeyPathIndexReturn: Flow<Int?> = flow { emit(null) }
    var getPrismLastKeyPairIndexReturn: Flow<Int> = flow { emit(0) }
    var getAllPeerDIDsReturn: Flow<Array<PeerDID>> = flow { emit(emptyArray()) }
    var getDIDPrivateKeysReturn: Flow<Array<PrivateKey>?> = flow { emit(emptyArray()) }
    var getAllDidPairsReturn: Flow<Array<DIDPair>> = flow { emit(emptyArray()) }
    var getPairReturn: Flow<DIDPair?> = flow { emit(null) }
    var getAllMessagesReturn: Flow<Array<Message>> = flow { emit(emptyArray()) }
    var getAllMessagesSentReturn: Flow<Array<Message>> = flow { emit(emptyArray()) }
    var getAllMessagesSentToReturn: Flow<Array<Message>> = flow { emit(emptyArray()) }
    var getAllMessagesReceivedReturn: Flow<Array<Message>> = flow { emit(emptyArray()) }
    var getAllMessagesReceivedFromReturn: Flow<Array<Message>> = flow { emit(emptyArray()) }
    var getAllMessagesOfTypeReturn: Flow<Array<Message>> = flow { emit(emptyArray()) }
    var getMessageReturn: Flow<Message?> = flow { emit(null) }
    var getAllMediatorsReturn: Flow<Array<MediatorDID>> = flow { emit(emptyArray()) }
    var getAllCredentialsReturn: Flow<Array<VerifiableCredential>> = flow { emit(emptyArray()) }

    override fun storePrismDID(
        did: DID,
        keyPathIndex: Int,
        alias: String?
    ) {
        storedPrismDID += did
    }

    override fun storePeerDID(did: DID, privateKeys: Array<PrivateKey>) {
        storedPeerDID += did
    }

    override fun storeDIDPair(host: DID, receiver: DID, name: String) {}

    override fun storeMessage(message: Message) {}

    override fun storeMessages(messages: Array<Message>) {}

    override fun storeMediator(mediator: DID, host: DID, routing: DID) {}

    override fun storeCredential(credential: VerifiableCredential) {}

    override fun getAllPrismDIDs(): Flow<Array<PrismDIDInfo>> = getAllPrismDIDsReturn

    override fun getDIDInfoByDID(did: DID): Flow<PrismDIDInfo?> = getDIDInfoByDIDReturn

    override fun getDIDInfoByAlias(alias: String): Flow<Array<PrismDIDInfo>> = getAllPrismDIDsReturn

    override fun getPrismDIDKeyPathIndex(did: DID): Flow<Int?> = getPrismDIDKeyPathIndexReturn

    override fun getPrismLastKeyPathIndex(): Flow<Int> = getPrismLastKeyPairIndexReturn

    override fun getAllPeerDIDs(): Flow<Array<PeerDID>> = getAllPeerDIDsReturn

    override fun getDIDPrivateKeysByDID(did: DID): Flow<Array<PrivateKey>?> = getDIDPrivateKeysReturn

    override fun getAllDidPairs(): Flow<Array<DIDPair>> = getAllDidPairsReturn

    override fun getPair(did: DID): Flow<Array<DIDPair>?> = getAllDidPairsReturn

    override fun getPair(name: String): Flow<DIDPair?> = getPairReturn

    override fun getAllMessages(): Flow<Array<Message>> = getAllMessagesReturn

    override fun getAllMessages(did: DID): Flow<Array<Message>> = getAllMessagesReturn

    override fun getAllMessagesSent(): Flow<Array<Message>> = getAllMessagesSentReturn

    override fun getAllMessagesReceived(): Flow<Array<Message>> = getAllMessagesReceivedReturn

    override fun getAllMessagesSentTo(did: DID): Flow<Array<Message>> = getAllMessagesSentToReturn

    override fun getAllMessagesReceivedFrom(did: DID): Flow<Array<Message>> = getAllMessagesReceivedFromReturn

    override fun getAllMessagesOfType(type: String, relatedWithDID: DID?): Flow<Array<Message>> = getAllMessagesOfTypeReturn

    override fun getAllMessages(from: DID, to: DID): Flow<Array<Message>> = getAllMessagesReturn

    override fun getMessage(id: String): Flow<Message?> = getMessageReturn

    override fun getAllMediators(): Flow<Array<MediatorDID>> = getAllMediatorsReturn

    override fun getAllCredentials(): Flow<Array<VerifiableCredential>> = getAllCredentialsReturn
}
