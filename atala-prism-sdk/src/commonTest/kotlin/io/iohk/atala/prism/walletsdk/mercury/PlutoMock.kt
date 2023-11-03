package io.iohk.atala.prism.walletsdk.mercury

/* ktlint-disable import-ordering */
import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Pluto
import io.iohk.atala.prism.walletsdk.domain.models.DID
import io.iohk.atala.prism.walletsdk.domain.models.DIDPair
import io.iohk.atala.prism.walletsdk.domain.models.Mediator
import io.iohk.atala.prism.walletsdk.domain.models.Message
import io.iohk.atala.prism.walletsdk.domain.models.PeerDID
import io.iohk.atala.prism.walletsdk.domain.models.PrismDIDInfo
import io.iohk.atala.prism.walletsdk.domain.models.StorableCredential
import io.iohk.atala.prism.walletsdk.pollux.models.CredentialRequestMeta
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.PrivateKey
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.StorableKey
import io.iohk.atala.prism.walletsdk.pluto.CredentialRecovery
import ioiohkatalaprismwalletsdkpluto.data.AvailableClaims
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
/* ktlint-disable import-ordering */

class PlutoMock : Pluto {
    var privateKeys = mutableListOf<PrivateKey>()

    override fun getDIDPrivateKeyByID(id: String): Flow<PrivateKey?> {
        val pk = privateKeys.find {
            it.getCurve() == id
        }

        return flow { emit(pk) }
    }

    override fun storePrismDIDAndPrivateKeys(
        did: DID,
        keyPathIndex: Int,
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

    override fun storePrivateKeys(privateKey: StorableKey, did: DID, keyPathIndex: Int, metaId: String?) {
        TODO("Not yet implemented")
    }

    override fun storeMediator(mediator: DID, host: DID, routing: DID) {
        TODO("Not yet implemented")
    }

    override fun storeCredential(credential: StorableCredential) {
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

    override fun getDIDPrivateKeysByDID(did: DID): Flow<List<PrivateKey?>> {
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

    override fun getMessage(id: String): Flow<Message?> {
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

    override fun storeCredentialMetadata(metadata: CredentialRequestMeta) {
        TODO("Not yet implemented")
    }

    override fun getLinkSecret(): Flow<String> {
        TODO("Not yet implemented")
    }

    override fun getCredentialMetadata(linkSecretName: String): Flow<CredentialRequestMeta?> {
        TODO("Not yet implemented")
    }
}
