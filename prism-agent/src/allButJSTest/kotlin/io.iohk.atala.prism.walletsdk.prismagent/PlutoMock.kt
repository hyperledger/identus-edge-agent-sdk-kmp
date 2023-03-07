package io.iohk.atala.prism.walletsdk.prismagent

import io.iohk.atala.prism.walletsdk.domain.buildingBlocks.Pluto
import io.iohk.atala.prism.walletsdk.domain.models.DID
import io.iohk.atala.prism.walletsdk.domain.models.DIDPair
import io.iohk.atala.prism.walletsdk.domain.models.Mediator
import io.iohk.atala.prism.walletsdk.domain.models.Message
import io.iohk.atala.prism.walletsdk.domain.models.PeerDID
import io.iohk.atala.prism.walletsdk.domain.models.PrismDIDInfo
import io.iohk.atala.prism.walletsdk.domain.models.PrivateKey
import io.iohk.atala.prism.walletsdk.domain.models.VerifiableCredential
import kotlinx.coroutines.flow.Flow

class PlutoMock : Pluto {
    var wasStorePrismDIDAndPrivateKeysCalled: Boolean = false
    var wasStorePeerDIDAndPrivateKeysCalled: Boolean = false
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
    var wasGetDIDPrivateKeysByIDCalled: Boolean = false
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

    var storedPrismDID: List<DID> = listOf()
    var storedPeerDID: List<DID> = listOf()
    lateinit var getAllPrismDIDsReturn: Flow<List<PrismDIDInfo>>
    lateinit var getDIDInfoByDIDReturn: Flow<PrismDIDInfo?>
    lateinit var getPrismDIDKeyPathIndexReturn: Flow<Int?>
    lateinit var getPrismLastKeyPathIndexReturn: Flow<Int>
    lateinit var getAllPeerDIDsReturn: Flow<List<PeerDID>>
    lateinit var getDIDPrivateKeysReturn: Flow<List<PrivateKey?>>
    lateinit var getDIDPrivateKeysByIDReturn: Flow<PrivateKey?>
    lateinit var getAllDidPairsReturn: Flow<List<DIDPair>>
    lateinit var getPairByDIDReturn: Flow<DIDPair?>
    lateinit var getPairByNameReturn: Flow<DIDPair?>
    lateinit var getAllMessagesReturn: Flow<List<Message>>
    lateinit var getAllMessagesSentReturn: Flow<List<Message>>
    lateinit var getAllMessagesSentToReturn: Flow<List<Message>>
    lateinit var getAllMessagesReceivedReturn: Flow<List<Message>>
    lateinit var getAllMessagesReceivedFromReturn: Flow<List<Message>>
    lateinit var getAllMessagesOfTypeReturn: Flow<List<Message>>
    lateinit var getMessageReturn: Flow<Message?>
    lateinit var getAllMediatorsReturn: Flow<List<Mediator>>
    lateinit var getAllCredentialsReturn: Flow<List<VerifiableCredential>>

    override fun storePrismDIDAndPrivateKeys(
        did: DID,
        keyPathIndex: Int,
        alias: String?,
        privateKeys: List<PrivateKey>,
    ) {
        storedPrismDID += did
        wasStorePrismDIDAndPrivateKeysCalled = true
    }

    override fun storePeerDIDAndPrivateKeys(did: DID, privateKeys: List<PrivateKey>) {
        storedPeerDID += did
        wasStorePeerDIDAndPrivateKeysCalled = true
    }

    override fun storeDIDPair(host: DID, receiver: DID, name: String) {
        wasStoreDIDPairCalled = true
    }

    override fun storeMessage(message: Message) {
        wasStoreMessageCalled = true
    }

    override fun storeMessages(messages: List<Message>) {
        wasStoreMessagesCalled = true
    }

    override fun storePrivateKeys(privateKey: PrivateKey, did: DID, keyPathIndex: Int, metaId: String?) {
        wasStorePrivateKeysCalled = true
    }

