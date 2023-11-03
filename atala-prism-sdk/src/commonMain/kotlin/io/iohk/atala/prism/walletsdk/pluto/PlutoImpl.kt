package io.iohk.atala.prism.walletsdk.pluto

import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import io.iohk.atala.prism.apollo.base64.base64UrlDecodedBytes
import io.iohk.atala.prism.apollo.base64.base64UrlEncoded
import io.iohk.atala.prism.apollo.uuid.UUID
import io.iohk.atala.prism.walletsdk.PrismPlutoDb
import io.iohk.atala.prism.walletsdk.apollo.utils.Ed25519PrivateKey
import io.iohk.atala.prism.walletsdk.apollo.utils.Secp256k1PrivateKey
import io.iohk.atala.prism.walletsdk.apollo.utils.X25519PrivateKey
import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Pluto
import io.iohk.atala.prism.walletsdk.domain.models.DID
import io.iohk.atala.prism.walletsdk.domain.models.DIDPair
import io.iohk.atala.prism.walletsdk.domain.models.Mediator
import io.iohk.atala.prism.walletsdk.domain.models.Message
import io.iohk.atala.prism.walletsdk.domain.models.PeerDID
import io.iohk.atala.prism.walletsdk.domain.models.PlutoError
import io.iohk.atala.prism.walletsdk.domain.models.PrismDIDInfo
import io.iohk.atala.prism.walletsdk.domain.models.StorableCredential
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.PrivateKey
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.StorableKey
import io.iohk.atala.prism.walletsdk.pluto.data.DbConnection
import io.iohk.atala.prism.walletsdk.pluto.data.isConnected
import io.iohk.atala.prism.walletsdk.pollux.models.CredentialRequestMeta
import io.iohk.atala.prism.walletsdk.pollux.models.LinkSecretBlindingData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ioiohkatalaprismwalletsdkpluto.data.AvailableClaims as AvailableClaimsDB
import ioiohkatalaprismwalletsdkpluto.data.DID as DIDDB
import ioiohkatalaprismwalletsdkpluto.data.DIDPair as DIDPairDB
import ioiohkatalaprismwalletsdkpluto.data.LinkSecret as LinkSecretDB
import ioiohkatalaprismwalletsdkpluto.data.Mediator as MediatorDB
import ioiohkatalaprismwalletsdkpluto.data.Message as MessageDB
import ioiohkatalaprismwalletsdkpluto.data.PrivateKey as PrivateKeyDB
import ioiohkatalaprismwalletsdkpluto.data.StorableCredential as StorableCredentialDB

class PlutoImpl(private val connection: DbConnection) : Pluto {
    private var db: PrismPlutoDb? = null

    val isConnected: Boolean
        get() {
            return this.connection.driver?.isConnected ?: false
        }

    @Throws(PlutoError.DatabaseServiceAlreadyRunning::class)
    @JvmOverloads
    public suspend fun start(context: Any? = null) {
        if (this.db != null) {
            throw PlutoError.DatabaseServiceAlreadyRunning()
        }
        this.db = this.connection.connectDb(context)
    }

    @Throws(PlutoError.DatabaseConnectionError::class)
    public fun stop() {
        val driver = this.connection.driver ?: throw PlutoError.DatabaseConnectionError()
        this.db = null
        driver.close()
    }

    @Throws(PlutoError.DatabaseConnectionError::class)
    private fun getInstance(): PrismPlutoDb {
        return this.db ?: throw PlutoError.DatabaseConnectionError()
    }

    override fun storePrismDIDAndPrivateKeys(
        did: DID,
        keyPathIndex: Int,
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
        privateKeys.map { privateKey ->
            storePrivateKeys(privateKey, did, keyPathIndex)
        }
    }

    override fun storePeerDID(did: DID) {
        getInstance().dIDQueries.insert(
            DIDDB(
                did.toString(),
                did.method,
                did.methodId,
                did.schema,
                null
            )
        )
    }

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

