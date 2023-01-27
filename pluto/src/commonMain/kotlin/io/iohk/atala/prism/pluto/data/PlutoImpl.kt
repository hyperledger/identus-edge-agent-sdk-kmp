package io.iohk.atala.prism.pluto.data

import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import com.squareup.sqldelight.runtime.coroutines.mapToOne
import io.iohk.atala.prism.apollo.uuid.UUID
import io.iohk.atala.prism.domain.buildingBlocks.Pluto
import io.iohk.atala.prism.domain.models.CredentialType
import io.iohk.atala.prism.domain.models.DID
import io.iohk.atala.prism.domain.models.DIDPair
import io.iohk.atala.prism.domain.models.JWTVerifiableCredential
import io.iohk.atala.prism.domain.models.MediatorDID
import io.iohk.atala.prism.domain.models.Message
import io.iohk.atala.prism.domain.models.PeerDID
import io.iohk.atala.prism.domain.models.PrismDIDInfo
import io.iohk.atala.prism.domain.models.PrivateKey
import io.iohk.atala.prism.domain.models.VerifiableCredential
import io.iohk.atala.prism.domain.models.W3CVerifiableCredential
import io.iohk.atala.prism.domain.models.getKeyCurveByNameAndIndex
import io.iohk.atala.prism.pluto.PrismPlutoDb
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import ioiohkatalaprismpluto.data.DID as DIDDB
import ioiohkatalaprismpluto.data.DIDPair as DIDPairDB
import ioiohkatalaprismpluto.data.Mediator as MediatorDB
import ioiohkatalaprismpluto.data.Message as MessageDB
import ioiohkatalaprismpluto.data.PrivateKey as PrivateKeyDB
import ioiohkatalaprismpluto.data.VerifiableCredential as VerifiableCredentialDB

class PlutoImpl(connection: DbConnection) : Pluto {

    private lateinit var db: PrismPlutoDb

    init {
        GlobalScope.launch {
            db = connection.connectDb()
        }
    }

    override fun storePrismDID(did: DID, keyPathIndex: Int, alias: String?) {
        db.dIDQueries.insert(DIDDB(did.toString(), did.method, did.methodId, did.schema, alias))
    }

    override fun storePeerDID(did: DID, privateKeys: List<PrivateKey>) {
        db.dIDQueries.insert(DIDDB(did.toString(), did.method, did.methodId, did.schema, null))
        privateKeys.map { privateKey ->
            db.privateKeyQueries.insert(
                PrivateKeyDB(
                    UUID.randomUUID4().toString(),
                    privateKey.keyCurve.curve.value,
                    privateKey.value.toString(),
                    privateKey.keyCurve.index,
                    did.methodId
                )
            )
        }
    }

    override fun storeDIDPair(host: DID, receiver: DID, name: String) {
        db.dIDPairQueries.insert(DIDPairDB("$host$receiver", name, host.toString(), receiver.toString()))
    }

