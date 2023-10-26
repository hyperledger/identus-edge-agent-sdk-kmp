package io.iohk.atala.prism.walletsdk.domain.buildingblocks

import io.iohk.atala.prism.walletsdk.domain.models.DID
import io.iohk.atala.prism.walletsdk.domain.models.DIDPair
import io.iohk.atala.prism.walletsdk.domain.models.Mediator
import io.iohk.atala.prism.walletsdk.domain.models.Message
import io.iohk.atala.prism.walletsdk.domain.models.PeerDID
import io.iohk.atala.prism.walletsdk.domain.models.PrismDIDInfo
import io.iohk.atala.prism.walletsdk.domain.models.StorableCredential
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.PrivateKey
import io.iohk.atala.prism.walletsdk.pluto.CredentialRecovery
import io.iohk.atala.prism.walletsdk.pollux.models.CredentialRequestMeta
import ioiohkatalaprismwalletsdkpluto.data.AvailableClaims
import kotlinx.coroutines.flow.Flow

interface Pluto {

    fun storePrismDIDAndPrivateKeys(
        did: DID,
        keyPathIndex: Int,
        alias: String?,
        privateKeys: List<PrivateKey>
    )

    fun storePeerDID(did: DID)

    fun storeDIDPair(host: DID, receiver: DID, name: String)

    fun storeMessage(message: Message)

    fun storeMessages(messages: List<Message>)

    fun storePrivateKeys(privateKey: PrivateKey, did: DID, keyPathIndex: Int, metaId: String? = null)

    fun storeMediator(mediator: DID, host: DID, routing: DID)

    fun storeCredential(credential: StorableCredential)

    fun storeLinkSecret(linkSecret: String)

    fun storeCredentialMetadata(metadata: CredentialRequestMeta)

    fun getAllPrismDIDs(): Flow<List<PrismDIDInfo>>

    fun getDIDInfoByDID(did: DID): Flow<PrismDIDInfo?>

    fun getDIDInfoByAlias(alias: String): Flow<List<PrismDIDInfo>>

    fun getPrismDIDKeyPathIndex(did: DID): Flow<Int?>

    fun getPrismLastKeyPathIndex(): Flow<Int>

    fun getAllPeerDIDs(): Flow<List<PeerDID>>

    fun getDIDPrivateKeysByDID(did: DID): Flow<List<PrivateKey?>>

    fun getDIDPrivateKeyByID(id: String): Flow<PrivateKey?>

    fun getAllDidPairs(): Flow<List<DIDPair>>

    fun getPairByDID(did: DID): Flow<DIDPair?>

    fun getPairByName(name: String): Flow<DIDPair?>

    // @JsName("getAllMessages")
    fun getAllMessages(): Flow<List<Message>>

    // @JsName("getAllMessagesByDID")
    fun getAllMessages(did: DID): Flow<List<Message>>

    fun getAllMessagesSent(): Flow<List<Message>>

    fun getAllMessagesReceived(): Flow<List<Message>>

    fun getAllMessagesSentTo(did: DID): Flow<List<Message>>

    fun getAllMessagesReceivedFrom(did: DID): Flow<List<Message>>

    fun getAllMessagesOfType(type: String, relatedWithDID: DID?): Flow<List<Message>>

    // @JsName("getAllMessagesByFromToDID")
    fun getAllMessages(from: DID, to: DID): Flow<List<Message>>

    fun getMessage(id: String): Flow<Message?>

    fun getAllMediators(): Flow<List<Mediator>>

    fun getAllCredentials(): Flow<List<CredentialRecovery>>

    fun insertAvailableClaim(credentialId: String, claim: String)

    fun insertAvailableClaims(credentialId: String, claims: Array<String>)

    fun getAvailableClaimsByCredentialId(credentialId: String): Flow<Array<AvailableClaims>>

    fun getAvailableClaimsByClaim(claim: String): Flow<Array<AvailableClaims>>

    fun getLinkSecret(): Flow<String?>

    fun getCredentialMetadata(linkSecretName: String): Flow<CredentialRequestMeta?>
}
