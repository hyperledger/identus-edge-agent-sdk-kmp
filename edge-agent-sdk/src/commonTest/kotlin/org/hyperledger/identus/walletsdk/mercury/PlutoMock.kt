package org.hyperledger.identus.walletsdk.mercury

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.hyperledger.identus.apollo.base64.base64UrlEncoded
import org.hyperledger.identus.walletsdk.domain.buildingblocks.Pluto
import org.hyperledger.identus.walletsdk.domain.models.DID
import org.hyperledger.identus.walletsdk.domain.models.DIDPair
import org.hyperledger.identus.walletsdk.domain.models.Mediator
import org.hyperledger.identus.walletsdk.domain.models.Message
import org.hyperledger.identus.walletsdk.domain.models.PeerDID
import org.hyperledger.identus.walletsdk.domain.models.PrismDIDInfo
import org.hyperledger.identus.walletsdk.domain.models.StorableCredential
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.PrivateKey
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.StorableKey
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.StorablePrivateKey
import org.hyperledger.identus.walletsdk.pluto.CredentialRecovery
import org.hyperledger.identus.walletsdk.pluto.data.AvailableClaims
import org.hyperledger.identus.walletsdk.pluto.models.backup.BackupV0_0_1
import org.hyperledger.identus.walletsdk.pollux.models.CredentialRequestMeta
import java.util.*

class PlutoMock : Pluto {
    var privateKeys = mutableListOf<PrivateKey>()

    override fun getDIDPrivateKeyByID(id: String): Flow<StorablePrivateKey?> {
        val pk = privateKeys.find {
            it.getCurve() == id
        }
        val storablePrivateKeys = pk?.let { privateKey ->
            StorablePrivateKey(
                id = UUID.randomUUID().toString(),
                restorationIdentifier = "secp256k1+priv",
                data = privateKey.raw.base64UrlEncoded,
                keyPathIndex = 0
            )
        }
        return flow { emit(storablePrivateKeys) }
    }

    override fun storePrismDIDAndPrivateKeys(
        did: DID,
        keyPathIndex: Int?,
        alias: String?,
        privateKeys: List<StorableKey>
    ) {
        TODO("Not yet implemented")
    }

    override fun storePeerDID(did: DID) {
        TODO("Not yet implemented")
    }

    override fun storeDIDPair(host: DID, receiver: DID, name: String) {
        TODO("Not yet implemented")
    }

    override fun storeMessage(message: Message) {
        TODO("Not yet implemented")
    }

    override fun storeMessages(messages: List<Message>) {
        TODO("Not yet implemented")
    }

    override fun storePrivateKeys(storableKey: StorableKey, did: DID, keyPathIndex: Int?, metaId: String?) {
        TODO("Not yet implemented")
    }

    override fun storePrivate(sorableKey: StorableKey, recoveryId: String) {
        TODO("Not yet implemented")
    }

    override fun storeMediator(mediator: DID, host: DID, routing: DID) {
        TODO("Not yet implemented")
    }

    override fun storeCredential(storableCredential: StorableCredential) {
        TODO("Not yet implemented")
    }

    override fun getAllPrismDIDs(): Flow<List<PrismDIDInfo>> {
        TODO("Not yet implemented")
    }

    override fun getDIDInfoByDID(did: DID): Flow<PrismDIDInfo?> {
        TODO("Not yet implemented")
    }

    override fun getDIDInfoByAlias(alias: String): Flow<List<PrismDIDInfo>> {
        TODO("Not yet implemented")
    }

    override fun getPrismDIDKeyPathIndex(did: DID): Flow<Int?> {
        TODO("Not yet implemented")
    }

    override fun getPrismLastKeyPathIndex(): Flow<Int> {
        TODO("Not yet implemented")
    }

    override fun getAllPeerDIDs(): Flow<List<PeerDID>> {
        TODO("Not yet implemented")
    }

    override fun getAllDIDs(): Flow<List<DID>> {
        TODO("Not yet implemented")
    }

    override fun getDIDPrivateKeysByDID(did: DID): Flow<List<StorablePrivateKey?>> {
        TODO("Not yet implemented")
    }

    override fun getAllDidPairs(): Flow<List<DIDPair>> {
        TODO("Not yet implemented")
    }

    override fun getPairByDID(did: DID): Flow<DIDPair?> {
        TODO("Not yet implemented")
    }

    override fun getPairByName(name: String): Flow<DIDPair?> {
        TODO("Not yet implemented")
    }

    override fun getAllMessages(): Flow<List<Message>> {
        TODO("Not yet implemented")
    }

    override fun getAllMessages(did: DID): Flow<List<Message>> {
        TODO("Not yet implemented")
    }

    override fun getAllMessagesSent(): Flow<List<Message>> {
        TODO("Not yet implemented")
    }

    override fun getAllMessagesReceived(): Flow<List<Message>> {
        TODO("Not yet implemented")
    }

    override fun getAllMessagesSentTo(did: DID): Flow<List<Message>> {
        TODO("Not yet implemented")
    }

    override fun getAllMessagesReceivedFrom(did: DID): Flow<List<Message>> {
        TODO("Not yet implemented")
    }

    override fun getAllMessagesOfType(type: String, relatedWithDID: DID?): Flow<List<Message>> {
        TODO("Not yet implemented")
    }

    override fun getAllMessages(from: DID, to: DID): Flow<List<Message>> {
        TODO("Not yet implemented")
    }

    override fun getAllMessagesByType(type: String): Flow<List<Message>> {
        TODO("Not yet implemented")
    }

    override fun getMessage(id: String): Flow<Message?> {
        TODO("Not yet implemented")
    }

    override fun getMessageByThidAndPiuri(thid: String, piuri: String): Flow<Message?> {
        TODO("Not yet implemented")
    }

    override fun getAllMediators(): Flow<List<Mediator>> {
        TODO("Not yet implemented")
    }

    override fun getAllCredentials(): Flow<List<CredentialRecovery>> {
        TODO("Not yet implemented")
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
        TODO("Not yet implemented")
    }

    override fun storeCredentialMetadata(name: String, linkSecretName: String, json: String) {
        TODO("Not yet implemented")
    }

    override fun getLinkSecret(): Flow<String> {
        TODO("Not yet implemented")
    }

    override fun getCredentialMetadata(linkSecretName: String): Flow<CredentialRequestMeta?> {
        TODO("Not yet implemented")
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
