package io.iohk.atala.prism.walletsdk.pluto

import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import io.iohk.atala.prism.apollo.uuid.UUID
import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Pluto
import io.iohk.atala.prism.walletsdk.domain.models.CredentialType
import io.iohk.atala.prism.walletsdk.domain.models.DID
import io.iohk.atala.prism.walletsdk.domain.models.DIDPair
import io.iohk.atala.prism.walletsdk.domain.models.JWTCredentialPayload
import io.iohk.atala.prism.walletsdk.domain.models.Mediator
import io.iohk.atala.prism.walletsdk.domain.models.Message
import io.iohk.atala.prism.walletsdk.domain.models.PeerDID
import io.iohk.atala.prism.walletsdk.domain.models.PlutoError
import io.iohk.atala.prism.walletsdk.domain.models.PrismDIDInfo
import io.iohk.atala.prism.walletsdk.domain.models.PrivateKey
import io.iohk.atala.prism.walletsdk.domain.models.VerifiableCredential
import io.iohk.atala.prism.walletsdk.domain.models.W3CVerifiableCredential
import io.iohk.atala.prism.walletsdk.domain.models.getKeyCurveByNameAndIndex
import io.iohk.atala.prism.walletsdk.pluto.data.DbConnection
import io.iohk.atala.prism.walletsdk.pluto.data.isConnected
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import ioiohkatalaprismwalletsdkpluto.data.DID as DIDDB
import ioiohkatalaprismwalletsdkpluto.data.DIDPair as DIDPairDB
import ioiohkatalaprismwalletsdkpluto.data.Mediator as MediatorDB
import ioiohkatalaprismwalletsdkpluto.data.Message as MessageDB
import ioiohkatalaprismwalletsdkpluto.data.PrivateKey as PrivateKeyDB
import ioiohkatalaprismwalletsdkpluto.data.VerifiableCredential as VerifiableCredentialDB

class PlutoImpl(private val connection: DbConnection) : Pluto {
    private var db: PrismPlutoDb? = null

    val isConnected: Boolean
        get() {
            return this.connection.driver?.isConnected ?: false
        }

    public suspend fun start(context: Any? = null) {
        if (this.db != null) {
            throw PlutoError.DatabaseServiceAlreadyRunning()
        }
        this.db = this.connection.connectDb(context)
    }

    public fun stop() {
        val driver = this.connection.driver ?: throw PlutoError.DatabaseConnectionError()
        this.db = null
        driver.close()
    }

    private fun getInstance(): PrismPlutoDb {
        return this.db ?: throw PlutoError.DatabaseConnectionError()
    }

    override fun storePrismDIDAndPrivateKeys(
        did: DID,
        keyPathIndex: Int,
        alias: String?,
        privateKeys: List<PrivateKey>,
    ) {
        getInstance().dIDQueries.insert(DIDDB(did.methodId, did.method, did.methodId, did.schema, alias))
        privateKeys.map { privateKey ->
            storePrivateKeys(privateKey, did, keyPathIndex)
        }
    }

    override fun storePeerDIDAndPrivateKeys(did: DID, privateKeys: List<PrivateKey>) {
        getInstance().dIDQueries.insert(DIDDB(did.methodId, did.method, did.methodId, did.schema, null))
        privateKeys.map { privateKey ->
            storePrivateKeys(privateKey, did, privateKey.keyCurve.index ?: 0)
        }
    }

    override fun storeDIDPair(host: DID, receiver: DID, name: String) {
        getInstance().dIDPairQueries.insert(DIDPairDB("$host$receiver", name, host.toString(), receiver.toString()))
    }

    override fun storeMessage(message: Message) {
        getInstance().messageQueries.insert(
            MessageDB(
                UUID.randomUUID4().toString(),
                message.createdTime,
                message.toJsonString(),
                message.from.toString(),
                message.thid,
                message.to.toString(),
                message.piuri,
                message.direction.value,
            ),
        )
    }

    override fun storePrivateKeys(privateKey: PrivateKey, did: DID, keyPathIndex: Int, metaId: String?) {
        getInstance().privateKeyQueries.insert(
            PrivateKeyDB(
                metaId ?: UUID.randomUUID4().toString(),
                privateKey.keyCurve.curve.value,
                privateKey.value.toString(),
                keyPathIndex,
                did.toString(),
            ),
        )
    }

    override fun storeMessages(messages: List<Message>) {
        messages.map { message ->
            storeMessage(message)
        }
    }

    override fun storeMediator(mediator: DID, host: DID, routing: DID) {
        getInstance().mediatorQueries.insert(
            MediatorDB(
                UUID.randomUUID4().toString(),
                mediator.methodId,
                host.methodId,
                routing.methodId,
            ),
        )
    }

    override fun storeCredential(credential: VerifiableCredential) {
        getInstance().verifiableCredentialQueries.insert(
            VerifiableCredentialDB(
                UUID.randomUUID4().toString(),
                credential.credentialType.type,
                credential.expirationDate,
                credential.issuanceDate,
                credential.toJsonString(),
                credential.issuer.toString(),
            ),
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
                    .map { didInfo ->
                        try {
                            PrivateKey(
                                getKeyCurveByNameAndIndex(
                                    didInfo.curve,
                                    didInfo.keyPathIndex,
                                ),
                                byteArrayOf(),
                            )
                        } catch (e: IllegalStateException) {
                            null
                        }
                    }
            }
    }

