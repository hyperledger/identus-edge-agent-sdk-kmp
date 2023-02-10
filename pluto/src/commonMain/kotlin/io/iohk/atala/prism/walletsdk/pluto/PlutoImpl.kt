package io.iohk.atala.prism.walletsdk.pluto.data

import io.iohk.atala.prism.apollo.uuid.UUID
import io.iohk.atala.prism.walletsdk.domain.buildingBlocks.Pluto
import io.iohk.atala.prism.walletsdk.domain.models.CredentialType
import io.iohk.atala.prism.walletsdk.domain.models.DID
import io.iohk.atala.prism.walletsdk.domain.models.DIDPair
import io.iohk.atala.prism.walletsdk.domain.models.JWTVerifiableCredential
import io.iohk.atala.prism.walletsdk.domain.models.MediatorDID
import io.iohk.atala.prism.walletsdk.domain.models.Message
import io.iohk.atala.prism.walletsdk.domain.models.PeerDID
import io.iohk.atala.prism.walletsdk.domain.models.PrismDIDInfo
import io.iohk.atala.prism.walletsdk.domain.models.PrivateKey
import io.iohk.atala.prism.walletsdk.domain.models.VerifiableCredential
import io.iohk.atala.prism.walletsdk.domain.models.W3CVerifiableCredential
import io.iohk.atala.prism.walletsdk.domain.models.getKeyCurveByNameAndIndex
import io.iohk.atala.prism.walletsdk.pluto.PrismPlutoDb
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import ioiohkatalaprismwalletsdkpluto.data.DID as DIDDB
import ioiohkatalaprismwalletsdkpluto.data.DIDPair as DIDPairDB
import ioiohkatalaprismwalletsdkpluto.data.Mediator as MediatorDB
import ioiohkatalaprismwalletsdkpluto.data.Message as MessageDB
import ioiohkatalaprismwalletsdkpluto.data.PrivateKey as PrivateKeyDB
import ioiohkatalaprismwalletsdkpluto.data.VerifiableCredential as VerifiableCredentialDB

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

    override fun storePeerDID(did: DID, privateKeys: Array<PrivateKey>) {
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

    override fun storePrivateKeys(privateKey: PrivateKey, did: DID, keyPathIndex: Int) {
        db.privateKeyQueries.insert(
            PrivateKeyDB(
                UUID.randomUUID4().toString(),
                privateKey.keyCurve.curve.value,
                privateKey.value.toString(),
                keyPathIndex,
                did.methodId
            )
        )
    }

    override fun storeMessages(messages: Array<Message>) {
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

    override fun getAllPrismDIDs(): Array<PrismDIDInfo> {
        return db.dIDQueries
            .fetchAllPrismDID()
            .executeAsList()
            .map {
                PrismDIDInfo(DID(it.did), it.keyPathIndex, it.alias)
            }.toTypedArray()
    }

    override fun getDIDInfoByDID(did: DID): PrismDIDInfo? {
        val didInfo = try {
            db.dIDQueries
                .fetchDIDInfoByDID(did.toString())
                .executeAsOne()
        } catch (e: NullPointerException) {
            null
        } ?: return null

        return PrismDIDInfo(DID(didInfo.did), didInfo.keyPathIndex, didInfo.alias)
    }

    override fun getDIDInfoByAlias(alias: String): Array<PrismDIDInfo> {
        return db.dIDQueries
            .fetchDIDInfoByAlias(alias)
            .executeAsList()
            .map { PrismDIDInfo(DID(it.did), it.keyPathIndex, it.alias) }
            .toTypedArray()
    }

    override fun getDIDPrivateKeysByDID(did: DID): Array<PrivateKey>? {
        val privateKeys = try {
            db.privateKeyQueries
                .fetchPrivateKeyByDID(did.toString())
                .executeAsList()
        } catch (e: IllegalStateException) {
            null
        } ?: return null

        return privateKeys.map {
            PrivateKey(
                getKeyCurveByNameAndIndex(
                    it.curve,
                    it.keyPathIndex
                ),
                byteArrayOf()
            )
        }
            .toTypedArray()
    }

    override fun getPrismDIDKeyPathIndex(did: DID): Int? {
        val did = try {
            db.privateKeyQueries.fetchKeyPathIndexByDID(did.methodId)
                .executeAsOne()
        } catch (e: NullPointerException) {
            null
        } ?: return null

        return did.keyPathIndex
    }

    override fun getPrismLastKeyPathIndex(): Int {
        return db.privateKeyQueries.fetchLastkeyPathIndex()
            .executeAsOne()
            .keyPathIndex ?: 0
    }

    override fun getAllPeerDIDs(): Array<PeerDID> {
        return db.dIDQueries.fetchAllPeerDID()
            .executeAsList()
            .groupBy { it.did }
            .map {
                val privateKeyList = it.value.map { data ->
                    PrivateKey(
                        getKeyCurveByNameAndIndex(data.curve, data.keyPathIndex),
                        byteArrayOf()
                    )
                }.toTypedArray()
                PeerDID(DID(it.key), privateKeyList)
            }.toTypedArray()
    }

    override fun getAllDidPairs(): Array<DIDPair> {
        return db.dIDPairQueries.fetchAllDIDPairs()
            .executeAsList()
            .map { DIDPair(DID(it.hostDID), DID(it.receiverDID), it.name) }
            .toTypedArray()
    }

    override fun getPairByDID(did: DID): DIDPair? {
        val didPair = try {
            db.dIDPairQueries.fetchDIDPairByDID(did.toString())
                .executeAsOne()
        } catch (e: NullPointerException) {
            null
        } ?: return null

        return DIDPair(DID(didPair.hostDID), DID(didPair.receiverDID), didPair.name)
    }

    override fun getPairByName(name: String): DIDPair? {
        val didPair = try {
            db.dIDPairQueries.fetchDIDPairByName(name)
                .executeAsOne()
        } catch (e: NullPointerException) {
            null
        } ?: return null

        return DIDPair(DID(didPair.hostDID), DID(didPair.receiverDID), didPair.name)
    }

    override fun getAllMessages(): Array<Message> {
        return db.messageQueries.fetchAllMessages()
            .executeAsList()
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
            }.toTypedArray()
    }

    override fun getAllMessages(did: DID): Array<Message> {
        return getAllMessages(did, did)
    }

    override fun getAllMessages(from: DID, to: DID): Array<Message> {
        return db.messageQueries.fetchAllMessagesFromTo(from.toString(), to.toString())
            .executeAsList()
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
            }.toTypedArray()
    }

    override fun getAllMessagesSent(): Array<Message> {
        return db.messageQueries.fetchAllSentMessages()
            .executeAsList()
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
            }.toTypedArray()
    }

    override fun getAllMessagesReceived(): Array<Message> {
        return db.messageQueries.fetchAllReceivedMessages()
            .executeAsList()
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
            }.toTypedArray()
    }

    override fun getAllMessagesSentTo(did: DID): Array<Message> {
        return db.messageQueries.fetchAllMessagesSentTo(did.toString())
            .executeAsList()
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
            }.toTypedArray()
    }

    override fun getAllMessagesReceivedFrom(did: DID): Array<Message> {
        return db.messageQueries.fetchAllMessagesReceivedFrom(did.toString())
            .executeAsList()
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
            }.toTypedArray()
    }

    override fun getAllMessagesOfType(type: String, relatedWithDID: DID?): Array<Message> {
        return db.messageQueries.fetchAllMessagesOfType(type, relatedWithDID.toString(), relatedWithDID.toString())
            .executeAsList()
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
            }.toTypedArray()
    }

    override fun getMessage(id: String): Message? {
        val message = try {
            db.messageQueries.fetchMessageById(id)
                .executeAsOne()
        } catch (e: NullPointerException) {
            null
        } ?: return null

        val messageDb = Json.decodeFromString<Message>(message.dataJson)

        return Message(
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

    override fun getAllMediators(): Array<MediatorDID> {
        return db.mediatorQueries.fetchAllMediators()
            .executeAsList()
            .map {
                MediatorDID(
                    it.id,
                    DID(it.MediatorDID),
                    DID(it.HostDID),
                    DID(it.RoutingDID)
                )
            }.toTypedArray()
    }

    // TODO: Define how to form JWTVerifiableCredential and W3CVerifiableCredential
    override fun getAllCredentials(): Array<VerifiableCredential> {
        return db.verifiableCredentialQueries.fetchAllCredentials()
            .executeAsList()
            .map {
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
            }.toTypedArray()
    }
}
