package org.hyperledger.identus.walletsdk.domain.buildingblocks

import kotlinx.coroutines.flow.Flow
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

/**
 * The `Pluto` interface defines the contract for storing and retrieving various data related to Atala PRISM architecture.
 */
interface Pluto {

    /**
     * Stores the Prism DID, key path index, alias, and private keys.
     *
     * @param did The Prism DID to store.
     * @param keyPathIndex The key path index.
     * @param alias The optional alias for the Prism DID.
     * @param privateKeys The list of private keys to store.
     */
    fun storePrismDIDAndPrivateKeys(
        did: DID,
        keyPathIndex: Int?,
        alias: String?,
        privateKeys: List<StorableKey>
    )

    /**
     * Stores the PeerDID in the system.
     *
     * @param did The PeerDID to store.
     */
    fun storePeerDID(did: DID)

    /**
     * Stores a pair of Distributed Identifier (DID) and a receiver DID with a given name.
     *
     * @param host The host DID to store.
     * @param receiver The receiver DID to store.
     * @param name The name of the stored pair.
     */
    fun storeDIDPair(host: DID, receiver: DID, name: String)

    /**
     * Stores a message in the system.
     *
     * @param message The message to store.
     */
    fun storeMessage(message: Message)

    /**
     * Stores a list of messages in the system.
     *
     * @param messages The list of messages to store.
     */
    fun storeMessages(messages: List<Message>)

    /**
     * Stores the private key along with additional information.
     *
     * @param storableKey The private key to store. Must implement the [StorableKey] interface.
     * @param did The DID associated with the private key.
     * @param keyPathIndex The key path index.
     * @param metaId The optional metadata ID.
     */
    fun storePrivateKeys(storableKey: StorableKey, did: DID, keyPathIndex: Int? = null, metaId: String? = null)

    /**
     * Stores a private key with its recovery ID.
     *
     * @param sorableKey The private key to store. Must implement the [StorableKey] interface.
     * @param recoveryId String that identifies the type of key used on recovery process.
     */
    fun storePrivate(sorableKey: StorableKey, recoveryId: String)

    /**
     * Stores a mediator in the system.
     *
     * @param mediator The mediator DID to store.
     * @param host The host DID associated with the mediator.
     * @param routing The routing DID for the mediator.
     */
    fun storeMediator(mediator: DID, host: DID, routing: DID)

    /**
     * Stores a credential in the system.
     *
     * @param storableCredential The credential to store. It must implement the [StorableCredential] interface.
     */
    fun storeCredential(storableCredential: StorableCredential)

    /**
     * Stores a link secret in the system.
     *
     * @param linkSecret The link secret to store.
     */
    fun storeLinkSecret(linkSecret: String)

    /**
     * Stores the metadata associated with a credential request.
     *
     * @param name the unique name used to retrieve the stored metadata.
     * @param linkSecretName The link secret name as String.
     * @param json The json string.
     */
    fun storeCredentialMetadata(name: String, linkSecretName: String, json: String)

    /**
     * Retrieves all PrismDIDs and their associated information.
     *
     * @return A flow of lists of [PrismDIDInfo] objects representing the PrismDIDs and their information.
     */
    fun getAllPrismDIDs(): Flow<List<PrismDIDInfo>>

    /**
     * Retrieves the [PrismDIDInfo] associated with a given [DID].
     *
     * @param did The [DID] for which to retrieve the [PrismDIDInfo].
     * @return A [Flow] that emits a nullable [PrismDIDInfo] object representing the [PrismDIDInfo] associated
     *         with the specified [DID]. If no [PrismDIDInfo] is found, null is emitted.
     */
    fun getDIDInfoByDID(did: DID): Flow<PrismDIDInfo?>

    /**
     * Retrieves the [PrismDIDInfo] objects associated with a given alias.
     *
     * @param alias The alias for which to retrieve the [PrismDIDInfo] objects.
     * @return A [Flow] that emits a list of [PrismDIDInfo] objects representing the
     *         [PrismDIDInfo] associated with the specified alias.
     */
    fun getDIDInfoByAlias(alias: String): Flow<List<PrismDIDInfo>>

