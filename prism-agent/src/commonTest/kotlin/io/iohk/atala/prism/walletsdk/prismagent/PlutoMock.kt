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
    var storedPrismDID: List<DID> = emptyList()
    var storedPeerDID: List<DID> = emptyList()
    var getAllPrismDIDsReturn: Flow<List<PrismDIDInfo>> = flow { emit(emptyList()) }
    var getDIDInfoByDIDReturn: Flow<PrismDIDInfo?> = flow { emit(null) }
    var getPrismDIDKeyPathIndexReturn: Flow<Int?> = flow { emit(null) }
    var getPrismLastKeyPairIndexReturn: Flow<Int> = flow { emit(0) }
    var getAllPeerDIDsReturn: Flow<List<PeerDID>> = flow { emit(emptyList()) }
    var getPeerDIDPrivateKeysReturn: Flow<List<PrivateKey>?> = flow { emit(emptyList()) }
    var getAllDidPairsReturn: Flow<List<DIDPair>> = flow { emit(emptyList()) }
    var getPairReturn: Flow<DIDPair?> = flow { emit(null) }
    var getAllMessagesReturn: Flow<List<Message>> = flow { emit(emptyList()) }
    var getAllMessagesSentReturn: Flow<List<Message>> = flow { emit(emptyList()) }
    var getAllMessagesSentToReturn: Flow<List<Message>> = flow { emit(emptyList()) }
    var getAllMessagesReceivedReturn: Flow<List<Message>> = flow { emit(emptyList()) }
    var getAllMessagesReceivedFromReturn: Flow<List<Message>> = flow { emit(emptyList()) }
    var getAllMessagesOfTypeReturn: Flow<List<Message>> = flow { emit(emptyList()) }
    var getMessageReturn: Flow<Message?> = flow { emit(null) }
    var getAllMediatorsReturn: Flow<List<MediatorDID>> = flow { emit(emptyList()) }
    var getAllCredentialsReturn: Flow<List<VerifiableCredential>> = flow { emit(emptyList()) }

    override fun storePrismDID(
        did: DID,
        keyPathIndex: Int,
        alias: String?
    ) {
        storedPrismDID += did
    }

    override fun storePeerDID(did: DID, privateKeys: List<PrivateKey>) {
        storedPeerDID += did
    }

    override fun storeDIDPair(host: DID, receiver: DID, name: String) {}

    override fun storeMessage(message: Message) {}

    override fun storeMessages(messages: List<Message>) {}

    override fun storeMediator(mediator: DID, host: DID, routing: DID) {}

    override fun storeCredential(credential: VerifiableCredential) {}

    override fun getAllPrismDIDs(): Flow<List<PrismDIDInfo>> = getAllPrismDIDsReturn

    override fun getDIDInfoByDID(did: DID): Flow<PrismDIDInfo?> = getDIDInfoByDIDReturn

    override fun getDIDInfoByAlias(alias: String): Flow<List<PrismDIDInfo>> = getAllPrismDIDsReturn

    override fun getPrismDIDKeyPathIndex(did: DID): Flow<Int?> = getPrismDIDKeyPathIndexReturn

    override fun getPrismLastKeyPathIndex(): Flow<Int> = getPrismLastKeyPairIndexReturn

    override fun getAllPeerDIDs(): Flow<List<PeerDID>> = getAllPeerDIDsReturn

    override fun getDIDPrivateKeysByDID(did: DID): Flow<List<PrivateKey>?> = getPeerDIDPrivateKeysReturn

    override fun getAllDidPairs(): Flow<List<DIDPair>> = getAllDidPairsReturn

    override fun getPair(did: DID): Flow<List<DIDPair>?> = getAllDidPairsReturn

    override fun getPair(name: String): Flow<DIDPair?> = getPairReturn

    override fun getAllMessages(): Flow<List<Message>> = getAllMessagesReturn

    override fun getAllMessages(did: DID): Flow<List<Message>> = getAllMessagesReturn

    override fun getAllMessagesSent(): Flow<List<Message>> = getAllMessagesSentReturn

    override fun getAllMessagesReceived(): Flow<List<Message>> = getAllMessagesReceivedReturn

    override fun getAllMessagesSentTo(did: DID): Flow<List<Message>> = getAllMessagesSentToReturn

    override fun getAllMessagesReceivedFrom(did: DID): Flow<List<Message>> = getAllMessagesReceivedFromReturn

    override fun getAllMessagesOfType(type: String, relatedWithDID: DID?): Flow<List<Message>> = getAllMessagesOfTypeReturn

    override fun getAllMessages(from: DID, to: DID): Flow<List<Message>> = getAllMessagesReturn

    override fun getMessage(id: String): Flow<Message?> = getMessageReturn

    override fun getAllMediators(): Flow<List<MediatorDID>> = getAllMediatorsReturn

    override fun getAllCredentials(): Flow<List<VerifiableCredential>> = getAllCredentialsReturn
}
