package io.iohk.atala.prism.walletsdk.pluto

import io.iohk.atala.prism.apollo.uuid.UUID
import io.iohk.atala.prism.walletsdk.domain.buildingBlocks.Pluto
import io.iohk.atala.prism.walletsdk.domain.models.CredentialType
import io.iohk.atala.prism.walletsdk.domain.models.DID
import io.iohk.atala.prism.walletsdk.domain.models.DIDPair
import io.iohk.atala.prism.walletsdk.domain.models.JWTVerifiableCredential
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

    override fun storePrismDID(did: DID, keyPathIndex: Int, alias: String?) {
        getInstance().dIDQueries.insert(DIDDB(did.toString(), did.method, did.methodId, did.schema, alias))
    }

    override fun storePeerDID(did: DID, privateKeys: Array<PrivateKey>) {
        getInstance().dIDQueries.insert(DIDDB(did.toString(), did.method, did.methodId, did.schema, null))
        privateKeys.map { privateKey ->
            getInstance().privateKeyQueries.insert(
                PrivateKeyDB(
                    UUID.randomUUID4().toString(),
                    privateKey.keyCurve.curve.value,
                    privateKey.value.toString(),
                    privateKey.keyCurve.index,
                    did.methodId,
                ),
            )
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

    override fun storePrivateKeys(privateKey: PrivateKey, did: DID, keyPathIndex: Int) {
        getInstance().privateKeyQueries.insert(
            PrivateKeyDB(
                UUID.randomUUID4().toString(),
                privateKey.keyCurve.curve.value,
                privateKey.value.toString(),
                keyPathIndex,
                did.methodId,
            ),
        )
    }

    override fun storeMessages(messages: Array<Message>) {
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

    override fun getAllPrismDIDs(): Array<PrismDIDInfo> {
        return getInstance().dIDQueries
            .fetchAllPrismDID()
            .executeAsList()
            .map {
                PrismDIDInfo(DID(it.did), it.keyPathIndex, it.alias)
            }.toTypedArray()
    }

    override fun getDIDInfoByDID(did: DID): PrismDIDInfo? {
        val didInfo = try {
            getInstance().dIDQueries
                .fetchDIDInfoByDID(did.toString())
                .executeAsOne()
        } catch (e: NullPointerException) {
            null
        } ?: return null

        return PrismDIDInfo(DID(didInfo.did), didInfo.keyPathIndex, didInfo.alias)
    }

    override fun getDIDInfoByAlias(alias: String): Array<PrismDIDInfo> {
        return getInstance().dIDQueries
            .fetchDIDInfoByAlias(alias)
            .executeAsList()
            .map { PrismDIDInfo(DID(it.did), it.keyPathIndex, it.alias) }
            .toTypedArray()
    }

    override fun getDIDPrivateKeysByDID(did: DID): Array<PrivateKey>? {
        val privateKeys = try {
            getInstance().privateKeyQueries
                .fetchPrivateKeyByDID(did.toString())
                .executeAsList()
        } catch (e: IllegalStateException) {
            null
        } ?: return null

        return privateKeys.map {
            PrivateKey(
                getKeyCurveByNameAndIndex(
                    it.curve,
                    it.keyPathIndex,
                ),
                byteArrayOf(),
            )
        }
            .toTypedArray()
    }

    override fun getPrismDIDKeyPathIndex(did: DID): Int? {
        val did = try {
            getInstance().privateKeyQueries.fetchKeyPathIndexByDID(did.methodId)
                .executeAsOne()
        } catch (e: NullPointerException) {
            null
        } ?: return null

        return did.keyPathIndex
    }

    override fun getPrismLastKeyPathIndex(): Int {
        return getInstance().privateKeyQueries.fetchLastkeyPathIndex()
            .executeAsOne()
            .keyPathIndex ?: 0
    }

    override fun getAllPeerDIDs(): Array<PeerDID> {
        return getInstance().dIDQueries.fetchAllPeerDID()
            .executeAsList()
            .groupBy { it.did }
            .map {
                val privateKeyList = it.value.map { data ->
                    PrivateKey(
                        getKeyCurveByNameAndIndex(data.curve, data.keyPathIndex),
                        byteArrayOf(),
                    )
                }.toTypedArray()
                PeerDID(DID(it.key), privateKeyList)
            }.toTypedArray()
    }

    override fun getAllDidPairs(): Array<DIDPair> {
        return getInstance().dIDPairQueries.fetchAllDIDPairs()
            .executeAsList()
            .map { DIDPair(DID(it.hostDID), DID(it.receiverDID), it.name) }
            .toTypedArray()
    }

    override fun getPairByDID(did: DID): DIDPair? {
        val didPair = try {
            getInstance().dIDPairQueries.fetchDIDPairByDID(did.toString())
                .executeAsOne()
        } catch (e: NullPointerException) {
            null
        } ?: return null

        return DIDPair(DID(didPair.hostDID), DID(didPair.receiverDID), didPair.name)
    }

    override fun getPairByName(name: String): DIDPair? {
        val didPair = try {
            getInstance().dIDPairQueries.fetchDIDPairByName(name)
                .executeAsOne()
        } catch (e: NullPointerException) {
            null
        } ?: return null

        return DIDPair(DID(didPair.hostDID), DID(didPair.receiverDID), didPair.name)
    }

    override fun getAllMessages(): Array<Message> {
        return getInstance().messageQueries.fetchAllMessages()
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
        return getInstance().messageQueries.fetchAllMessagesFromTo(from.toString(), to.toString())
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
        return getInstance().messageQueries.fetchAllSentMessages()
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
        return getInstance().messageQueries.fetchAllReceivedMessages()
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
        return getInstance().messageQueries.fetchAllMessagesSentTo(did.toString())
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
        return getInstance().messageQueries.fetchAllMessagesReceivedFrom(did.toString())
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
        return getInstance().messageQueries.fetchAllMessagesOfType(type, relatedWithDID.toString(), relatedWithDID.toString())
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
            getInstance().messageQueries.fetchMessageById(id)
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

    override fun getAllMediators(): Array<Mediator> {
        return getInstance().mediatorQueries.fetchAllMediators()
            .executeAsList()
            .map {
                Mediator(
                    it.id,
                    DID(it.MediatorDID),
                    DID(it.HostDID),
                    DID(it.RoutingDID),
                )
            }.toTypedArray()
    }

    // TODO: Define how to form JWTVerifiableCredential and W3CVerifiableCredential
    override fun getAllCredentials(): Array<VerifiableCredential> {
        return getInstance().verifiableCredentialQueries.fetchAllCredentials()
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
                            verifiableCredential.aud,
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
                            verifiableCredential.aud,
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
                            verifiableCredential.aud,
                        )
                }
            }.toTypedArray()
    }
}