    /**
     * Retrieves the key path index associated with a given Prism DID.
     *
     * @param did The Prism DID for which to retrieve the key path index.
     * @return A [Flow] that emits a nullable [Int] representing the key path index associated with the specified Prism DID.
     *         If no key path index is found, null is emitted.
     */
    fun getPrismDIDKeyPathIndex(did: DID): Flow<Int?>

    /**
     * Retrieves the last key path index associated with the Prism DID.
     *
     * @return A [Flow] that emits an [Int] representing the last key path index associated with the Prism DID.
     */
    fun getPrismLastKeyPathIndex(): Flow<Int>

    /**
     * Retrieves all PeerDIDs.
     *
     * @return A flow of lists of PeerDIDs.
     */
    fun getAllPeerDIDs(): Flow<List<PeerDID>>

    /**
     * Retrieves all DIDs.
     *
     * @return A flow of lists of DIDs.
     */
    fun getAllDIDs(): Flow<List<DID>>

    /**
     * Retrieves a list of private keys associated with a given DID.
     *
     * @param did The DID for which to retrieve private keys.
     * @return A flow that emits a list of nullable [StorablePrivateKey] objects. In case a private key is not found, null is emitted.
     */
    fun getDIDPrivateKeysByDID(did: DID): Flow<List<StorablePrivateKey?>>

    /**
     * Retrieves the private key associated with a given ID.
     *
     * @param id The ID of the private key.
     * @return A [Flow] that emits the private key as a nullable [StorablePrivateKey] object. If no private key is found,
     * null is emitted.
     */
    fun getDIDPrivateKeyByID(id: String): Flow<StorablePrivateKey?>

    /**
     * Retrieves all the pairs of DIDs stored in the system.
     *
     * @return a [Flow] emitting a list of [DIDPair] objects representing the pairs of DIDs.
     */
    fun getAllDidPairs(): Flow<List<DIDPair>>

    /**
     * Retrieves a DIDPair object using the provided DID.
     *
     * @param did The DID to search for.
     * @return A Flow of DIDPair objects. If a match is found, the flow emits the matching DIDPair.
     * If no match is found, the flow emits null.
     */
    fun getPairByDID(did: DID): Flow<DIDPair?>

    /**
     * Retrieve a [DIDPair] from a flow by its name.
     *
     * @param name The name of the [DIDPair] to retrieve.
     * @return A [Flow] emitting the [DIDPair] object that matches the given name,
     *         or `null` if no matching [DIDPair] is found.
     */
    fun getPairByName(name: String): Flow<DIDPair?>

    /**
     * Retrieves all the messages.
     *
     * @return a Flow of List of Message objects representing all the messages.
     */
    fun getAllMessages(): Flow<List<Message>>

    fun getAllMessagesByType(type: String): Flow<List<Message>>

    /**
     * Retrieves all messages based on the provided DID.
     *
     * @param did The DID (Direct Inward Dialing) to filter messages by.
     * @return A flow of list of messages.
     */
    fun getAllMessages(did: DID): Flow<List<Message>>

    /**
     * Retrieves all the messages that have been sent.
     *
     * @return A [Flow] of type [List] containing the sent messages.
     */
    fun getAllMessagesSent(): Flow<List<Message>>

    /**
     * Retrieves all messages received by the user.
     *
     * @return A [Flow] emitting a list of [Message] objects representing all the messages received.
     */
    fun getAllMessagesReceived(): Flow<List<Message>>

    /**
     * Retrieves all messages sent to the specified DID.
     *
     * @param did the destination DID to filter the messages by
     * @return a [Flow] of [List] of [Message] objects containing all the messages sent to the specified DID
     */
    fun getAllMessagesSentTo(did: DID): Flow<List<Message>>

    /**
     * Returns a Flow of lists of all messages received from the specified DID.
     *
     * @param did the DID (Decentralized Identifier) to get the received messages from
     * @return a Flow of lists of messages received from the specified DID
     */
    fun getAllMessagesReceivedFrom(did: DID): Flow<List<Message>>

