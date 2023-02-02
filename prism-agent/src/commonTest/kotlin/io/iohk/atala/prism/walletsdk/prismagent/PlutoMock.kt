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
    var wasStorePrismDIDCalled: Boolean = false
    var wasStorePeerDIDCalled: Boolean = false
    var wasStoreDIDPairCalled: Boolean = false
    var wasStoreMessageCalled: Boolean = false
    var wasStoreMessagesCalled: Boolean = false
    var wasStorePrivateKeysCalled: Boolean = false
    var wasStoreMediatorCalled: Boolean = false
    var wasStoreCredentialCalled: Boolean = false
    var wasGetAllPrismDIDsCalled: Boolean = false
    var wasGetDIDInfoByDIDCalled: Boolean = false
    var wasGetPrismLastKeyPathIndexCalled: Boolean = false
    var wasGetAllPeerDIDsCalled: Boolean = false
    var wasGetDIDPrivateKeysByDIDCalled: Boolean = false
    var wasGetAllDidPairsCalled: Boolean = false
    var wasGetPairByDIDCalled: Boolean = false
    var wasGetPairByNameCalled: Boolean = false
    var wasGetAllMessagesCalled: Boolean = false
    var wasGetAllMessagesSentCalled: Boolean = false
    var wasGetAllMessagesSentToCalled: Boolean = false
    var wasGetAllMessagesReceivedCalled: Boolean = false
    var wasGetAllMessagesReceivedFromCalled: Boolean = false
    var wasGetAllMessagesOfTypeCalled: Boolean = false
    var wasGetMessageCalled: Boolean = false
    var wasGetAllMediatorsCalled: Boolean = false
    var wasGetAllCredentialsCalled: Boolean = false

    var storedPrismDID: Array<DID> = emptyArray()
    var storedPeerDID: Array<DID> = emptyArray()
    var getAllPrismDIDsReturn: Flow<Array<PrismDIDInfo>> = flow { emit(emptyArray()) }
    var getDIDInfoByDIDReturn: Flow<PrismDIDInfo?> = flow { emit(null) }
    var getPrismDIDKeyPathIndexReturn: Flow<Int?> = flow { emit(null) }
    var getPrismLastKeyPathIndexReturn: Flow<Int> = flow { emit(0) }
    var getAllPeerDIDsReturn: Flow<Array<PeerDID>> = flow { emit(emptyArray()) }
    var getDIDPrivateKeysReturn: Flow<Array<PrivateKey>?> = flow { emit(emptyArray()) }
    var getAllDidPairsReturn: Flow<Array<DIDPair>> = flow { emit(emptyArray()) }
    var getPairByDIDReturn: Flow<DIDPair?> = flow { emit(null) }
    var getPairByNameReturn: Flow<DIDPair?> = flow { emit(null) }
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
        wasStorePrismDIDCalled = true
    }

    override fun storePeerDID(did: DID, privateKeys: Array<PrivateKey>) {
        storedPeerDID += did
        wasStorePeerDIDCalled = true
    }

    override fun storeDIDPair(host: DID, receiver: DID, name: String) {
        wasStoreDIDPairCalled = true
    }

    override fun storeMessage(message: Message) {
        wasStoreMessageCalled = true
    }

    override fun storeMessages(messages: Array<Message>) {
        wasStoreMessagesCalled = true
    }

    override fun storePrivateKeys(privateKey: PrivateKey, did: DID, keyPathIndex: Int) {
        wasStorePrivateKeysCalled = true
    }

    override fun storeMediator(mediator: DID, host: DID, routing: DID) {
        wasStoreMediatorCalled = true
    }

    override fun storeCredential(credential: VerifiableCredential) {
        wasStoreCredentialCalled = true

    }

    override fun getAllPrismDIDs(): Flow<Array<PrismDIDInfo>> {
        wasGetAllPrismDIDsCalled = true
        return getAllPrismDIDsReturn
    }

    override fun getDIDInfoByDID(did: DID): Flow<PrismDIDInfo?> {
        wasGetDIDInfoByDIDCalled = true
        return getDIDInfoByDIDReturn
    }

    override fun getDIDInfoByAlias(alias: String): Flow<Array<PrismDIDInfo>> = getAllPrismDIDsReturn

    override fun getPrismDIDKeyPathIndex(did: DID): Flow<Int?> = getPrismDIDKeyPathIndexReturn

    override fun getPrismLastKeyPathIndex(): Flow<Int> {
        wasGetPrismLastKeyPathIndexCalled = true
        return getPrismLastKeyPathIndexReturn
    }


    override fun getAllPeerDIDs(): Flow<Array<PeerDID>> {
        wasGetAllPeerDIDsCalled = true
        return getAllPeerDIDsReturn
    }

    override fun getDIDPrivateKeysByDID(did: DID): Flow<Array<PrivateKey>?> {
        wasGetDIDPrivateKeysByDIDCalled = true
        return getDIDPrivateKeysReturn
    }

    override fun getAllDidPairs(): Flow<Array<DIDPair>> {
        wasGetAllDidPairsCalled = true
        return getAllDidPairsReturn
    }

    override fun getPairByDID(did: DID): Flow<Array<DIDPair>?> {
        wasGetPairByDIDCalled = true
        return getAllDidPairsReturn
    }

    override fun getPairByName(name: String): Flow<DIDPair?> {
        wasGetPairByNameCalled = true
        return getPairByNameReturn
    }

    override fun getAllMessages(): Flow<Array<Message>> {
        wasGetAllMessagesCalled = true
        return getAllMessagesReturn
    }

    override fun getAllMessages(did: DID): Flow<Array<Message>> {
        wasGetAllMessagesCalled = true
        return getAllMessagesReturn
    }

    override fun getAllMessagesSent(): Flow<Array<Message>> {
       wasGetAllMessagesSentCalled = true
       return getAllMessagesSentReturn
    }

    override fun getAllMessagesReceived(): Flow<Array<Message>> {
        wasGetAllMessagesReceivedCalled = true
        return getAllMessagesReceivedReturn
    }

    override fun getAllMessagesSentTo(did: DID): Flow<Array<Message>> {
        wasGetAllMessagesSentToCalled = true
        return getAllMessagesSentToReturn
    }

    override fun getAllMessagesReceivedFrom(did: DID): Flow<Array<Message>> {
        wasGetAllMessagesReceivedFromCalled = true
        return getAllMessagesReceivedFromReturn
    }

    override fun getAllMessagesOfType(type: String, relatedWithDID: DID?): Flow<Array<Message>> {
        wasGetAllMessagesOfTypeCalled = true
        return getAllMessagesOfTypeReturn
    }

    override fun getAllMessages(from: DID, to: DID): Flow<Array<Message>> {
        wasGetAllMessagesCalled = true
        return getAllMessagesReturn
    }

    override fun getMessage(id: String): Flow<Message?> {
        wasGetMessageCalled = true
        return getMessageReturn
    }

    override fun getAllMediators(): Flow<Array<MediatorDID>> {
        wasGetAllMediatorsCalled = true
        return getAllMediatorsReturn
    }

    override fun getAllCredentials(): Flow<Array<VerifiableCredential>> {
        wasGetAllCredentialsCalled = true
        return getAllCredentialsReturn
    }
}