    override fun storeMediator(mediator: DID, host: DID, routing: DID) {
        wasStoreMediatorCalled = true
    }

    override fun storeCredential(credential: VerifiableCredential) {
        wasStoreCredentialCalled = true
    }

    override fun getAllPrismDIDs(): Flow<List<PrismDIDInfo>> {
        wasGetAllPrismDIDsCalled = true
        return getAllPrismDIDsReturn
    }

    override fun getDIDInfoByDID(did: DID): Flow<PrismDIDInfo?> {
        wasGetDIDInfoByDIDCalled = true
        return getDIDInfoByDIDReturn
    }

    override fun getDIDInfoByAlias(alias: String): Flow<List<PrismDIDInfo>> = getAllPrismDIDsReturn

    override fun getPrismDIDKeyPathIndex(did: DID): Flow<Int?> = getPrismDIDKeyPathIndexReturn

    override fun getPrismLastKeyPathIndex(): Flow<Int> {
        wasGetPrismLastKeyPathIndexCalled = true
        return getPrismLastKeyPathIndexReturn
    }

    override fun getAllPeerDIDs(): Flow<List<PeerDID>> {
        wasGetAllPeerDIDsCalled = true
        return getAllPeerDIDsReturn
    }

    override fun getDIDPrivateKeysByDID(did: DID): Flow<List<PrivateKey?>> {
        wasGetDIDPrivateKeysByDIDCalled = true
        return getDIDPrivateKeysReturn
    }

    override fun getDIDPrivateKeyByID(id: String): Flow<PrivateKey?> {
        wasGetDIDPrivateKeysByIDCalled = true
        return getDIDPrivateKeysByIDReturn
    }

    override fun getAllDidPairs(): Flow<List<DIDPair>> {
        wasGetAllDidPairsCalled = true
        return getAllDidPairsReturn
    }

    override fun getPairByDID(did: DID): Flow<DIDPair?> {
        wasGetPairByDIDCalled = true
        return getPairByDIDReturn
    }

    override fun getPairByName(name: String): Flow<DIDPair?> {
        wasGetPairByNameCalled = true
        return getPairByNameReturn
    }

    override fun getAllMessages(): Flow<List<Message>> {
        wasGetAllMessagesCalled = true
        return getAllMessagesReturn
    }

    override fun getAllMessages(did: DID): Flow<List<Message>> {
        wasGetAllMessagesCalled = true
        return getAllMessagesReturn
    }

    override fun getAllMessagesSent(): Flow<List<Message>> {
        wasGetAllMessagesSentCalled = true
        return getAllMessagesSentReturn
    }

    override fun getAllMessagesReceived(): Flow<List<Message>> {
        wasGetAllMessagesReceivedCalled = true
        return getAllMessagesReceivedReturn
    }

    override fun getAllMessagesSentTo(did: DID): Flow<List<Message>> {
        wasGetAllMessagesSentToCalled = true
        return getAllMessagesSentToReturn
    }

    override fun getAllMessagesReceivedFrom(did: DID): Flow<List<Message>> {
        wasGetAllMessagesReceivedFromCalled = true
        return getAllMessagesReceivedFromReturn
    }

    override fun getAllMessagesOfType(type: String, relatedWithDID: DID?): Flow<List<Message>> {
        wasGetAllMessagesOfTypeCalled = true
        return getAllMessagesOfTypeReturn
    }

    override fun getAllMessages(from: DID, to: DID): Flow<List<Message>> {
        wasGetAllMessagesCalled = true
        return getAllMessagesReturn
    }

    override fun getMessage(id: String): Flow<Message?> {
        wasGetMessageCalled = true
        return getMessageReturn
    }

    override fun getAllMediators(): Flow<List<Mediator>> {
        wasGetAllMediatorsCalled = true
        return getAllMediatorsReturn
    }

    override fun getAllCredentials(): Flow<List<VerifiableCredential>> {
        wasGetAllCredentialsCalled = true
        return getAllCredentialsReturn
    }
}