    /**
     * Retrieves all messages of a specific type that are related to a given DID.
     *
     * @param type The type of the messages to retrieve.
     * @param relatedWithDID The optional DID to which the messages are related.
     * @return A [Flow] emitting a list of [Message] objects that match the given type and are related to the specified DID.
     */
    fun getAllMessagesOfType(type: String, relatedWithDID: DID?): Flow<List<Message>>

    /**
     * Retrieves all messages exchanged between the specified 'from' and 'to' DIDs.
     *
     * @param from the sender DID
     * @param to the receiver DID
     * @return a Flow emitting a list of messages exchanged between the 'from' and 'to' DIDs
     */
    fun getAllMessages(from: DID, to: DID): Flow<List<Message>>

    /**
     * Retrieves the message with the specified ID.
     *
     * @param id The unique ID of the message.
     * @return A [Flow] that emits the message with the specified ID, or null if no such message exists.
     *         The [Flow] completes when the message is successfully retrieved, or when an error occurs.
     */
    fun getMessage(id: String): Flow<Message?>

    /**
     * Retrieves the message with the specified thid.
     *
     * @param thid The unique ID of a request.
     * @param piuri The type of message.
     * @return A [Flow] that emits the message with the specified ID, or null if no such message exists.
     *         The [Flow] completes when the message is successfully retrieved, or when an error occurs.
     */
    fun getMessageByThidAndPiuri(thid: String, piuri: String): Flow<Message?>

    /**
     * Returns a Flow of lists of [Mediator] objects representing all the available mediators.
     *
     * @return a Flow of lists of [Mediator] objects.
     */
    fun getAllMediators(): Flow<List<Mediator>>

    /**
     * Retrieves all credentials for credential recovery.
     *
     * @return A flow of a list of [CredentialRecovery] objects representing the credentials for recovery.
     */
    fun getAllCredentials(): Flow<List<CredentialRecovery>>

    /**
     * Inserts an available claim for a specific credential.
     *
     * @param credentialId The ID of the credential.
     * @param claim The claim to insert.
     */
    fun insertAvailableClaim(credentialId: String, claim: String)

    /**
     * Inserts the available claims for a given credential ID.
     *
     * @param credentialId the ID of the credential
     * @param claims an array of available claims to be inserted
     */
    fun insertAvailableClaims(credentialId: String, claims: Array<String>)

    /**
     * Retrieves the available claims for a given credential ID.
     *
     * @param credentialId The ID of the credential.
     * @return A flow that emits an array of AvailableClaims.
     */
    fun getAvailableClaimsByCredentialId(credentialId: String): Flow<Array<AvailableClaims>>

    /**
     * Retrieves the available claims for a given claim.
     *
     * @param claim The claim for which the available claims are to be retrieved.
     * @return A flow of arrays of AvailableClaims representing the available claims for the given claim.
     */
    fun getAvailableClaimsByClaim(claim: String): Flow<Array<AvailableClaims>>

    /**
     * Retrieves the secret link associated with the current instance.
     *
     * @return A [Flow] emitting the secret link as a nullable [String].
     */
    fun getLinkSecret(): Flow<String?>

    /**
     * Retrieves the metadata associated with a credential request.
     *
     * @param linkSecretName The name of the link secret used for the credential request.
     * @return A [Flow] emitting the [CredentialRequestMeta] object for the specified link secret name,
     * or null if no metadata is found.
     */
    fun getCredentialMetadata(linkSecretName: String): Flow<CredentialRequestMeta?>

    /**
     * Revokes an existing credential using the credential ID.
     *
     * @param credentialId The ID of the credential to be revoked
     */
    fun revokeCredential(credentialId: String)

    /**
     * Provides a flow to listen for revoked credentials.
     */
    fun observeRevokedCredentials(): Flow<List<CredentialRecovery>>

    fun getAllKeysForBackUp(): Flow<List<BackupV0_0_1.Key>>

    /**
     * Retrieves a list of all private keys.
     *
     * @return A flow that emits a list of nullable [StorablePrivateKey] objects. In case a private key is not found, null is emitted.
     */
    fun getAllPrivateKeys(): Flow<List<StorablePrivateKey?>>

    suspend fun start(context: Any? = null)
}