    override fun getDIDPrivateKeyByID(id: String): Flow<PrivateKey?> {
        return getInstance().privateKeyQueries
            .fetchPrivateKeyByID(id)
            .asFlow()
            .map {
                it.executeAsList().firstOrNull()?.let {
                    try {
                        PrivateKey(
                            getKeyCurveByNameAndIndex(
                                it.curve,
                                it.keyPathIndex,
                            ),
                            byteArrayOf(),
                        )
                    } catch (e: IllegalStateException) {
                        null
                    }
                }
            }
    }

    override fun getPrismDIDKeyPathIndex(did: DID): Flow<Int?> {
        return getInstance().privateKeyQueries.fetchKeyPathIndexByDID(did.methodId)
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
                        val privateKeyList = it.value.map { data ->
                            PrivateKey(
                                getKeyCurveByNameAndIndex(data.curve, data.keyPathIndex),
                                byteArrayOf(),
                            )
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
                        messageDb.direction,
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
                        messageDb.direction,
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
                        messageDb.direction,
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
                        messageDb.direction,
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
                        messageDb.direction,
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
                        messageDb.direction,
                    )
                }
            }
    }

    override fun getAllMessagesOfType(type: String, relatedWithDID: DID?): Flow<List<Message>> {
        return getInstance().messageQueries.fetchAllMessagesOfType(
            type,
            relatedWithDID.toString(),
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
                        messageDb.direction,
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
                        messageDb.direction,
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
                it.executeAsList().map { mediator ->
                    Mediator(
                        mediator.id,
                        DID(mediator.MediatorDID),
                        DID(mediator.HostDID),
                        DID(mediator.RoutingDID),
                    )
                }
            }
    }

    override fun getAllCredentials(): Flow<List<VerifiableCredential>> {
        return getInstance().verifiableCredentialQueries.fetchAllCredentials()
            .asFlow()
            .map {
                it.executeAsList().map { verifiableCredential ->
                    val verifiableCredential =
                        Json.decodeFromString<VerifiableCredential>(verifiableCredential.verifiableCredentialJson)
                    when (verifiableCredential.credentialType) {
                        CredentialType.JWT -> {
                            JWTCredentialPayload.JWTVerifiableCredential(
                                id = verifiableCredential.id,
                                credentialType = CredentialType.JWT,
                                context = verifiableCredential.context,
                                type = verifiableCredential.type,
                                credentialSchema = verifiableCredential.credentialSchema,
                                credentialSubject = verifiableCredential.credentialSubject,
                                credentialStatus = verifiableCredential.credentialStatus,
                                refreshService = verifiableCredential.refreshService,
                                evidence = verifiableCredential.evidence,
                                termsOfUse = verifiableCredential.termsOfUse,
                                issuer = verifiableCredential.issuer,
                                issuanceDate = verifiableCredential.issuanceDate,
                                expirationDate = verifiableCredential.expirationDate,
                                validFrom = verifiableCredential.validFrom,
                                validUntil = verifiableCredential.validUntil,
                                proof = verifiableCredential.proof,
                                aud = verifiableCredential.aud,
                            )
                        }

                        CredentialType.W3C ->
                            W3CVerifiableCredential(
                                id = verifiableCredential.id,
                                credentialType = CredentialType.JWT,
                                context = verifiableCredential.context,
                                type = verifiableCredential.type,
                                credentialSchema = verifiableCredential.credentialSchema,
                                credentialSubject = verifiableCredential.credentialSubject,
                                credentialStatus = verifiableCredential.credentialStatus,
                                refreshService = verifiableCredential.refreshService,
                                evidence = verifiableCredential.evidence,
                                termsOfUse = verifiableCredential.termsOfUse,
                                issuer = verifiableCredential.issuer,
                                issuanceDate = verifiableCredential.issuanceDate,
                                expirationDate = verifiableCredential.expirationDate,
                                validFrom = verifiableCredential.validFrom,
                                validUntil = verifiableCredential.validUntil,
                                proof = verifiableCredential.proof,
                                aud = verifiableCredential.aud,
                            )

                        else ->
                            JWTCredentialPayload.JWTVerifiableCredential(
                                id = verifiableCredential.id,
                                credentialType = CredentialType.JWT,
                                context = verifiableCredential.context,
                                type = verifiableCredential.type,
                                credentialSchema = verifiableCredential.credentialSchema,
                                credentialSubject = verifiableCredential.credentialSubject,
                                credentialStatus = verifiableCredential.credentialStatus,
                                refreshService = verifiableCredential.refreshService,
                                evidence = verifiableCredential.evidence,
                                termsOfUse = verifiableCredential.termsOfUse,
                                issuer = verifiableCredential.issuer,
                                issuanceDate = verifiableCredential.issuanceDate,
                                expirationDate = verifiableCredential.expirationDate,
                                validFrom = verifiableCredential.validFrom,
                                validUntil = verifiableCredential.validUntil,
                                proof = verifiableCredential.proof,
                                aud = verifiableCredential.aud,
                            )
                    }
                }
            }
    }
}