    override fun storeMessage(message: Message) {
        db.messageQueries.insert(
            MessageDB(
                UUID.randomUUID4().toString(),
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

    override fun storeMessages(messages: List<Message>) {
        messages.map { message ->
            storeMessage(message)
        }
    }

    override fun storeMediator(mediator: DID, host: DID, routing: DID) {
        db.mediatorQueries.insert(
            MediatorDB(
                UUID.randomUUID4().toString(),
                mediator.methodId,
                host.methodId,
                routing.methodId
            )
        )
    }

    override fun storeCredential(credential: VerifiableCredential) {
        db.verifiableCredentialQueries.insert(
            VerifiableCredentialDB(
                UUID.randomUUID4().toString(),
                credential.credentialType.type,
                credential.expirationDate,
                credential.issuanceDate,
                credential.toJsonString(),
                credential.issuer.toString()
            )
        )
    }

    override fun getAllPrismDIDs(): Flow<List<PrismDIDInfo>> {
        return db.dIDQueries.fetchAllPrismDID()
            .asFlow()
            .mapToList()
            .map { list ->
                list.map {
                    PrismDIDInfo(DID(it.did), it.keyPathIndex, it.alias)
                }
            }
    }

    override fun getDIDInfoByDID(did: DID): Flow<PrismDIDInfo?> {
        return db.dIDQueries.fetchDIDInfoByDID(did.toString())
            .asFlow()
            .mapToOne()
            .map {
                PrismDIDInfo(DID(it.did), it.keyPathIndex, it.alias)
            }
    }

    override fun getDIDInfoByAlias(alias: String): Flow<List<PrismDIDInfo>> {
        return db.dIDQueries.fetchDIDInfoByAlias(alias)
            .asFlow()
            .mapToList()
            .map { list ->
                list.map {
                    PrismDIDInfo(DID(it.did), it.keyPathIndex, it.alias)
                }
            }
    }

    override fun getDIDPrivateKeysByDID(did: DID): Flow<List<PrivateKey>?> {
        return db.privateKeyQueries.fetchPrivateKeyByDID(did.toString())
            .asFlow()
            .mapToList()
            .map { list ->
                list.map {
                    PrivateKey(getKeyCurveByNameAndIndex(it.curve, it.keyPathIndex), byteArrayOf())
                }
            }
    }

    override fun getPrismDIDKeyPathIndex(did: DID): Flow<Int?> {
        return db.privateKeyQueries.fetchKeyPathIndexByDID(did.methodId)
            .asFlow()
            .mapToOne()
            .map { it.keyPathIndex }
    }

    override fun getPrismLastKeyPathIndex(): Flow<Int> {
        return db.privateKeyQueries.fetchLastkeyPathIndex()
            .asFlow()
            .mapToOne()
            .map { it.keyPathIndex ?: 0 }
    }

    override fun getAllPeerDIDs(): Flow<List<PeerDID>> {
        return db.dIDQueries.fetchAllPeerDID()
            .asFlow()
            .mapToList()
            .map { list ->
                list
                    .groupBy { it.did }
                    .map {
                        var privateKeyList = it.value.map { data ->
                            PrivateKey(
                                getKeyCurveByNameAndIndex(data.curve, data.keyPathIndex),
                                byteArrayOf()
                            )
                        }.toTypedArray()
                        PeerDID(DID(it.key), privateKeyList)
                    }
            }
    }

    override fun getAllDidPairs(): Flow<List<DIDPair>> {
        return db.dIDPairQueries.fetchAllDIDPairs()
            .asFlow()
            .mapToList()
            .map { list ->
                list.map {
                    DIDPair(DID(it.hostDID), DID(it.receiverDID), it.name)
                }
            }
    }

    override fun getPair(did: DID): Flow<List<DIDPair>?> {
        return db.dIDPairQueries.fetchDIDPairByDID(did.toString())
            .asFlow()
            .mapToList()
            .map { list ->
                list.map {
                    DIDPair(DID(it.hostDID), DID(it.receiverDID), it.name)
                }
            }
    }

    override fun getPair(name: String): Flow<DIDPair?> {
        return db.dIDPairQueries.fetchDIDPairByName(name)
            .asFlow()
            .mapToOne()
            .map {
                DIDPair(DID(it.hostDID), DID(it.receiverDID), it.name)
            }
    }

    override fun getAllMessages(): Flow<List<Message>> {
        return db.messageQueries.fetchAllMessages()
            .asFlow()
            .mapToList()
            .map { list ->
                list.map {
                    val messageDb = Json.decodeFromString<Message>(it.dataJson)
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
        return db.messageQueries.fetchAllMessagesFromTo(from.toString(), to.toString())
            .asFlow()
            .mapToList()
            .map { list ->
                list.map {
                    val messageDb = Json.decodeFromString<Message>(it.dataJson)
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
        return db.messageQueries.fetchAllSentMessages()
            .asFlow()
            .mapToList()
            .map { list ->
                list.map {
                    val messageDb = Json.decodeFromString<Message>(it.dataJson)
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
        return db.messageQueries.fetchAllReceivedMessages()
            .asFlow()
            .mapToList()
            .map { list ->
                list.map {
                    val messageDb = Json.decodeFromString<Message>(it.dataJson)
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
        return db.messageQueries.fetchAllMessagesSentTo(did.toString())
            .asFlow()
            .mapToList()
            .map { list ->
                list.map {
                    val messageDb = Json.decodeFromString<Message>(it.dataJson)
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
        return db.messageQueries.fetchAllMessagesReceivedFrom(did.toString())
            .asFlow()
            .mapToList()
            .map { list ->
                list.map {
                    val messageDb = Json.decodeFromString<Message>(it.dataJson)
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
        return db.messageQueries.fetchAllMessagesOfType(type, relatedWithDID.toString(), relatedWithDID.toString())
            .asFlow()
            .mapToList()
            .map { list ->
                list.map {
                    val messageDb = Json.decodeFromString<Message>(it.dataJson)
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
        return db.messageQueries.fetchMessageById(id)
            .asFlow()
            .mapToOne()
            .map {
                val messageDb = Json.decodeFromString<Message>(it.dataJson)
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

    override fun getAllMediators(): Flow<List<MediatorDID>> {
        return db.mediatorQueries.fetchAllMediators()
            .asFlow()
            .mapToList()
            .map { list ->
                list.map {
                    MediatorDID(
                        it.id,
                        DID(it.MediatorDID),
                        DID(it.HostDID),
                        DID(it.RoutingDID)
                    )
                }
            }
    }

    // TODO: Define how to form JWTVerifiableCredential and W3CVerifiableCredential
    override fun getAllCredentials(): Flow<List<VerifiableCredential>> {
        return db.verifiableCredentialQueries.fetchAllCredentials()
            .asFlow()
            .mapToList()
            .map { list ->
                list.map {
                    val verifiableCredential = Json.decodeFromString<VerifiableCredential>(it.verifiableCredentialJson)
                    when (it.credentialType) {
                        CredentialType.JWT.type ->
                            JWTVerifiableCredential(
                                CredentialType.JWT,
                                verifiableCredential.id,
                                verifiableCredential.context,
                                verifiableCredential.type,
                                verifiableCredential.issuer,
                                verifiableCredential.issuanceDate,
                                verifiableCredential.expirationDate,
                                verifiableCredential.credentialSchema,
                                verifiableCredential.credentialSubject,
                                verifiableCredential.credentialStatus,
                                verifiableCredential.refreshService,
                                verifiableCredential.evidence,
                                verifiableCredential.termsOfUse,
                                verifiableCredential.validFrom,
                                verifiableCredential.validUntil,
                                verifiableCredential.proof,
                                verifiableCredential.aud
                            )

                        CredentialType.W3C.type ->
                            W3CVerifiableCredential(
                                CredentialType.JWT,
                                verifiableCredential.id,
                                verifiableCredential.context,
                                verifiableCredential.type,
                                verifiableCredential.issuer,
                                verifiableCredential.issuanceDate,
                                verifiableCredential.expirationDate,
                                verifiableCredential.credentialSchema,
                                verifiableCredential.credentialSubject,
                                verifiableCredential.credentialStatus,
                                verifiableCredential.refreshService,
                                verifiableCredential.evidence,
                                verifiableCredential.termsOfUse,
                                verifiableCredential.validFrom,
                                verifiableCredential.validUntil,
                                verifiableCredential.proof,
                                verifiableCredential.aud
                            )

                        else ->
                            JWTVerifiableCredential(
                                CredentialType.JWT,
                                verifiableCredential.id,
                                verifiableCredential.context,
                                verifiableCredential.type,
                                verifiableCredential.issuer,
                                verifiableCredential.issuanceDate,
                                verifiableCredential.expirationDate,
                                verifiableCredential.credentialSchema,
                                verifiableCredential.credentialSubject,
                                verifiableCredential.credentialStatus,
                                verifiableCredential.refreshService,
                                verifiableCredential.evidence,
                                verifiableCredential.termsOfUse,
                                verifiableCredential.validFrom,
                                verifiableCredential.validUntil,
                                verifiableCredential.proof,
                                verifiableCredential.aud
                            )
                    }
                }
            }
    }
}