    override fun storePrivateKeys(
        storableKey: StorableKey,
        did: DID,
        keyPathIndex: Int,
        metaId: String?
    ) {
        metaId?.let { id ->
            val list = getInstance().privateKeyQueries.fetchPrivateKeyByID(id).executeAsList()
            if (list.isEmpty()) {
                getInstance().privateKeyQueries.insert(
                    PrivateKeyDB(
                        metaId,
                        storableKey.restorationIdentifier,
                        storableKey.storableData.base64UrlEncoded,
                        keyPathIndex,
                        did.toString()
                    )
                )
            } else {
                // TODO: Implement Delete, Update
            }
        } ?: run {
            getInstance().privateKeyQueries.insert(
                PrivateKeyDB(
                    UUID.randomUUID4().toString(),
                    storableKey.restorationIdentifier,
                    storableKey.storableData.base64UrlEncoded,
                    keyPathIndex,
                    did.toString()
                )
            )
        }
    }

    override fun storeMessages(messages: List<Message>) {
        messages.map { message ->
            storeMessage(message)
        }
    }

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
                UUID.randomUUID4().toString(),
                mediator.methodId,
                host.methodId,
                routing.methodId
            )
        )
    }

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

    override fun storeLinkSecret(linkSecret: String) {
        getInstance().linkSecretQueries
            .insert(LinkSecretDB(linkSecret))
    }

    override fun storeCredentialMetadata(metadata: CredentialRequestMeta) {
        getInstance().credentialMetadataQueries.insert(
            id = UUID.randomUUID4().toString(),
            nonce = metadata.nonce,
            linkSecretName = metadata.linkSecretName,
            linkSecretBlindingData = Json.encodeToString(metadata.linkSecretBlindingData)
        )
    }

    override fun getAllPrismDIDs(): Flow<List<PrismDIDInfo>> {
        return getInstance().dIDQueries
            .fetchAllPrismDID()
            .asFlow()
            .mapToList()
            .map { list ->
                list.map {
                    PrismDIDInfo(DID(it.did), it.keyPathIndex, it.alias)
                }
            }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getDIDInfoByDID(did: DID): Flow<PrismDIDInfo?> {
        return getInstance().dIDQueries
            .fetchDIDInfoByDID(did.methodId)
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

    override fun getDIDPrivateKeysByDID(did: DID): Flow<List<PrivateKey?>> {
        return getInstance().privateKeyQueries
            .fetchPrivateKeyByDID(did.toString())
            .asFlow()
            .map {
                it.executeAsList()
                    .map { storableKey ->
                        when (storableKey.restorationIdentifier) {
                            "secp256k1+priv" -> {
                                Secp256k1PrivateKey(storableKey.data_.base64UrlDecodedBytes)
                            }

                            "ed25519+priv" -> {
                                Ed25519PrivateKey(storableKey.data_.base64UrlDecodedBytes)
                            }

                            "x25519+priv" -> {
                                X25519PrivateKey(storableKey.data_.base64UrlDecodedBytes)
                            }

                            else -> {
                                throw PlutoError.InvalidRestorationIdentifier()
                            }
                        }
                    }
            }
    }

    override fun getDIDPrivateKeyByID(id: String): Flow<PrivateKey?> {
        return getInstance().privateKeyQueries
            .fetchPrivateKeyByID(id)
            .asFlow()
            .map { it ->
                it.executeAsList().firstOrNull()?.let { storableKey ->
                    when (storableKey.restorationIdentifier) {
                        "secp256k1+priv" -> {
                            Secp256k1PrivateKey(storableKey.data_.base64UrlDecodedBytes)
                        }

                        "ed25519+priv" -> {
                            Ed25519PrivateKey(storableKey.data_.base64UrlDecodedBytes)
                        }

                        "x25519+priv" -> {
                            X25519PrivateKey(storableKey.data_.base64UrlDecodedBytes)
                        }

                        else -> {
                            throw PlutoError.InvalidRestorationIdentifier()
                        }
                    }
                }
            }
    }

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

    override fun getPrismLastKeyPathIndex(): Flow<Int> {
        return getInstance().privateKeyQueries.fetchLastkeyPathIndex()
            .asFlow()
            .map {
                it.executeAsList().firstOrNull()?.keyPathIndex ?: 0
            }
    }

    override fun getAllPeerDIDs(): Flow<List<PeerDID>> {
        return getInstance().dIDQueries.fetchAllPeerDID()
            .asFlow()
            .map { allDIDs ->
                allDIDs.executeAsList()
                    .groupBy { allPeerDid -> allPeerDid.did }
                    .map {
                        val privateKeyList = it.value.mapNotNull { storableKey ->
                            when (storableKey.restorationIdentifier) {
                                "secp256k1+priv" -> {
                                    Secp256k1PrivateKey(storableKey.data_.base64UrlDecodedBytes)
                                }

                                "ed25519+priv" -> {
                                    Ed25519PrivateKey(storableKey.data_.base64UrlDecodedBytes)
                                }

                                "x25519+priv" -> {
                                    X25519PrivateKey(storableKey.data_.base64UrlDecodedBytes)
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

    override fun getAllDidPairs(): Flow<List<DIDPair>> {
        return getInstance().dIDPairQueries.fetchAllDIDPairs()
            .asFlow()
            .map { didPair ->
                didPair.executeAsList()
                    .map { DIDPair(DID(it.hostDID), DID(it.receiverDID), it.name) }
            }
    }

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

    override fun getAllMessages(did: DID): Flow<List<Message>> {
        return getAllMessages(did, did)
    }

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

    override fun getAllMediators(): Flow<List<Mediator>> {
        return getInstance().mediatorQueries.fetchAllMediators()
            .asFlow()
            .map {
                val fetchAllMediators = it.executeAsList()
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

    override fun getAllCredentials(): Flow<List<CredentialRecovery>> {
        return getInstance().storableCredentialQueries.fetchAllCredentials()
            .asFlow()
            .map {
                it.executeAsList().map { credential ->
                    CredentialRecovery(
                        restorationId = credential.recoveryId,
                        credentialData = credential.credentialData
                    )
                }
            }
    }

    override fun insertAvailableClaim(credentialId: String, claim: String) {
        getInstance().availableClaimsQueries.insert(credentialId, claim)
    }

    override fun insertAvailableClaims(credentialId: String, claims: Array<String>) {
        getInstance().availableClaimsQueries.transaction {
            claims.forEach {
                getInstance().availableClaimsQueries.insert(credentialId, it)
            }
        }
    }

    override fun getAvailableClaimsByCredentialId(credentialId: String): Flow<Array<AvailableClaimsDB>> {
        return getInstance().availableClaimsQueries.fetchAvailableClaimsByCredentialId(credentialId)
            .asFlow()
            .map { claims ->
                claims.executeAsList().toTypedArray()
            }
    }

    override fun getAvailableClaimsByClaim(claim: String): Flow<Array<AvailableClaimsDB>> {
        return getInstance().availableClaimsQueries.fetchAvailableClaimsByClaim(claim)
            .asFlow()
            .map { claims ->
                claims.executeAsList().toTypedArray()
            }
    }

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

    override fun getCredentialMetadata(linkSecretName: String): Flow<CredentialRequestMeta?> {
        return getInstance().credentialMetadataQueries.fetchCredentialMetadata(linkSecretName = linkSecretName)
            .asFlow()
            .map {
                val metadata = it.executeAsOne()
                CredentialRequestMeta(
                    nonce = metadata.nonce,
                    linkSecretName = metadata.linkSecretName,
                    linkSecretBlindingData = LinkSecretBlindingData(metadata.linkSecretBlindingData)
                )
            }
    }
}
