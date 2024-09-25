@file:Suppress("ktlint:standard:import-ordering")

package org.hyperledger.identus.walletsdk.pluto

import app.cash.sqldelight.ColumnAdapter
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.db.AfterVersion
import java.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.hyperledger.identus.apollo.base64.base64UrlDecodedBytes
import org.hyperledger.identus.apollo.base64.base64UrlEncoded
import org.hyperledger.identus.walletsdk.SdkPlutoDb
import org.hyperledger.identus.walletsdk.apollo.utils.Ed25519PrivateKey
import org.hyperledger.identus.walletsdk.apollo.utils.Secp256k1PrivateKey
import org.hyperledger.identus.walletsdk.apollo.utils.X25519PrivateKey
import org.hyperledger.identus.walletsdk.domain.buildingblocks.Pluto
import org.hyperledger.identus.walletsdk.domain.models.DID
import org.hyperledger.identus.walletsdk.domain.models.DIDPair
import org.hyperledger.identus.walletsdk.domain.models.Mediator
import org.hyperledger.identus.walletsdk.domain.models.Message
import org.hyperledger.identus.walletsdk.domain.models.PeerDID
import org.hyperledger.identus.walletsdk.domain.models.PlutoError
import org.hyperledger.identus.walletsdk.domain.models.PrismDIDInfo
import org.hyperledger.identus.walletsdk.domain.models.StorableCredential
import org.hyperledger.identus.walletsdk.domain.models.UnknownError
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.JWK
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.PrivateKey
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.StorableKey
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.StorablePrivateKey
import org.hyperledger.identus.walletsdk.logger.Logger
import org.hyperledger.identus.walletsdk.logger.LoggerImpl
import org.hyperledger.identus.walletsdk.logger.LogComponent
import org.hyperledger.identus.walletsdk.pluto.models.backup.BackupV0_0_1
import org.hyperledger.identus.walletsdk.pluto.data.DbConnection
import org.hyperledger.identus.walletsdk.pluto.data.isConnected
import org.hyperledger.identus.walletsdk.pluto.models.DidKeyLink
import org.hyperledger.identus.walletsdk.pollux.models.CredentialRequestMeta
import org.hyperledger.identus.walletsdk.pluto.data.AvailableClaims as AvailableClaimsDB
import org.hyperledger.identus.walletsdk.pluto.data.DID as DIDDB
import org.hyperledger.identus.walletsdk.pluto.data.DIDPair as DIDPairDB
import org.hyperledger.identus.walletsdk.pluto.data.LinkSecret as LinkSecretDB
import org.hyperledger.identus.walletsdk.pluto.data.Mediator as MediatorDB
import org.hyperledger.identus.walletsdk.pluto.data.Message as MessageDB
import org.hyperledger.identus.walletsdk.pluto.data.PrivateKey as PrivateKeyDB
import org.hyperledger.identus.walletsdk.pluto.data.StorableCredential as StorableCredentialDB

/**
 * `PlutoImpl` is a class that provides an implementation of the Pluto interface for interacting with the database.
 *
 * @property db The instance of `SdkPlutoDb` representing the connection to the database.
 * @property isConnected A flag to indicate whether the database connection is established or not.
 */
