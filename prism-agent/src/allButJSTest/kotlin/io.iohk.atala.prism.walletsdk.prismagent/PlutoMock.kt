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

    var storedPrismDID: Array<DID> = emptyArray()
    var storedPeerDID: Array<DID> = emptyArray()
    var getAllPrismDIDsReturn: Array<PrismDIDInfo> = emptyArray()
    var getDIDInfoByDIDReturn: PrismDIDInfo? = null
    var getPrismDIDKeyPathIndexReturn: Int? = null
    var getPrismLastKeyPathIndexReturn: Int = 0
    var getAllPeerDIDsReturn: Array<PeerDID> = emptyArray()
    var getDIDPrivateKeysReturn: Array<PrivateKey>? = emptyArray()
    var getDIDPrivateKeysByIDReturn: PrivateKey? = null
    var getAllDidPairsReturn: Array<DIDPair> = emptyArray()
    var getPairByDIDReturn: DIDPair? = null
    var getPairByNameReturn: DIDPair? = null
    var getAllMessagesReturn: Array<Message> = emptyArray()
    var getAllMessagesSentReturn: Array<Message> = emptyArray()
    var getAllMessagesSentToReturn: Array<Message> = emptyArray()
    var getAllMessagesReceivedReturn: Array<Message> = emptyArray()
    var getAllMessagesReceivedFromReturn: Array<Message> = emptyArray()
    var getAllMessagesOfTypeReturn: Array<Message> = emptyArray()
    var getMessageReturn: Message? = null
    var getAllMediatorsReturn: Array<Mediator> = emptyArray()
    var getAllCredentialsReturn: Array<VerifiableCredential> = emptyArray()

    override fun storePrismDID(
        did: DID,
        keyPathIndex: Int,
        alias: String?,
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

    override fun storePrivateKeys(privateKey: PrivateKey, did: DID, keyPathIndex: Int, metaId: String?) {
        wasStorePrivateKeysCalled = true
    }

    override fun storeMediator(mediator: DID, host: DID, routing: DID) {
        wasStoreMediatorCalled = true
    }

    override fun storeCredential(credential: VerifiableCredential) {
        wasStoreCredentialCalled = true
    }

    override fun getAllPrismDIDs(): Array<PrismDIDInfo> {
        wasGetAllPrismDIDsCalled = true
        return getAllPrismDIDsReturn
    }

    override fun getDIDInfoByDID(did: DID): PrismDIDInfo? {
        wasGetDIDInfoByDIDCalled = true
        return getDIDInfoByDIDReturn
    }

    override fun getDIDInfoByAlias(alias: String): Array<PrismDIDInfo> = getAllPrismDIDsReturn

    override fun getPrismDIDKeyPathIndex(did: DID): Int? = getPrismDIDKeyPathIndexReturn

    override fun getPrismLastKeyPathIndex(): Int {
        wasGetPrismLastKeyPathIndexCalled = true
        return getPrismLastKeyPathIndexReturn
    }

    override fun getAllPeerDIDs(): Array<PeerDID> {
        wasGetAllPeerDIDsCalled = true
        return getAllPeerDIDsReturn
    }

    override fun getDIDPrivateKeysByDID(did: DID): Array<PrivateKey>? {
        wasGetDIDPrivateKeysByDIDCalled = true
        return getDIDPrivateKeysReturn
    }

    override fun getDIDPrivateKeyByID(id: String): PrivateKey? {
        wasGetDIDPrivateKeysByIDCalled = true
        return getDIDPrivateKeysByIDReturn
    }

    override fun getAllDidPairs(): Array<DIDPair> {
        wasGetAllDidPairsCalled = true
        return getAllDidPairsReturn
    }

    override fun getPairByDID(did: DID): DIDPair? {
        wasGetPairByDIDCalled = true
        return getPairByDIDReturn
    }

    override fun getPairByName(name: String): DIDPair? {
        wasGetPairByNameCalled = true
        return getPairByNameReturn
    }

    override fun getAllMessages(): Array<Message> {
        wasGetAllMessagesCalled = true
        return getAllMessagesReturn
    }

    override fun getAllMessages(did: DID): Array<Message> {
        wasGetAllMessagesCalled = true
        return getAllMessagesReturn
    }

    override fun getAllMessagesSent(): Array<Message> {
        wasGetAllMessagesSentCalled = true
        return getAllMessagesSentReturn
    }

    override fun getAllMessagesReceived(): Array<Message> {
        wasGetAllMessagesReceivedCalled = true
        return getAllMessagesReceivedReturn
    }

    override fun getAllMessagesSentTo(did: DID): Array<Message> {
        wasGetAllMessagesSentToCalled = true
        return getAllMessagesSentToReturn
    }

    override fun getAllMessagesReceivedFrom(did: DID): Array<Message> {
        wasGetAllMessagesReceivedFromCalled = true
        return getAllMessagesReceivedFromReturn
    }

    override fun getAllMessagesOfType(type: String, relatedWithDID: DID?): Array<Message> {
        wasGetAllMessagesOfTypeCalled = true
        return getAllMessagesOfTypeReturn
    }

    override fun getAllMessages(from: DID, to: DID): Array<Message> {
        wasGetAllMessagesCalled = true
        return getAllMessagesReturn
    }

    override fun getMessage(id: String): Message? {
        wasGetMessageCalled = true
        return getMessageReturn
    }

    override fun getAllMediators(): Array<Mediator> {
        wasGetAllMediatorsCalled = true
        return getAllMediatorsReturn
    }

    override fun getAllCredentials(): Array<VerifiableCredential> {
        wasGetAllCredentialsCalled = true
        return getAllCredentialsReturn
    }
}
