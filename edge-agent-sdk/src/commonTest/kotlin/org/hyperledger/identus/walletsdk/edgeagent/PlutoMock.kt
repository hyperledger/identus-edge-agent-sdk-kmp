package org.hyperledger.identus.walletsdk.edgeagent

import kotlinx.coroutines.flow.Flow
import org.hyperledger.identus.walletsdk.domain.buildingblocks.Pluto
import org.hyperledger.identus.walletsdk.domain.models.DID
import org.hyperledger.identus.walletsdk.domain.models.DIDPair
import org.hyperledger.identus.walletsdk.domain.models.Mediator
import org.hyperledger.identus.walletsdk.domain.models.Message
import org.hyperledger.identus.walletsdk.domain.models.PeerDID
import org.hyperledger.identus.walletsdk.domain.models.PrismDIDInfo
import org.hyperledger.identus.walletsdk.domain.models.StorableCredential
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.StorableKey
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.StorablePrivateKey
import org.hyperledger.identus.walletsdk.pluto.CredentialRecovery
import org.hyperledger.identus.walletsdk.pluto.data.AvailableClaims
import org.hyperledger.identus.walletsdk.pluto.models.backup.BackupV0_0_1
import org.hyperledger.identus.walletsdk.pollux.models.CredentialRequestMeta

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
    lateinit var getDIDPrivateKeysReturn: Flow<List<StorablePrivateKey?>>
    lateinit var getDIDPrivateKeysByIDReturn: Flow<StorablePrivateKey?>
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
    lateinit var getAllCredentialsReturn: Flow<List<CredentialRecovery>>
    lateinit var getLinkSecretReturn: Flow<String>
    lateinit var getCredentialMetadataReturn: Flow<CredentialRequestMeta?>

    override fun storePrismDIDAndPrivateKeys(
        did: DID,
        keyPathIndex: Int?,
        alias: String?,
        privateKeys: List<StorableKey>
    ) {
        storedPrismDID += did
        wasStorePrismDIDAndPrivateKeysCalled = true
    }

    override fun storePeerDID(did: DID) {
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

    override fun storePrivateKeys(storableKey: StorableKey, did: DID, keyPathIndex: Int?, metaId: String?) {
        wasStorePrivateKeysCalled = true
    }

    override fun storePrivate(sorableKey: StorableKey, recoveryId: String) {
        TODO("Not yet implemented")
    }

    override fun storeMediator(mediator: DID, host: DID, routing: DID) {
        wasStoreMediatorCalled = true
    }

    override fun storeCredential(storableCredential: StorableCredential) {
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

    override fun getAllDIDs(): Flow<List<DID>> {
        TODO("Not yet implemented")
    }

    override fun getDIDPrivateKeysByDID(did: DID): Flow<List<StorablePrivateKey?>> {
        wasGetDIDPrivateKeysByDIDCalled = true
        return getDIDPrivateKeysReturn
    }

    override fun getDIDPrivateKeyByID(id: String): Flow<StorablePrivateKey?> {
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

    override fun getAllMessagesByType(type: String): Flow<List<Message>> {
        TODO("Not yet implemented")
    }

    override fun getMessage(id: String): Flow<Message?> {
        wasGetMessageCalled = true
        return getMessageReturn
    }

    override fun getMessageByThidAndPiuri(thid: String, piuri: String): Flow<Message?> {
        TODO("Not yet implemented")
    }

    override fun getAllMediators(): Flow<List<Mediator>> {
        wasGetAllMediatorsCalled = true
        return getAllMediatorsReturn
    }

    override fun getAllCredentials(): Flow<List<CredentialRecovery>> {
        wasGetAllCredentialsCalled = true
        return getAllCredentialsReturn
    }

    override fun insertAvailableClaim(credentialId: String, claim: String) {
        TODO("Not yet implemented")
    }

    override fun insertAvailableClaims(credentialId: String, claims: Array<String>) {
        TODO("Not yet implemented")
    }

    override fun getAvailableClaimsByCredentialId(credentialId: String): Flow<Array<AvailableClaims>> {
        TODO("Not yet implemented")
    }

    override fun getAvailableClaimsByClaim(claim: String): Flow<Array<AvailableClaims>> {
        TODO("Not yet implemented")
    }

    override fun storeLinkSecret(linkSecret: String) {
    }

    override fun storeCredentialMetadata(name: String, metadata: CredentialRequestMeta) {
        TODO("Not yet implemented")
    }

    override fun storeCredentialMetadata(name: String, linkSecretName: String, json: String) {
        TODO("Not yet implemented")
    }

    override fun getLinkSecret(): Flow<String> {
        return getLinkSecretReturn
    }

    override fun getCredentialMetadata(linkSecretName: String): Flow<CredentialRequestMeta?> {
        return getCredentialMetadataReturn
    }

    override fun revokeCredential(credentialId: String) {
        TODO("Not yet implemented")
    }

    override fun observeRevokedCredentials(): Flow<List<CredentialRecovery>> {
        TODO("Not yet implemented")
    }

    override fun getAllKeysForBackUp(): Flow<List<BackupV0_0_1.Key>> {
        TODO("Not yet implemented")
    }

    override suspend fun start(context: Any?) {
        TODO("Not yet implemented")
    }

    override fun getAllPrivateKeys(): Flow<List<StorablePrivateKey?>> {
        TODO("Not yet implemented")
    }
}