class PlutoImpl(
    private val connection: DbConnection,
    private val logger: Logger = LoggerImpl(LogComponent.PLUTO)
) : Pluto {
    private var db: SdkPlutoDb? = null

    init {
        this.connection.driver?.let { driver ->
            SdkPlutoDb.Schema.migrate(
                driver,
                1,
                SdkPlutoDb.Schema.version,
                AfterVersion(1) {
                    it.execute(null, "ALTER TABLE CredentialMetadata DROB COLUMN nonce;", 0)
                    it.execute(
                        null,
                        "ALTER TABLE CredentialMetadata DROB COLUMN linkSecretBlindingData;",
                        0
                    )
                    it.execute(null, "ALTER TABLE CredentialMetadata ADD COLUMN json TEXT;", 0)
                }
            )
        } ?: {
            throw PlutoError.DatabaseConnectionError("Database migration failed to: ${SdkPlutoDb.Schema.version}")
        }
    }

    /**
     * isConnected indicates whether the connection to the database is currently established or not.
     *
     * @return true if the connection is established, false otherwise
     */
    val isConnected: Boolean
        get() {
            return this.connection.driver?.isConnected ?: false
        }

    /**
     * Starts the database service.
     *
     * @param context The context data required for establishing the connection. This can be null in some cases.
     * @throws PlutoError.DatabaseServiceAlreadyRunning if the database service is already running.
     */
    @Throws(PlutoError.DatabaseServiceAlreadyRunning::class)
    override suspend fun start(context: Any?) {
        if (this.db != null) {
            throw PlutoError.DatabaseServiceAlreadyRunning()
        }
        this.db = SdkPlutoDb(
            this.connection.connectDb(context),
            org.hyperledger.identus.walletsdk.pluto.data.Message.Adapter(
                isReceivedAdapter = object : ColumnAdapter<Int, Long> {
                    override fun decode(databaseValue: Long): Int {
                        return databaseValue.toInt()
                    }

                    override fun encode(value: Int): Long {
                        return value.toLong()
                    }
                }
            ),
            org.hyperledger.identus.walletsdk.pluto.data.PrivateKey.Adapter(
                keyPathIndexAdapter = object : ColumnAdapter<Int, Long> {
                    override fun decode(databaseValue: Long): Int {
                        return databaseValue.toInt()
                    }

                    override fun encode(value: Int): Long {
                        return value.toLong()
                    }
                }
            ),
            org.hyperledger.identus.walletsdk.pluto.data.StorableCredential.Adapter(
                revokedAdapter = object : ColumnAdapter<Int, Long> {
                    override fun decode(databaseValue: Long): Int {
                        return databaseValue.toInt()
                    }

                    override fun encode(value: Int): Long {
                        return value.toLong()
                    }
                }
            )
        )
    }

    /**
     * Closes the connection to the database.
     *
     * @throws PlutoError.DatabaseConnectionError if there is an error with the database connection
     */
    @Throws(PlutoError.DatabaseConnectionError::class)
    public fun stop() {
        val driver = this.connection.driver ?: throw PlutoError.DatabaseConnectionError()
        this.db = null
        driver.close()
    }

    /**
     * Retrieves an instance of the SdkPlutoDb object.
     * Throws DatabaseConnectionError if the database connection is not established.
     *
     * @throws PlutoError.DatabaseConnectionError if the database connection is not established
     * @return the SdkPlutoDb instance
     */
    @Throws(PlutoError.DatabaseConnectionError::class)
    private fun getInstance(): SdkPlutoDb {
        return this.db ?: throw PlutoError.DatabaseConnectionError()
    }

    /**
     * Stores the Prism DID, key path index, alias, and private keys.
     *
     * @param did The Prism DID to store.
     * @param keyPathIndex The key path index.
     * @param alias The optional alias for the Prism DID.
     * @param privateKeys The list of private keys to store.
     */
    override fun storePrismDIDAndPrivateKeys(
        did: DID,
        keyPathIndex: Int?,
        alias: String?,
        privateKeys: List<StorableKey>
    ) {
        getInstance().dIDQueries.insert(
            DIDDB(
                did.toString(),
                did.method,
                did.methodId,
                did.schema,
                alias
            )
        )
        privateKeys.forEach { privateKey ->
            storePrivateKeys(privateKey, did, keyPathIndex)
        }
    }

    /**
     * Stores the PeerDID in the system.
     *
     * @param did The PeerDID to store.
     */
    override fun storePeerDID(did: DID) {
        getInstance().dIDQueries.insert(
            DIDDB(
                did.toString(),
                did.method,
                did.methodId,
                did.schema,
                did.alias
            )
        )
    }

    /**
     * Stores a pair of Distributed Identifier (DID) and a receiver DID with a given name.
     *
     * @param host The host DID to store.
     * @param receiver The receiver DID to store.
     * @param name The name of the stored pair.
     */
    override fun storeDIDPair(host: DID, receiver: DID, name: String) {
        getInstance().dIDPairQueries.insert(
            DIDPairDB(
                "$host$receiver",
                name,
                host.toString(),
                receiver.toString()
            )
        )
    }

    /**
     * Stores a message in the system.
     *
     * @param message The message to store.
     */
    override fun storeMessage(message: Message) {
        getInstance().messageQueries.insert(
            MessageDB(
                message.id,
                message.createdTime,
                message.toJsonString(),
                message.from.toString(),
                message.thid,
                message.to.toString(),
                message.piuri,
                message.direction.value
            )
        )
    }

    /**
     * Stores the private key along with additional information.
     *
     * @param storableKey The private key to store. Must implement the [StorableKey] interface.
     * @param did The DID associated with the private key.
     * @param keyPathIndex The key path index.
     * @param metaId The optional metadata ID.
     */
    override fun storePrivateKeys(
        storableKey: StorableKey,
        did: DID,
        keyPathIndex: Int?,
        metaId: String?
    ) {
        val id = metaId ?: did.toString()
        val list = getInstance().privateKeyQueries.fetchPrivateKeyByID(id).executeAsList()
        if (list.isEmpty()) {
            getInstance().privateKeyQueries.insert(
                PrivateKeyDB(
                    id,
                    storableKey.restorationIdentifier,
                    storableKey.storableData.base64UrlEncoded,
                    keyPathIndex
                )
            )
            getInstance().dIDKeyLinkQueries.insert(
                didId = did.toString(),
                keyId = id,
                alias = did.alias
            )
        } else {
            // TODO: Implement Delete, Update
        }
    }

    override fun storePrivate(sorableKey: StorableKey, recoveryId: String) {
        getInstance().privateKeyQueries.insert(
            PrivateKeyDB(
                UUID.randomUUID().toString(),
                recoveryId,
                sorableKey.storableData.base64UrlEncoded,
                null
            )
        )
    }

    /**
     * Stores a list of messages in the system.
     *
     * @param messages The list of messages to store.
     */
    override fun storeMessages(messages: List<Message>) {
        messages.map { message ->
            storeMessage(message)
        }
    }

    /**
     * Stores a mediator in the system.
     *
     * @param mediator The mediator DID to store.
     * @param host The host DID associated with the mediator.
     * @param routing The routing DID for the mediator.
     */
    override fun storeMediator(mediator: DID, host: DID, routing: DID) {
        val instance = getInstance()
        instance.dIDQueries.insert(
            DIDDB(
                mediator.toString(),
                mediator.method,
                mediator.methodId,
                mediator.schema,
                null
            )
        )
        instance.dIDQueries.insert(
            DIDDB(
                routing.toString(),
                routing.method,
                routing.methodId,
                routing.schema,
                null
            )
        )
        instance.mediatorQueries.insert(
            MediatorDB(
                UUID.randomUUID().toString(),
                mediator.methodId,
                host.methodId,
                routing.methodId
            )
        )
    }

    /**
     * Stores a credential in the system.
     *
     * @param storableCredential The credential to store. It must implement the [StorableCredential] interface.
     */
    override fun storeCredential(storableCredential: StorableCredential) {
        getInstance().storableCredentialQueries.insert(
            StorableCredentialDB(
                id = storableCredential.id,
                recoveryId = storableCredential.recoveryId,
                credentialSchema = storableCredential.credentialSchema ?: "",
                credentialData = storableCredential.credentialData,
                issuer = storableCredential.issuer,
                subject = storableCredential.subject,
                credentialCreated = storableCredential.credentialCreated,
                credentialUpdated = storableCredential.credentialUpdated,
                validUntil = storableCredential.validUntil,
                revoked = if (storableCredential.revoked == true) 1 else 0
            )
        )
        getInstance().availableClaimsQueries.transaction {
            storableCredential.availableClaims.forEach { claim ->
                getInstance().availableClaimsQueries.insert(storableCredential.id, claim)
            }
        }
    }

    /**
     * Stores a link secret in the system.
     *
     * @param linkSecret The link secret to store.
     */
    override fun storeLinkSecret(linkSecret: String) {
        getInstance().linkSecretQueries
            .insert(LinkSecretDB(linkSecret))
    }

    /**
     * Stores the metadata associated with a credential request.
     *
     * @param name the unique name used to retrieve the stored metadata.
     * @param linkSecretName The link secret name as String.
     * @param json The json string.
     */
    override fun storeCredentialMetadata(name: String, linkSecretName: String, json: String) {
        getInstance().credentialMetadataQueries.insert(
            id = name,
            linkSecretName = linkSecretName,
            json = json
        )
    }

    /**
     * Retrieves all PrismDIDs and their associated information.
     *
     * @return A flow of lists of [PrismDIDInfo] objects representing the PrismDIDs and their information.
     */
    override fun getAllPrismDIDs(): Flow<List<PrismDIDInfo>> {
        return getInstance().dIDQueries
            .fetchAllPrismDID()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { list ->
                list.map {
                    PrismDIDInfo(DID(it.did), it.keyPathIndex, it.alias)
                }
            }
    }

    /**
     * Retrieves the [PrismDIDInfo] associated with a given [DID].
     *
     * @param did The [DID] for which to retrieve the [PrismDIDInfo].
     * @return A [Flow] that emits a nullable [PrismDIDInfo] object representing the [PrismDIDInfo] associated
     *         with the specified [DID]. If no [PrismDIDInfo] is found, null is emitted.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getDIDInfoByDID(did: DID): Flow<PrismDIDInfo?> {
        return getInstance().dIDQueries
            .fetchDIDInfoByDID(did.toString())
            .asFlow()
            .mapLatest {
                try {
                    val didInfo = it.executeAsOne()
                    PrismDIDInfo(DID(didInfo.did), didInfo.keyPathIndex, didInfo.alias)
                } catch (e: NullPointerException) {
                    null
                }
            }
    }

    /**
     * Retrieves the [PrismDIDInfo] objects associated with a given alias.
     *
     * @param alias The alias for which to retrieve the [PrismDIDInfo] objects.
     * @return A [Flow] that emits a list of [PrismDIDInfo] objects representing the
     *         [PrismDIDInfo] associated with the specified alias.
     */
    override fun getDIDInfoByAlias(alias: String): Flow<List<PrismDIDInfo>> {
        return getInstance().dIDQueries
            .fetchDIDInfoByAlias(alias)
            .asFlow()
            .map {
                it.executeAsList()
                    .map { didInfo ->
                        PrismDIDInfo(DID(didInfo.did), didInfo.keyPathIndex, didInfo.alias)
                    }
            }
    }

    /**
     * Retrieves a list of private keys associated with a given DID.
     *
     * @param did The DID for which to retrieve private keys.
     * @return A flow that emits a list of nullable [PrivateKey] objects. In case a private key is not found, null is emitted.
     */
    override fun getDIDPrivateKeysByDID(did: DID): Flow<List<StorablePrivateKey>> {
        return getInstance().privateKeyQueries
            .fetchPrivateKeyByDID(did.toString())
            .asFlow()
            .map {
                it.executeAsList()
                    .map { storableKey ->
                        StorablePrivateKey(
                            id = storableKey.id,
                            restorationIdentifier = storableKey.restorationIdentifier,
                            data = storableKey.data_,
                            keyPathIndex = storableKey.keyPathIndex
                        )
                    }
            }
    }

    /**
     * Retrieves the private key associated with a given ID.
     *
     * @param id The ID of the private key.
     * @return A [Flow] that emits the private key as a nullable [PrivateKey] object. If no private key is found,
     * null is emitted.
     */
    override fun getDIDPrivateKeyByID(id: String): Flow<StorablePrivateKey?> {
        return getInstance().privateKeyQueries
            .fetchPrivateKeyByID(id)
            .asFlow()
            .map {
                it.executeAsList().firstOrNull()?.let { storableKey ->
                    StorablePrivateKey(
                        id = storableKey.id,
                        restorationIdentifier = storableKey.restorationIdentifier,
                        data = storableKey.data_,
                        keyPathIndex = storableKey.keyPathIndex
                    )
                }
            }
    }

    /**
     * Retrieves the key path index associated with a given Prism DID.
     *
     * @param did The Prism DID for which to retrieve the key path index.
     * @return A [Flow] that emits a nullable [Int] representing the key path index associated with the specified Prism DID.
     *         If no key path index is found, null is emitted.
     */
    override fun getPrismDIDKeyPathIndex(did: DID): Flow<Int?> {
        return getInstance().privateKeyQueries.fetchKeyPathIndexByDID(did.toString())
            .asFlow()
            .map {
                try {
                    it.executeAsOne().keyPathIndex
                } catch (e: NullPointerException) {
                    null
                }
            }
    }

    /**
     * Retrieves the last key path index associated with the Prism DID.
     *
     * @return A [Flow] that emits an [Int] representing the last key path index associated with the Prism DID.
     */
    override fun getPrismLastKeyPathIndex(): Flow<Int> {
        return getInstance().privateKeyQueries.fetchLastkeyPathIndex()
            .asFlow()
            .map {
                it.executeAsList().firstOrNull()?.keyPathIndex ?: 0
            }
    }

    /**
     * Retrieves all PeerDIDs.
     *
     * @return A flow of lists of PeerDIDs.
     */
    override fun getAllPeerDIDs(): Flow<List<PeerDID>> {
        return getInstance().dIDQueries.fetchAllPeerDID()
            .asFlow()
            .map { allDIDs ->
                allDIDs.executeAsList()
                    .groupBy { allPeerDid -> allPeerDid.did }
                    .map {
                        val privateKeyList: Array<PrivateKey> = it.value.map { allPeerDID ->
                            when (allPeerDID.restorationIdentifier) {
                                "secp256k1+priv" -> {
                                    Secp256k1PrivateKey(allPeerDID.data_.base64UrlDecodedBytes)
                                }

                                "ed25519+priv" -> {
                                    Ed25519PrivateKey(allPeerDID.data_.base64UrlDecodedBytes)
                                }

                                "x25519+priv" -> {
                                    X25519PrivateKey(allPeerDID.data_.base64UrlDecodedBytes)
                                }

                                else -> {
                                    throw PlutoError.InvalidRestorationIdentifier()
                                }
                            }
                        }.toTypedArray()
                        PeerDID(DID(it.key), privateKeyList)
                    }
            }
    }

    /**
     * Retrieves all DIDs.
     *
     * @return A flow of lists of DIDs.
     */
    override fun getAllDIDs(): Flow<List<DID>> {
        return getInstance().dIDQueries.fetchAllDIDs()
            .asFlow()
            .map { didList ->
                didList.executeAsList()
                    .map { did ->
                        DID(
                            schema = did.schema,
                            method = did.method,
                            methodId = did.methodId,
                            alias = did.alias
                        )
                    }
            }
    }

    /**
     * Retrieves all the pairs of DIDs stored in the system.
     *
     * @return a [Flow] emitting a list of [DIDPair] objects representing the pairs of DIDs.
     */
    override fun getAllDidPairs(): Flow<List<DIDPair>> {
        return getInstance().dIDPairQueries.fetchAllDIDPairs()
            .asFlow()
            .map { didPair ->
                didPair.executeAsList()
                    .map { DIDPair(DID(it.hostDID), DID(it.receiverDID), it.name) }
            }
    }

    /**
     * Retrieves a DIDPair object using the provided DID.
     *
     * @param did The DID to search for.
     * @return A Flow of DIDPair objects. If a match is found, the flow emits the matching DIDPair.
     * If no match is found, the flow emits null.
     */
    override fun getPairByDID(did: DID): Flow<DIDPair?> {
        return getInstance().dIDPairQueries.fetchDIDPairByDID(did.toString())
            .asFlow()
            .map {
                try {
                    val didPair = it.executeAsOne()
                    DIDPair(DID(didPair.hostDID), DID(didPair.receiverDID), didPair.name)
                } catch (e: NullPointerException) {
                    null
                }
            }
    }

    /**
     * Retrieve a [DIDPair] from a flow by its name.
     *
     * @param name The name of the [DIDPair] to retrieve.
     * @return A [Flow] emitting the [DIDPair] object that matches the given name,
     *         or `null` if no matching [DIDPair] is found.
     */
    override fun getPairByName(name: String): Flow<DIDPair?> {
        return getInstance().dIDPairQueries.fetchDIDPairByName(name)
            .asFlow()
            .map {
                try {
                    val didPair = it.executeAsOne()
                    DIDPair(DID(didPair.hostDID), DID(didPair.receiverDID), didPair.name)
                } catch (e: NullPointerException) {
                    null
                }
            }
    }

    /**
     * Retrieves all the messages.
     *
     * @return a Flow of List of Message objects representing all the messages.
     */
    override fun getAllMessages(): Flow<List<Message>> {
        return getInstance().messageQueries.fetchAllMessages()
            .asFlow()
            .map {
                it.executeAsList().map { message ->
                    val messageDb = Json.decodeFromString<Message>(message.dataJson)
                    Message(
                        messageDb.id,
                        messageDb.piuri,
                        messageDb.from,
                        messageDb.to,
                        messageDb.fromPrior,
                        messageDb.body,
                        messageDb.extraHeaders,
                        messageDb.createdTime,
                        messageDb.expiresTimePlus,
                        messageDb.attachments,
                        messageDb.thid,
                        messageDb.pthid,
                        messageDb.ack,
                        messageDb.direction
                    )
                }
            }
    }

    /**
     * Retrieves all the messages of the provided type.
     *
     * @param type The message type as a string
     * @return a Flow of List of Message objects representing all the messages of the type
     */
    override fun getAllMessagesByType(type: String): Flow<List<Message>> {
        return getInstance().messageQueries.fetchAllMessagesByType(type)
            .asFlow()
            .map {
                it.executeAsList().map { message ->
                    val messageDb = Json.decodeFromString<Message>(message.dataJson)
                    Message(
                        messageDb.id,
                        messageDb.piuri,
                        messageDb.from,
                        messageDb.to,
                        messageDb.fromPrior,
                        messageDb.body,
                        messageDb.extraHeaders,
                        messageDb.createdTime,
                        messageDb.expiresTimePlus,
                        messageDb.attachments,
                        messageDb.thid,
                        messageDb.pthid,
                        messageDb.ack,
                        messageDb.direction
                    )
                }
            }
    }

    /**
     * Retrieves all messages based on the provided DID.
     *
     * @param did The DID (Direct Inward Dialing) to filter messages by.
     * @return A flow of list of messages.
     */
    override fun getAllMessages(did: DID): Flow<List<Message>> {
        return getAllMessages(did, did)
    }

    /**
     * Retrieves all messages exchanged between the specified 'from' and 'to' DIDs.
     *
     * @param from the sender DID
     * @param to the receiver DID
     * @return a Flow emitting a list of messages exchanged between the 'from' and 'to' DIDs
     */
    override fun getAllMessages(from: DID, to: DID): Flow<List<Message>> {
        return getInstance().messageQueries.fetchAllMessagesFromTo(from.toString(), to.toString())
            .asFlow()
            .map {
                it.executeAsList().map { message ->
                    val messageDb = Json.decodeFromString<Message>(message.dataJson)
                    Message(
                        messageDb.id,
                        messageDb.piuri,
                        messageDb.from,
                        messageDb.to,
                        messageDb.fromPrior,
                        messageDb.body,
                        messageDb.extraHeaders,
                        messageDb.createdTime,
                        messageDb.expiresTimePlus,
                        messageDb.attachments,
                        messageDb.thid,
                        messageDb.pthid,
                        messageDb.ack,
                        messageDb.direction
                    )
                }
            }
    }

    /**
     * Retrieves all the messages that have been sent.
     *
     * @return A [Flow] of type [List] containing the sent messages.
     */
    override fun getAllMessagesSent(): Flow<List<Message>> {
        return getInstance().messageQueries.fetchAllSentMessages()
            .asFlow()
            .map {
                it.executeAsList().map { message ->
                    val messageDb = Json.decodeFromString<Message>(message.dataJson)
                    Message(
                        messageDb.id,
                        messageDb.piuri,
                        messageDb.from,
                        messageDb.to,
                        messageDb.fromPrior,
                        messageDb.body,
                        messageDb.extraHeaders,
                        messageDb.createdTime,
                        messageDb.expiresTimePlus,
                        messageDb.attachments,
                        messageDb.thid,
                        messageDb.pthid,
                        messageDb.ack,
                        messageDb.direction
                    )
                }
            }
    }

    /**
     * Retrieves all messages received by the user.
     *
     * @return A [Flow] emitting a list of [Message] objects representing all the messages received.
     */
    override fun getAllMessagesReceived(): Flow<List<Message>> {
        return getInstance().messageQueries.fetchAllReceivedMessages()
            .asFlow()
            .map {
                it.executeAsList().map { message ->
                    val messageDb = Json.decodeFromString<Message>(message.dataJson)
                    Message(
                        messageDb.id,
                        messageDb.piuri,
                        messageDb.from,
                        messageDb.to,
                        messageDb.fromPrior,
                        messageDb.body,
                        messageDb.extraHeaders,
                        messageDb.createdTime,
                        messageDb.expiresTimePlus,
                        messageDb.attachments,
                        messageDb.thid,
                        messageDb.pthid,
                        messageDb.ack,
                        messageDb.direction
                    )
                }
            }
    }

    /**
     * Retrieves all messages sent to the specified DID.
     *
     * @param did the destination DID to filter the messages by
     * @return a [Flow] of [List] of [Message] objects containing all the messages sent to the specified DID
     */
    override fun getAllMessagesSentTo(did: DID): Flow<List<Message>> {
        return getInstance().messageQueries.fetchAllMessagesSentTo(did.toString())
            .asFlow()
            .map {
                it.executeAsList().map { message ->
                    val messageDb = Json.decodeFromString<Message>(message.dataJson)
                    Message(
                        messageDb.id,
                        messageDb.piuri,
                        messageDb.from,
                        messageDb.to,
                        messageDb.fromPrior,
                        messageDb.body,
                        messageDb.extraHeaders,
                        messageDb.createdTime,
                        messageDb.expiresTimePlus,
                        messageDb.attachments,
                        messageDb.thid,
                        messageDb.pthid,
                        messageDb.ack,
                        messageDb.direction
                    )
                }
            }
    }

    /**
     * Returns a Flow of lists of all messages received from the specified DID.
     *
     * @param did the DID (Decentralized Identifier) to get the received messages from
     * @return a Flow of lists of messages received from the specified DID
     */
    override fun getAllMessagesReceivedFrom(did: DID): Flow<List<Message>> {
        return getInstance().messageQueries.fetchAllMessagesReceivedFrom(did.toString())
            .asFlow()
            .map {
                it.executeAsList().map { message ->
                    val messageDb = Json.decodeFromString<Message>(message.dataJson)
                    Message(
                        messageDb.id,
                        messageDb.piuri,
                        messageDb.from,
                        messageDb.to,
                        messageDb.fromPrior,
                        messageDb.body,
                        messageDb.extraHeaders,
                        messageDb.createdTime,
                        messageDb.expiresTimePlus,
                        messageDb.attachments,
                        messageDb.thid,
                        messageDb.pthid,
                        messageDb.ack,
                        messageDb.direction
                    )
                }
            }
    }

    /**
     * Retrieves all messages of a specific type that are related to a given DID.
     *
     * @param type The type of the messages to retrieve.
     * @param relatedWithDID The optional DID to which the messages are related.
     * @return A [Flow] emitting a list of [Message] objects that match the given type and are related to the specified DID.
     */
    override fun getAllMessagesOfType(type: String, relatedWithDID: DID?): Flow<List<Message>> {
        return getInstance().messageQueries.fetchAllMessagesOfType(
            type,
            relatedWithDID.toString()
        )
            .asFlow()
            .map {
                it.executeAsList().map { message ->
                    val messageDb = Json.decodeFromString<Message>(message.dataJson)
                    Message(
                        messageDb.id,
                        messageDb.piuri,
                        messageDb.from,
                        messageDb.to,
                        messageDb.fromPrior,
                        messageDb.body,
                        messageDb.extraHeaders,
                        messageDb.createdTime,
                        messageDb.expiresTimePlus,
                        messageDb.attachments,
                        messageDb.thid,
                        messageDb.pthid,
                        messageDb.ack,
                        messageDb.direction
                    )
                }
            }
    }

    /**
     * Retrieves the message with the specified ID.
     *
     * @param id The unique ID of the message.
     * @return A [Flow] that emits the message with the specified ID, or null if no such message exists.
     *         The [Flow] completes when the message is successfully retrieved, or when an error occurs.
     */
    override fun getMessage(id: String): Flow<Message?> {
        return getInstance().messageQueries.fetchMessageById(id)
            .asFlow()
            .map {
                try {
                    val messageDb = Json.decodeFromString<Message>(it.executeAsOne().dataJson)
                    Message(
                        messageDb.id,
                        messageDb.piuri,
                        messageDb.from,
                        messageDb.to,
                        messageDb.fromPrior,
                        messageDb.body,
                        messageDb.extraHeaders,
                        messageDb.createdTime,
                        messageDb.expiresTimePlus,
                        messageDb.attachments,
                        messageDb.thid,
                        messageDb.pthid,
                        messageDb.ack,
                        messageDb.direction
                    )
                } catch (e: NullPointerException) {
                    null
                }
            }
    }

    override fun getMessageByThidAndPiuri(thid: String, piuri: String): Flow<Message?> {
        return getInstance().messageQueries.fetchMessageByThidAndPiuri(thid, piuri)
            .asFlow()
            .map {
                try {
                    val messageDb = Json.decodeFromString<Message>(it.executeAsOne().dataJson)
                    Message(
                        messageDb.id,
                        messageDb.piuri,
                        messageDb.from,
                        messageDb.to,
                        messageDb.fromPrior,
                        messageDb.body,
                        messageDb.extraHeaders,
                        messageDb.createdTime,
                        messageDb.expiresTimePlus,
                        messageDb.attachments,
                        messageDb.thid,
                        messageDb.pthid,
                        messageDb.ack,
                        messageDb.direction
                    )
                } catch (e: NullPointerException) {
                    null
                }
            }
    }

    /**
     * Returns a Flow of lists of [Mediator] objects representing all the available mediators.
     *
     * @return a Flow of lists of [Mediator] objects.
     */
    override fun getAllMediators(): Flow<List<Mediator>> {
        return getInstance().mediatorQueries.fetchAllMediators()
            .asFlow()
            .map { query ->
                val fetchAllMediators = query.executeAsList()
                fetchAllMediators.map {
                    Mediator(
                        it.id,
                        DID(it.MediatorDID),
                        DID(it.HostDID),
                        DID(it.RoutingDID)
                    )
                }
            }
    }

    /**
     * Retrieves all credentials for credential recovery.
     *
     * @return A flow of a list of [CredentialRecovery] objects representing the credentials for recovery.
     */
    override fun getAllCredentials(): Flow<List<CredentialRecovery>> {
        return getInstance().storableCredentialQueries.fetchAllCredentials()
            .asFlow()
            .map {
                it.executeAsList().map { credential ->
                    CredentialRecovery(
                        restorationId = credential.recoveryId,
                        credentialData = credential.credentialData,
                        revoked = credential.revoked != 0
                    )
                }
            }
    }

    /**
     * Inserts an available claim for a specific credential.
     *
     * @param credentialId The ID of the credential.
     * @param claim The claim to insert.
     */
    override fun insertAvailableClaim(credentialId: String, claim: String) {
        getInstance().availableClaimsQueries.insert(credentialId, claim)
    }

    /**
     * Inserts the available claims for a given credential ID.
     *
     * @param credentialId the ID of the credential
     * @param claims an array of available claims to be inserted
     */
    override fun insertAvailableClaims(credentialId: String, claims: Array<String>) {
        getInstance().availableClaimsQueries.transaction {
            claims.forEach {
                getInstance().availableClaimsQueries.insert(credentialId, it)
            }
        }
    }

    /**
     * Retrieves the available claims for a given credential ID.
     *
     * @param credentialId The ID of the credential.
     * @return A flow that emits an array of AvailableClaims.
     */
    override fun getAvailableClaimsByCredentialId(credentialId: String): Flow<Array<AvailableClaimsDB>> {
        return getInstance().availableClaimsQueries.fetchAvailableClaimsByCredentialId(credentialId)
            .asFlow()
            .map { claims ->
                claims.executeAsList().toTypedArray()
            }
    }

    /**
     * Retrieves the available claims for a given claim.
     *
     * @param claim The claim for which the available claims are to be retrieved.
     * @return A flow of arrays of AvailableClaims representing the available claims for the given claim.
     */
    override fun getAvailableClaimsByClaim(claim: String): Flow<Array<AvailableClaimsDB>> {
        return getInstance().availableClaimsQueries.fetchAvailableClaimsByClaim(claim)
            .asFlow()
            .map { claims ->
                claims.executeAsList().toTypedArray()
            }
    }

    /**
     * Retrieves the secret link associated with the current instance.
     *
     * @return A [Flow] emitting the secret link as a nullable [String].
     */
    override fun getLinkSecret(): Flow<String?> {
        return getInstance().linkSecretQueries.fetchLinkSecret()
            .asFlow()
            .map {
                val result = it.executeAsList()
                if (result.isEmpty()) {
                    null
                } else {
                    it.executeAsOne()
                }
            }
    }

    /**
     * Retrieves the metadata associated with a credential request.
     *
     * @param linkSecretName The name of the link secret used for the credential request.
     * @return A [Flow] emitting the [CredentialRequestMeta] object for the specified link secret name,
     * or null if no metadata is found.
     */
    override fun getCredentialMetadata(linkSecretName: String): Flow<CredentialRequestMeta?> {
        return getInstance().credentialMetadataQueries.fetchCredentialMetadata(linkSecretName = linkSecretName)
            .asFlow()
            .map {
                val metadata = it.executeAsOne()
                CredentialRequestMeta(
                    linkSecretName = metadata.linkSecretName,
                    json = metadata.json
                )
            }
    }

    /**
     * Revokes an existing credential using the credential ID.
     *
     * @param credentialId The ID of the credential to be revoked
     */
    override fun revokeCredential(credentialId: String) {
        getInstance().storableCredentialQueries.revokeCredentialById(credentialId)
    }

    /**
     * Provides a flow to listen for revoked credentials.
     */
    override fun observeRevokedCredentials(): Flow<List<CredentialRecovery>> {
        return getInstance().storableCredentialQueries.observeRevokedCredential()
            .asFlow()
            .map {
                it.executeAsList().map { credential ->
                    CredentialRecovery(
                        restorationId = credential.recoveryId,
                        credentialData = credential.credentialData,
                        revoked = credential.revoked != 0
                    )
                }
            }
    }

    /**
     * Converts a storable key to a JSON Web Key (JWK) object.
     *
     * @param restorationIdentifier The restoration identifier of the storable key.
     * @param b64Bytes The base64-encoded bytes of the storable key.
     * @return The JSON Web Key (JWK) object representing the storable key.
     * @throws UnknownError.SomethingWentWrongError if the restoration identifier is unknown.
     */
    private fun storableKeyToJWK(restorationIdentifier: String, b64Bytes: String): JWK {
        return when (restorationIdentifier) {
            "secp256k1+priv" -> {
                Secp256k1PrivateKey(b64Bytes.base64UrlDecodedBytes)
                    .getJwk()
            }

            "x25519+priv" -> {
                X25519PrivateKey(b64Bytes.base64UrlDecodedBytes)
                    .getJwk()
            }

            "ed25519+priv" -> {
                Ed25519PrivateKey(b64Bytes.base64UrlDecodedBytes)
                    .getJwk()
            }

            else -> {
                // Should never run
                throw UnknownError.SomethingWentWrongError("Unknown restoration identifier: $restorationIdentifier")
            }
        }
    }

    /**
     * Retrieves all the keys for backup.
     *
     * @return A flow that emits a list of [BackupV0_0_1.Key] objects.
     */
    override fun getAllKeysForBackUp(): Flow<List<BackupV0_0_1.Key>> {
        val keysWithDID = getInstance().privateKeyQueries.fetchAllPrivateKeyWithDID()
            .executeAsList()
            .map { keyDIDRecord ->
                val jwk = storableKeyToJWK(keyDIDRecord.restorationIdentifier, keyDIDRecord.data_)
                BackupV0_0_1.Key(
                    key = Json.encodeToString(jwk),
                    did = keyDIDRecord.id,
                    index = keyDIDRecord.keyPathIndex,
                    recoveryId = keyDIDRecord.restorationIdentifier
                )
            }
        val keysWithNoDID = getInstance().privateKeyQueries.fetchAllPrivateKeys()
            .executeAsList()
            .map { key ->
                val jwk = storableKeyToJWK(key.restorationIdentifier, key.data_)
                BackupV0_0_1.Key(
                    key = Json.encodeToString(jwk),
                    did = null,
                    index = key.keyPathIndex,
                    recoveryId = key.restorationIdentifier
                )
            }.filter { keyWithNoDID ->
                keysWithDID.firstOrNull { keyWithDID ->
                    keyWithNoDID.key == keyWithDID.key
                }?.let {
                    false
                } ?: run {
                    true
                }
            }
        val keys = keysWithDID + keysWithNoDID
        return flowOf(keys)
    }

    override fun getAllPrivateKeys(): Flow<List<StorablePrivateKey>> {
        return getInstance().privateKeyQueries
            .fetchAllPrivateKeys()
            .asFlow()
            .map {
                it.executeAsList()
                    .map { storableKey ->
                        StorablePrivateKey(
                            id = storableKey.id,
                            restorationIdentifier = storableKey.restorationIdentifier,
                            data = storableKey.data_,
                            keyPathIndex = storableKey.keyPathIndex
                        )
                    }
            }
    }

    fun getAllDIDKeyLinkData(): Flow<List<DidKeyLink>> {
        return getInstance().dIDKeyLinkQueries
            .fetchAll()
            .asFlow()
            .map { didKeyLinks ->
                didKeyLinks.executeAsList().map { didKeyLink ->
                    DidKeyLink(
                        id = didKeyLink.id.toInt(),
                        didId = didKeyLink.didId,
                        keyId = didKeyLink.keyId,
                        alias = didKeyLink.alias
                    )
                }
            }
    }
}
