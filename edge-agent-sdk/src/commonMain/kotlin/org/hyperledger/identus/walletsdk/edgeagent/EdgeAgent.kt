@file:Suppress("ktlint:standard:import-ordering")

package org.hyperledger.identus.walletsdk.edgeagent

import anoncreds_uniffi.CredentialOffer
import anoncreds_uniffi.CredentialRequestMetadata
import anoncreds_uniffi.createLinkSecret
import com.nimbusds.jose.EncryptionMethod
import com.nimbusds.jose.JWEAlgorithm
import com.nimbusds.jose.JWEDecrypter
import com.nimbusds.jose.JWEEncrypter
import com.nimbusds.jose.JWEHeader
import com.nimbusds.jose.JWEObject
import com.nimbusds.jose.Payload
import com.nimbusds.jose.crypto.X25519Decrypter
import com.nimbusds.jose.crypto.X25519Encrypter
import com.nimbusds.jose.jwk.OctetKeyPair
import com.nimbusds.jose.util.Base64URL
import eu.europa.ec.eudi.sdjwt.vc.SD_JWT_VC_TYPE
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.Url
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.date.getTimeMillis
import java.net.UnknownHostException
import java.security.SecureRandom
import java.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.hyperledger.identus.apollo.base64.base64UrlEncoded
import org.hyperledger.identus.walletsdk.apollo.utils.Ed25519KeyPair
import org.hyperledger.identus.walletsdk.apollo.utils.Ed25519PrivateKey
import org.hyperledger.identus.walletsdk.apollo.utils.Secp256k1KeyPair
import org.hyperledger.identus.walletsdk.apollo.utils.Secp256k1PrivateKey
import org.hyperledger.identus.walletsdk.apollo.utils.X25519KeyPair
import org.hyperledger.identus.walletsdk.apollo.utils.X25519PrivateKey
import org.hyperledger.identus.walletsdk.domain.DIDCOMM_MESSAGING
import org.hyperledger.identus.walletsdk.domain.buildingblocks.Apollo
import org.hyperledger.identus.walletsdk.domain.buildingblocks.Castor
import org.hyperledger.identus.walletsdk.domain.buildingblocks.Mercury
import org.hyperledger.identus.walletsdk.domain.buildingblocks.Pluto
import org.hyperledger.identus.walletsdk.domain.buildingblocks.Pollux
import org.hyperledger.identus.walletsdk.domain.models.Api
import org.hyperledger.identus.walletsdk.domain.models.ApiImpl
import org.hyperledger.identus.walletsdk.domain.models.ApolloError
import org.hyperledger.identus.walletsdk.domain.models.AttachmentData
import org.hyperledger.identus.walletsdk.domain.models.AttachmentData.AttachmentBase64

import org.hyperledger.identus.walletsdk.domain.models.AttachmentDescriptor
import org.hyperledger.identus.walletsdk.domain.models.Credential
import org.hyperledger.identus.walletsdk.domain.models.CredentialOperationsOptions
import org.hyperledger.identus.walletsdk.domain.models.CredentialType
import org.hyperledger.identus.walletsdk.domain.models.Curve
import org.hyperledger.identus.walletsdk.domain.models.DID
import org.hyperledger.identus.walletsdk.domain.models.DIDDocument
import org.hyperledger.identus.walletsdk.domain.models.DIDPair
import org.hyperledger.identus.walletsdk.domain.models.KeyCurve
import org.hyperledger.identus.walletsdk.domain.models.Message
import org.hyperledger.identus.walletsdk.domain.models.PeerDID
import org.hyperledger.identus.walletsdk.domain.models.PolluxError
import org.hyperledger.identus.walletsdk.domain.models.PresentationClaims
import org.hyperledger.identus.walletsdk.domain.models.PrismDIDInfo
import org.hyperledger.identus.walletsdk.domain.models.ProvableCredential
import org.hyperledger.identus.walletsdk.domain.models.Seed
import org.hyperledger.identus.walletsdk.domain.models.Signature
import org.hyperledger.identus.walletsdk.domain.models.UnknownError
import org.hyperledger.identus.walletsdk.domain.models.httpClient
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.CurveKey
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.DerivationPathKey
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.KeyPair
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.KeyTypes
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.PrivateKey
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.SeedKey
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.StorableKey
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.StorablePrivateKey
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.TypeKey
import org.hyperledger.identus.walletsdk.edgeagent.helpers.AgentOptions
import org.hyperledger.identus.walletsdk.edgeagent.mediation.BasicMediatorHandler
import org.hyperledger.identus.walletsdk.edgeagent.mediation.MediationHandler
import org.hyperledger.identus.walletsdk.edgeagent.models.ConnectionlessMessageData
import org.hyperledger.identus.walletsdk.edgeagent.protocols.ProtocolType
import org.hyperledger.identus.walletsdk.edgeagent.protocols.connection.DIDCommConnectionRunner
import org.hyperledger.identus.walletsdk.edgeagent.protocols.findProtocolTypeByValue
import org.hyperledger.identus.walletsdk.edgeagent.protocols.issueCredential.IssueCredential
import org.hyperledger.identus.walletsdk.edgeagent.protocols.issueCredential.OfferCredential
import org.hyperledger.identus.walletsdk.edgeagent.protocols.issueCredential.RequestCredential
import org.hyperledger.identus.walletsdk.edgeagent.protocols.outOfBand.ConnectionlessCredentialOffer
import org.hyperledger.identus.walletsdk.edgeagent.protocols.outOfBand.ConnectionlessRequestPresentation
import org.hyperledger.identus.walletsdk.edgeagent.protocols.outOfBand.DIDCommInvitationRunner
import org.hyperledger.identus.walletsdk.edgeagent.protocols.outOfBand.InvitationType
import org.hyperledger.identus.walletsdk.edgeagent.protocols.outOfBand.OutOfBandInvitation
import org.hyperledger.identus.walletsdk.edgeagent.protocols.outOfBand.PrismOnboardingInvitation
import org.hyperledger.identus.walletsdk.edgeagent.protocols.proofOfPresentation.AnoncredsPresentationOptions
import org.hyperledger.identus.walletsdk.edgeagent.protocols.proofOfPresentation.JWTPresentationOptions
import org.hyperledger.identus.walletsdk.edgeagent.protocols.proofOfPresentation.Presentation
import org.hyperledger.identus.walletsdk.edgeagent.protocols.proofOfPresentation.PresentationSubmissionOptionsAnoncreds
import org.hyperledger.identus.walletsdk.edgeagent.protocols.proofOfPresentation.PresentationSubmissionOptionsJWT
import org.hyperledger.identus.walletsdk.edgeagent.protocols.proofOfPresentation.RequestPresentation
import org.hyperledger.identus.walletsdk.logger.LogComponent
import org.hyperledger.identus.walletsdk.logger.Metadata
import org.hyperledger.identus.walletsdk.logger.Logger
import org.hyperledger.identus.walletsdk.logger.LoggerImpl
import org.hyperledger.identus.walletsdk.pluto.PlutoBackupTask
import org.hyperledger.identus.walletsdk.pluto.PlutoRestoreTask
import org.hyperledger.identus.walletsdk.pluto.models.backup.BackupV0_0_1
import org.hyperledger.identus.walletsdk.pollux.models.AnoncredsPresentationDefinitionRequest
import org.hyperledger.identus.walletsdk.pollux.models.CredentialRequestMeta
import org.hyperledger.identus.walletsdk.pollux.models.JWTPresentationDefinitionRequest
import org.kotlincrypto.hash.sha2.SHA256

/**
 * Check if the passed URL is valid or not.
 * @param str string to check its URL validity
 * @return [Url] if valid, null if not valid
 */
private fun Url.Companion.parse(str: String): Url? {
    return try {
        Url(str)
    } catch (e: Throwable) {
        null
    }
}

/**
 * EdgeAgent class is responsible for handling the connection to other agents in the network using a provided Mediator
 * Service Endpoint and seed data.
 */
open class EdgeAgent {
    var state: State = State.STOPPED
        private set(value) {
            field = value
            edgeAgentScope.launch {
                flowState.emit(value)
            }
        }
    val seed: Seed
    val apollo: Apollo
    val castor: Castor
    val pluto: Pluto
    val mercury: Mercury
    val pollux: Pollux
    val flowState = MutableSharedFlow<State>()

    private val edgeAgentScope: CoroutineScope = CoroutineScope(Dispatchers.Default)
    private val api: Api
    internal var connectionManager: ConnectionManager
    private var logger: Logger
    private val agentOptions: AgentOptions

    /**
     * Initializes the EdgeAgent with the given dependencies.
     *
     * @param apollo The Apollo instance used by the EdgeAgent.
     * @param castor The Castor instance used by the EdgeAgent.
     * @param pluto The Pluto instance used by the EdgeAgent.
     * @param mercury The Mercury instance used by the EdgeAgent.
     * @param pollux The Pollux instance used by the EdgeAgent.
     * @param connectionManager The ConnectionManager instance used by the EdgeAgent.
     * @param seed An optional Seed instance used by the Apollo if provided, otherwise a random seed will be used.
     * @param api An optional Api instance used by the EdgeAgent if provided, otherwise a default ApiImpl will be used.
     * @param logger An optional PrismLogger instance used by the EdgeAgent if provided, otherwise a PrismLoggerImpl with
     *               LogComponent.PRISM_AGENT will be used.
     * @param agentOptions Options to configure certain features with in the prism agent.
     */
    @JvmOverloads
    constructor(
        apollo: Apollo,
        castor: Castor,
        pluto: Pluto,
        mercury: Mercury,
        pollux: Pollux,
        connectionManager: ConnectionManager,
        seed: Seed?,
        api: Api?,
        logger: Logger = LoggerImpl(LogComponent.EDGE_AGENT),
        agentOptions: AgentOptions = AgentOptions()
    ) {
        edgeAgentScope.launch {
            flowState.emit(State.STOPPED)
        }
        this.apollo = apollo
        this.castor = castor
        this.pluto = pluto
        this.mercury = mercury
        this.pollux = pollux
        this.connectionManager = connectionManager
        this.seed = seed ?: apollo.createRandomSeed().seed
        this.api = api ?: ApiImpl(
            httpClient {
                install(ContentNegotiation) {
                    json(
                        Json {
                            ignoreUnknownKeys = true
                            prettyPrint = true
                            isLenient = true
                        }
                    )
                }
            }
        )
        this.logger = logger
        this.agentOptions = agentOptions
    }

    /**
     * Initializes the EdgeAgent constructor.
     *
     * @param apollo The instance of Apollo.
     * @param castor The instance of Castor.
     * @param pluto The instance of Pluto.
     * @param mercury The instance of Mercury.
     * @param pollux The instance of Pollux.
     * @param seed The seed value for random generation. Default is null.
     * @param api The instance of the API. Default is null.
     * @param mediatorHandler The mediator handler.
     * @param logger The logger for EdgeAgent. Default is PrismLoggerImpl with LogComponent.PRISM_AGENT.
     * @param agentOptions Options to configure certain features with in the prism agent.
     */
    @JvmOverloads
    constructor(
        apollo: Apollo,
        castor: Castor,
        pluto: Pluto,
        mercury: Mercury,
        pollux: Pollux,
        seed: Seed? = null,
        api: Api? = null,
        mediatorHandler: MediationHandler,
        logger: Logger = LoggerImpl(LogComponent.EDGE_AGENT),
        agentOptions: AgentOptions = AgentOptions()
    ) {
        edgeAgentScope.launch {
            flowState.emit(State.STOPPED)
        }
        this.apollo = apollo
        this.castor = castor
        this.pluto = pluto
        this.mercury = mercury
        this.pollux = pollux
        this.seed = seed ?: apollo.createRandomSeed().seed
        this.api = api ?: ApiImpl(
            httpClient {
                install(ContentNegotiation) {
                    json(
                        Json {
                            ignoreUnknownKeys = true
                            prettyPrint = true
                            isLenient = true
                        }
                    )
                }
            }
        )
        this.logger = logger
        this.agentOptions = agentOptions
        // Pairing will be removed in the future
        this.connectionManager =
            ConnectionManagerImpl(
                mercury,
                castor,
                pluto,
                mediatorHandler,
                mutableListOf(),
                pollux,
                agentOptions.experiments.liveMode
            )
    }

    init {
        flowState.onSubscription {
            if (flowState.subscriptionCount.value <= 0) {
                state = State.STOPPED
            } else {
                throw EdgeAgentError.EdgeAgentStateAcceptOnlyOneObserver()
            }
        }
    }

    // Prism agent actions
    /**
     * Start the [EdgeAgent] and Mediator services.
     *
     * @throws [EdgeAgentError.MediationRequestFailedError] failed to connect to mediator.
     * @throws [UnknownHostException] if unable to connect to the mediator.
     */
    @Throws(EdgeAgentError.MediationRequestFailedError::class, UnknownHostException::class)
    suspend fun start() {
        if (state != State.STOPPED) {
            return
        }
        logger.info(message = "Starting agent")
        state = State.STARTING
        try {
            connectionManager.startMediator()
        } catch (error: EdgeAgentError.NoMediatorAvailableError) {
            logger.info(message = "Start accept DIDComm invitation")
            try {
                val hostDID = createNewPeerDID(updateMediator = false)

                logger.info(message = "Sending DIDComm connection message")
                connectionManager.registerMediator(hostDID)
            } catch (error: UnknownHostException) {
                state = State.STOPPED
                throw error
            }
        }
        if (connectionManager.mediationHandler.mediator != null) {
            state = State.RUNNING
            logger.info(
                message = "Mediation Achieved",
                metadata = arrayOf(
                    Metadata.PublicMetadata(
                        key = "Routing DID",
                        value = connectionManager.mediationHandler.mediatorDID.toString()
                    )
                )
            )
            logger.info(message = "Agent running")
        } else {
            state = State.STOPPED
            throw EdgeAgentError.MediationRequestFailedError()
        }
    }

    /**
     * Stops the [EdgeAgent].
     * The function sets the state of [EdgeAgent] to [State.STOPPING].
     * All ongoing events that was created by the [EdgeAgent] are stopped.
     * After all the events are stopped the state of the [EdgeAgent] is set to [State.STOPPED].
     */
    fun stop() {
        if (state != State.RUNNING) {
            return
        }
        logger.info(message = "Stoping agent")
        state = State.STOPPING
        state = State.STOPPED
        logger.info(message = "Agent not running")
    }

    // DID Higher Functions
    /**
     * This method create a new Prism DID, that can be used to identify the agent and interact with other agents.
     *
     * @param keyPathIndex key path index used to identify the DID.
     * @param alias An alias that can be used to identify the DID.
     * @param services an array of services associated to the DID.
     * @return The new created [DID]
     */
    @JvmOverloads
    suspend fun createNewPrismDID(
        keyPathIndex: Int? = null,
        alias: String? = null,
        services: Array<DIDDocument.Service> = emptyArray()
    ): DID {
        val index = keyPathIndex ?: (pluto.getPrismLastKeyPathIndex().first() + 1)
        val keyPair = Secp256k1KeyPair.generateKeyPair(seed, KeyCurve(Curve.SECP256K1, index))
        val did = castor.createPrismDID(masterPublicKey = keyPair.publicKey, services = services)
        registerPrismDID(did, index, alias, keyPair.privateKey)
        return did
    }

    /**
     * This function receives a Prism DID and its information and stores it into the local database.
     *
     * @param did The DID to be stored
     * @param keyPathIndex The index associated with the PrivateKey
     * @param alias The alias associated with the DID if any
     * @param privateKey The private key used to create the PrismDID
     */
    private fun registerPrismDID(
        did: DID,
        keyPathIndex: Int,
        alias: String? = null,
        privateKey: PrivateKey
    ) {
        pluto.storePrismDIDAndPrivateKeys(
            did = did,
            keyPathIndex = keyPathIndex,
            alias = alias ?: did.alias,
            listOf(privateKey as StorableKey)
        )
    }

    /**
     * This function creates a new Peer DID, stores it in pluto database and updates the mediator if requested.
     *
     * @param services The services associated to the new DID.
     * @param updateMediator Indicates if the new DID should be added to the mediator's list. It will as well add the
     * mediator service.
     * @return A new [DID].
     */
    @JvmOverloads
    open suspend fun createNewPeerDID(
        services: Array<DIDDocument.Service> = emptyArray(),
        updateMediator: Boolean
    ): DID {
        val keyAgreementKeyPair = X25519KeyPair.generateKeyPair()
        val authenticationKeyPair = Ed25519KeyPair.generateKeyPair()

        var tmpServices = services
        if (updateMediator) {
            tmpServices = tmpServices.plus(
                DIDDocument.Service(
                    id = DIDCOMM1,
                    type = arrayOf(
                        DIDCOMM_MESSAGING
                    ),
                    serviceEndpoint = DIDDocument.ServiceEndpoint(
                        uri = connectionManager.mediationHandler.mediator?.routingDID.toString()
                    )
                )
            )
        }

        val did = castor.createPeerDID(
            arrayOf(keyAgreementKeyPair, authenticationKeyPair),
            services = tmpServices
        )
        registerPeerDID(
            did,
            keyAgreementKeyPair,
            authenticationKeyPair,
            updateMediator
        )
        return did
    }

    /**
     * Registers a peer DID with the specified DID and private keys.
     *
     * @param did The DID (Decentralized Identifier) to register.
     * @param keyAgreementKeyPair The key pair used for key agreement.
     * @param authenticationKeyPair The key pair used for authentication.
     * @param updateMediator Whether to update the mediator with the DID.
     */
    suspend fun registerPeerDID(
        did: DID,
        keyAgreementKeyPair: KeyPair,
        authenticationKeyPair: KeyPair,
        updateMediator: Boolean
    ) {
        if (updateMediator) {
            updateMediatorWithDID(did)
        }
        // The next logic is a bit tricky, so it's not forgotten this is a reminder.
        // The next few lines are needed because of DIDComm library, the library will need
        // to get the secret(private key) that is pair of the public key within the DIDPeer Document
        // to this end the library will give you the id of the public key that is `did:{method}:{methodId}#ecnumbasis`.
        // So the code below after the did is created, it will retrieve the document and
        // store the private keys with the corresponding `id` of the one created on the document.
        // So when the secret resolver asks for the secret we can identify it.
        val document = castor.resolveDID(did.toString())

        val listOfVerificationMethods: MutableList<DIDDocument.VerificationMethod> =
            mutableListOf()
        document.coreProperties.forEach {
            if (it is DIDDocument.Authentication) {
                listOfVerificationMethods.addAll(it.verificationMethods)
            }
            if (it is DIDDocument.KeyAgreement) {
                listOfVerificationMethods.addAll(it.verificationMethods)
            }
        }
        val verificationMethods =
            DIDDocument.VerificationMethods(listOfVerificationMethods.toTypedArray())

        verificationMethods.values.forEach {
            if (it.type.contains("X25519")) {
                pluto.storePrivateKeys(
                    keyAgreementKeyPair.privateKey as StorableKey,
                    did,
                    0,
                    it.id.toString()
                )
            } else if (it.type.contains("Ed25519")) {
                pluto.storePrivateKeys(
                    authenticationKeyPair.privateKey as StorableKey,
                    did,
                    0,
                    it.id.toString()
                )
            }
        }

        pluto.storePeerDID(
            did = did
        )
    }

    /**
     * Updates the mediator with the specified DID by updating the key list with the given DID.
     *
     * @param did The DID to update the mediator with.
     */
    suspend fun updateMediatorWithDID(did: DID) {
        connectionManager.mediationHandler.updateKeyListWithDIDs(arrayOf(did))
    }

    fun setupMediatorHandler(mediatorHandler: MediationHandler) {
        stop()
        this.connectionManager =
            ConnectionManagerImpl(
                mercury,
                castor,
                pluto,
                mediatorHandler,
                mutableListOf(),
                pollux,
                agentOptions.experiments.liveMode
            )
    }

    /**
     * Sets up a mediator DID for communication with the specified DID.
     *
     * @param did The DID of the mediator to set up.
     */
    fun setupMediatorDID(did: DID) {
        val tmpMediatorHandler = BasicMediatorHandler(
            mediatorDID = did,
            mercury = mercury,
            store = BasicMediatorHandler.PlutoMediatorRepositoryImpl(pluto)
        )
        setupMediatorHandler(tmpMediatorHandler)
    }

    /**
     * This method fetches a DIDInfo from local storage.
     *
     * @param did The DID to fetch the info for
     * @return A PrismDIDInfo if existent, null otherwise
     */
    suspend fun getDIDInfo(did: DID): PrismDIDInfo? {
        return pluto.getDIDInfoByDID(did)
            .first()
    }

    /**
     * This method registers a DID pair into the local database.
     *
     * @param pair The DIDPair to be stored
     */
    fun registerDIDPair(pair: DIDPair) {
        pluto.storeDIDPair(pair.holder, pair.receiver, pair.name ?: "")
    }

    /**
     * This method returns all DID pairs
     *
     * @return A list of the store DID pair
     */
    suspend fun getAllDIDPairs(): List<DIDPair> {
        return pluto.getAllDidPairs().first()
    }

    /**
     * This method returns all registered PeerDIDs.
     *
     * @return A list of the stored PeerDIDs
     */
    suspend fun getAllRegisteredPeerDIDs(): List<PeerDID> {
        return pluto.getAllPeerDIDs().first()
    }

    // Messages related actions

    /**
     * Sends a DIDComm message through HTTP using mercury and returns a message if this is returned immediately by the REST endpoint.
     *
     * @param message The message to be sent
     * @return The message sent if successful, null otherwise
     */
    suspend fun sendMessage(message: Message): Message? {
        return connectionManager.sendMessage(message)
    }

    // Credentials related actions

    /**
     * This function will use the provided DID to sign a given message.
     *
     * @param did The DID which will be used to sign the message.
     * @param message The message to be signed.
     * @return The signature of the message.
     */
    @Throws(EdgeAgentError.CannotFindDIDPrivateKey::class)
    suspend fun signWith(did: DID, message: ByteArray): Signature {
        val storablePrivateKey: StorablePrivateKey = pluto.getDIDPrivateKeysByDID(did).first().first()
            ?: throw EdgeAgentError.CannotFindDIDPrivateKey(did.toString())
        val privateKey = apollo.restorePrivateKey(storablePrivateKey.restorationIdentifier, storablePrivateKey.data)

        val returnByteArray: ByteArray =
            when (privateKey.getCurve()) {
                Curve.ED25519.value -> {
                    val ed = privateKey as Ed25519PrivateKey
                    ed.sign(message)
                }

                Curve.SECP256K1.value -> {
                    val secp = privateKey as Secp256k1PrivateKey
                    secp.sign(message)
                }

                else -> {
                    throw ApolloError.InvalidKeyCurve(
                        privateKey.getCurve()
                    )
                }
            }

        return Signature(returnByteArray)
    }

    /**
     * This function prepares a request credential from an offer given the subject DID.
     * @param did Subject DID.
     * @param offer Received offer credential.
     * @return Created request credential.
     * @throws [PolluxError.InvalidPrismDID] if there is a problem creating the request credential.
     * @throws [UnknownError.SomethingWentWrongError] if credential type is not supported
     **/
    @Throws(
        PolluxError.InvalidPrismDID::class,
        UnknownError.SomethingWentWrongError::class
    )
    suspend fun prepareRequestCredentialWithIssuer(
        did: DID,
        offer: OfferCredential
    ): RequestCredential {
        if (did.method != "prism") {
            throw PolluxError.InvalidPrismDID("DID method is not \"prism\" ")
        }

        return when (val type = pollux.extractCredentialFormatFromMessage(offer.attachments)) {
            CredentialType.JWT -> {
                val privateKeyKeyPath = pluto.getPrismDIDKeyPathIndex(did).first()

                val keyPair = Secp256k1KeyPair.generateKeyPair(
                    seed,
                    KeyCurve(Curve.SECP256K1, privateKeyKeyPath)
                )
                val offerDataString = offer.attachments.firstNotNullOf {
                    it.data.getDataAsJsonString()
                }
                val offerJsonObject = Json.parseToJsonElement(offerDataString).jsonObject
                val jwtString =
                    pollux.processCredentialRequestJWT(did, keyPair.privateKey, offerJsonObject)
                val attachmentDescriptor =
                    AttachmentDescriptor(
                        mediaType = ContentType.Application.Json.toString(),
                        format = CredentialType.JWT.type,
                        data = AttachmentBase64(jwtString.base64UrlEncoded)
                    )
                return RequestCredential(
                    from = offer.to,
                    to = offer.from,
                    thid = offer.thid,
                    body = RequestCredential.Body(
                        offer.body.goalCode,
                        offer.body.comment
                    ),
                    attachments = arrayOf(attachmentDescriptor)
                )
            }

            CredentialType.ANONCREDS_OFFER -> {
                val linkSecret = getLinkSecret()
                val offerDataString = offer.attachments.firstNotNullOf {
                    it.data.getDataAsJsonString()
                }
                if (offer.thid == null) {
                    throw EdgeAgentError.MissingOrNullFieldError("thid", "OfferCredential")
                }
                val anonOffer = CredentialOffer(offerDataString)
                val pair = pollux.processCredentialRequestAnoncreds(
                    did = did,
                    offer = anonOffer,
                    linkSecret = linkSecret,
                    linkSecretName = offer.thid
                )

                val credentialRequest = pair.first
                val credentialRequestMetadata = pair.second

                val json = credentialRequest.toJson()

                val metadata =
                    CredentialRequestMeta.fromCredentialRequestMetadata(credentialRequestMetadata)
                pluto.storeCredentialMetadata(offer.thid, metadata.linkSecretName, metadata.json)

                val attachmentDescriptor =
                    AttachmentDescriptor(
                        mediaType = ContentType.Application.Json.toString(),
                        format = CredentialType.ANONCREDS_REQUEST.type,
                        data = AttachmentBase64(json.base64UrlEncoded)
                    )

                RequestCredential(
                    from = offer.to,
                    to = offer.from,
                    thid = offer.thid,
                    body = RequestCredential.Body(
                        offer.body.goalCode,
                        offer.body.comment
                    ),
                    attachments = arrayOf(attachmentDescriptor)
                )
            }

            else -> {
                throw EdgeAgentError.InvalidCredentialError(type = type)
            }
        }
    }

    /**
     * This function parses an issued credential message, stores, and returns the verifiable credential.
     * @param message Issue credential Message.
     * @return The parsed verifiable credential.
     * @throws org.hyperledger.identus.walletsdk.domain.models.UnknownError.SomethingWentWrongError if there is a problem parsing the credential.
     */
    @Throws(UnknownError.SomethingWentWrongError::class)
    suspend fun processIssuedCredentialMessage(message: IssueCredential): Credential {
        val credentialType = pollux.extractCredentialFormatFromMessage(message.attachments)
        val attachmentData = message.attachments.firstOrNull()?.data?.getDataAsJsonString()

        return attachmentData?.let { credentialData ->
            if (message.thid != null) {
                val linkSecret = if (credentialType == CredentialType.ANONCREDS_ISSUE) {
                    getLinkSecret()
                } else {
                    null
                }
                val metadata = if (credentialType == CredentialType.ANONCREDS_ISSUE) {
                    val plutoMetadata =
                        pluto.getCredentialMetadata(message.thid).first()
                            ?: throw EdgeAgentError.InvalidCredentialMetadata()
                    CredentialRequestMetadata(
                        plutoMetadata.json
                    )
                } else {
                    null
                }

                val credential =
                    pollux.parseCredential(credentialData, credentialType, linkSecret, metadata)

                val storableCredential =
                    pollux.credentialToStorableCredential(
                        type = credentialType,
                        credential = credential
                    )
                pluto.storeCredential(storableCredential)
                return credential
            } else {
                throw UnknownError.SomethingWentWrongError("Thid should not be null")
            }
        } ?: throw EdgeAgentError.AttachmentTypeNotSupported()
    }

// Message Events
    /**
     * Start fetching the messages from the mediator.
     */
    @JvmOverloads
    fun startFetchingMessages(requestInterval: Int = 5) {
        connectionManager.startFetchingMessages(requestInterval)
    }

    /**
     * Stop fetching messages
     */
    fun stopFetchingMessages() {
        logger.info(message = "Stop streaming new unread messages")
        connectionManager.stopConnection()
    }

    /**
     * Handles the messages events and return a publisher of the messages.
     *
     * @return [Flow] of [Message].
     */
    fun handleMessagesEvents(): Flow<List<Message>> {
        return pluto.getAllMessages()
    }

    /**
     * Handles the received messages events and return a publisher of the messages.
     *
     * @return [Flow] of [Message].
     */
    fun handleReceivedMessagesEvents(): Flow<List<Message>> {
        return pluto.getAllMessagesReceived()
    }

// Invitation functionalities
    /**
     * Parses the given string as an invitation
     * @param str The string to parse
     * @return The parsed invitation [InvitationType]
     * @throws [EdgeAgentError.UnknownInvitationTypeError] if the invitation is not a valid Prism or OOB type
     * @throws [SerializationException] if Serialization failed
     */
    @Throws(EdgeAgentError.UnknownInvitationTypeError::class, SerializationException::class)
    suspend fun parseInvitation(str: String): InvitationType {
        Url.parse(str)?.let {
            val outOfBandInvitation = parseOOBInvitation(it)
            if (outOfBandInvitation.attachments.isNotEmpty()) {
                return connectionlessInvitation(outOfBandInvitation)
            } else {
                return outOfBandInvitation
            }
        } ?: run {
            try {
                val json = Json.decodeFromString<JsonObject>(str)
                val typeString: String = if (json.containsKey("type")) {
                    json["type"].toString().trim('"')
                } else {
                    ""
                }

                val invite: InvitationType = when (val type = findProtocolTypeByValue(typeString)) {
                    ProtocolType.PrismOnboarding -> parsePrismInvitation(str)
                    ProtocolType.Didcomminvitation -> parseOOBInvitation(str)
                    else ->
                        throw EdgeAgentError.UnknownInvitationTypeError(type.toString())
                }

                return invite
            } catch (e: SerializationException) {
                throw e
            }
        }
    }

    /**
     * Parses the given string as a Prism Onboarding invitation
     * @param str The string to parse
     * @return The parsed Prism Onboarding invitation
     * @throws [org.hyperledger.identus.walletsdk.domain.models.UnknownError.SomethingWentWrongError] if the string is not a valid Prism Onboarding invitation
     */
    @Throws(UnknownError.SomethingWentWrongError::class)
    private suspend fun parsePrismInvitation(str: String): PrismOnboardingInvitation {
        try {
            val prismOnboarding = PrismOnboardingInvitation.fromJsonString(str)
            val url = prismOnboarding.onboardEndpoint
            val did = createNewPeerDID(
                arrayOf(
                    DIDDocument.Service(
                        id = DIDCOMM1,
                        type = arrayOf(DIDCOMM_MESSAGING),
                        serviceEndpoint = DIDDocument.ServiceEndpoint(
                            uri = url,
                            accept = arrayOf(DIDCOMM_MESSAGING),
                            routingKeys = arrayOf()
                        )
                    )
                ),
                false
            )
            prismOnboarding.from = did
            return prismOnboarding
        } catch (e: Exception) {
            throw org.hyperledger.identus.walletsdk.domain.models.UnknownError.SomethingWentWrongError(
                e.message,
                e.cause
            )
        }
    }

    /**
     * Parses the given string as an Out-of-Band invitation
     * @param str The string to parse
     * @returns The parsed Out-of-Band invitation
     * @throws [SerializationException] if Serialization failed
     */
    @Throws(SerializationException::class)
    private fun parseOOBInvitation(str: String): OutOfBandInvitation {
        return try {
            Json.decodeFromString(str)
        } catch (ex: SerializationException) {
            throw ex
        }
    }

    /**
     * Parses the given URL as an Out-of-Band invitation
     * @param url The URL to parse
     * @return The parsed Out-of-Band invitation
     * @throws [EdgeAgentError.UnknownInvitationTypeError] if the URL is not a valid Out-of-Band invitation
     */
    private fun parseOOBInvitation(url: Url): OutOfBandInvitation {
        return DIDCommInvitationRunner(url).run()
    }

    /**
     * Accepts an Out-of-Band (DIDComm), verifies if it contains attachments or not. If it does contain attachments
     * means is a contactless request presentation. If it does not, it just creates a pair and establishes a connection.
     * @param invitation The Out-of-Band invitation to accept
     * @throws [EdgeAgentError.NoMediatorAvailableError] if there is no mediator available or other errors occur during the acceptance process
     */
    suspend fun acceptOutOfBandInvitation(invitation: OutOfBandInvitation) {
        val ownDID = createNewPeerDID(updateMediator = true)
        // Regular OOB invitation
        val pair = DIDCommConnectionRunner(invitation, pluto, ownDID, connectionManager).run()
        connectionManager.addConnection(pair)
    }

    /**
     * Accepts a Prism Onboarding invitation and performs the onboarding process
     * @param invitation The Prism Onboarding invitation to accept
     * @throws [EdgeAgentError.FailedToOnboardError] if failed to on board
     */
    @Throws(EdgeAgentError.FailedToOnboardError::class)
    suspend fun acceptInvitation(invitation: PrismOnboardingInvitation) {
        @Serializable
        data class SendDID(val did: String)

        val response = api.request(
            HttpMethod.Post.toString(),
            invitation.onboardEndpoint,
            arrayOf(),
            arrayOf(),
            SendDID(invitation.from.toString())
        )

        if (response.status != 200) {
            throw EdgeAgentError.FailedToOnboardError(response.status, response.jsonString)
        }
    }

    /**
     * This method returns a list of all the VerifiableCredentials stored locally.
     */
    fun getAllCredentials(): Flow<List<Credential>> {
        return pluto.getAllCredentials()
            .map { list ->
                list.map {
                    pollux.restoreCredential(it.restorationId, it.credentialData, it.revoked)
                }
            }
    }

// Proof related actions

    /**
     * This function creates a Presentation from a request verification.
     * @param request Request message received.
     * @param credential Verifiable Credential to present.
     * @return Presentation message prepared to send.
     * @throws EdgeAgentError if there is a problem creating the presentation.
     **/
    @Throws(PolluxError.InvalidPrismDID::class, EdgeAgentError.CredentialNotValidForPresentationRequest::class)
    suspend fun <T> preparePresentationForRequestProof(
        request: RequestPresentation,
        credential: T
    ): Presentation where T : Credential, T : ProvableCredential {
        val attachmentFormat = request.attachments.first().format ?: CredentialType.Unknown.type
        // Presentation request from agent
        var mediaType: String? = null
        var presentationString: String?
        when (attachmentFormat) {
            CredentialType.PRESENTATION_EXCHANGE_DEFINITIONS.type -> {
                // Presentation Exchange
                return handlePresentationDefinitionRequest(request, credential)
            }

            CredentialType.ANONCREDS_PROOF_REQUEST.type -> {
                val format = pollux.extractCredentialFormatFromMessage(request.attachments)
                if (format != CredentialType.ANONCREDS_PROOF_REQUEST) {
                    throw EdgeAgentError.InvalidCredentialFormatError(CredentialType.ANONCREDS_PROOF_REQUEST)
                }
                val requestData = request.attachments.firstNotNullOf {
                    it.data.getDataAsJsonString()
                }
                val linkSecret = getLinkSecret()
                try {
                    presentationString = credential.presentation(
                        requestData.encodeToByteArray(),
                        listOf(
                            CredentialOperationsOptions.LinkSecret("", linkSecret),
                            CredentialOperationsOptions.SchemaDownloader(api),
                            CredentialOperationsOptions.CredentialDefinitionDownloader(api)
                        )
                    )
                } catch (e: Exception) {
                    throw EdgeAgentError.CredentialNotValidForPresentationRequest()
                }
            }

            CredentialType.JWT.type -> {
                val subjectDID = credential.subject?.let {
                    DID(it)
                } ?: DID("")
                if (subjectDID.method != PRISM) {
                    throw PolluxError.InvalidPrismDID()
                }

                val privateKeyKeyPath = pluto.getPrismDIDKeyPathIndex(subjectDID).first()
                val keyPair =
                    Secp256k1KeyPair.generateKeyPair(
                        seed,
                        KeyCurve(Curve.SECP256K1, privateKeyKeyPath)
                    )
                val requestData = request.attachments.firstNotNullOf {
                    it.data.getDataAsJsonString()
                }
                try {
                    presentationString = credential.presentation(
                        requestData.encodeToByteArray(),
                        listOf(
                            CredentialOperationsOptions.SubjectDID(subjectDID),
                            CredentialOperationsOptions.ExportableKey(keyPair.privateKey)
                        )
                    )
                } catch (e: Exception) {
                    throw EdgeAgentError.CredentialNotValidForPresentationRequest()
                }
                mediaType = JWT_MEDIA_TYPE
            }

            CredentialType.SDJWT.type -> {
                val requestData = request.attachments.firstNotNullOf {
                    it.data.getDataAsJsonString()
                }
                try {
                    presentationString = credential.presentation(
                        requestData.encodeToByteArray(),
                        listOf(CredentialOperationsOptions.DisclosingClaims(listOf(credential.claims.toString())))
                    )
                } catch (e: Exception) {
                    throw EdgeAgentError.CredentialNotValidForPresentationRequest()
                }
                mediaType = SD_JWT_VC_TYPE
            }

            else -> {
                throw EdgeAgentError.InvalidCredentialError(credential)
            }
        }

        val attachmentDescriptor =
            AttachmentDescriptor(
                mediaType = mediaType,
                data = AttachmentBase64(presentationString.base64UrlEncoded)
            )

        val fromDID = request.to ?: createNewPeerDID(updateMediator = true)

        return Presentation(
            from = fromDID,
            to = request.from,
            thid = request.thid,
            body = Presentation.Body(request.body.goalCode, request.body.comment),
            attachments = arrayOf(attachmentDescriptor)
        )
    }

    suspend fun initiatePresentationRequest(
        type: CredentialType,
        toDID: DID,
        presentationClaims: PresentationClaims,
        domain: String? = null,
        challenge: String? = null
    ) {
        val didDocument = this.castor.resolveDID(toDID.toString())
        val newPeerDID = createNewPeerDID(services = didDocument.services, updateMediator = true)

        val presentationDefinitionRequest: String
        val attachmentDescriptor: AttachmentDescriptor
        when (type) {
            CredentialType.JWT -> {
                if (domain == null) {
                    throw EdgeAgentError.MissingOrNullFieldError("Domain", "initiatePresentationRequest parameters")
                }
                if (challenge == null) {
                    throw EdgeAgentError.MissingOrNullFieldError("Challenge", "initiatePresentationRequest parameters")
                }

                presentationDefinitionRequest = pollux.createPresentationDefinitionRequest(
                    type = type,
                    presentationClaims = presentationClaims,
                    options = JWTPresentationOptions(
                        jwt = arrayOf("ES256K"),
                        domain = domain,
                        challenge = challenge
                    )
                )
                attachmentDescriptor = AttachmentDescriptor(
                    mediaType = "application/json",
                    format = CredentialType.PRESENTATION_EXCHANGE_DEFINITIONS.type,
                    data = AttachmentBase64(presentationDefinitionRequest.base64UrlEncoded)
                )
            }

            CredentialType.ANONCREDS_PROOF_REQUEST -> {
                presentationDefinitionRequest = pollux.createPresentationDefinitionRequest(
                    type = type,
                    presentationClaims = presentationClaims,
                    options = AnoncredsPresentationOptions(
                        nonce = generateNumericNonce()
                    )
                )
                attachmentDescriptor = AttachmentDescriptor(
                    mediaType = "application/json",
                    format = CredentialType.PRESENTATION_EXCHANGE_DEFINITIONS.type,
                    data = AttachmentBase64(presentationDefinitionRequest.base64UrlEncoded)
                )
            }

            else -> {
                throw EdgeAgentError.CredentialTypeNotSupported(
                    type.type,
                    arrayOf(CredentialType.JWT.type, CredentialType.ANONCREDS_PROOF_REQUEST.type)
                )
            }
        }

        val presentationRequest = RequestPresentation(
            body = RequestPresentation.Body(proofTypes = emptyArray()),
            attachments = arrayOf(attachmentDescriptor),
            thid = UUID.randomUUID().toString(),
            from = newPeerDID,
            to = toDID,
            direction = Message.Direction.SENT
        )
        connectionManager.sendMessage(presentationRequest.makeMessage())
    }

    private suspend fun handlePresentationDefinitionRequest(
        requestPresentation: RequestPresentation,
        credential: Credential
    ): Presentation {
        try {
            if (credential !is ProvableCredential) {
                throw EdgeAgentError.InvalidCredentialError(credential)
            }

            val presentationDefinitionRequestString =
                requestPresentation.attachments.firstNotNullOf { it.data.getDataAsJsonString() }
            if (presentationDefinitionRequestString.contains("jwt")) {
                // If the json can be used to instantiate a JWTPresentationDefinitionRequest, process the request
                // as JWT.
                val didString =
                    credential.subject ?: throw Exception("Credential must contain subject")

                val storablePrivateKey = pluto.getDIDPrivateKeysByDID(DID(didString)).first().first()
                    ?: throw EdgeAgentError.CannotFindDIDPrivateKey(didString)
                val privateKey =
                    apollo.restorePrivateKey(storablePrivateKey.restorationIdentifier, storablePrivateKey.data)

                val presentationSubmissionProof = pollux.createJWTPresentationSubmission(
                    presentationDefinitionRequest = presentationDefinitionRequestString,
                    credential = credential,
                    privateKey = privateKey,
                )

                val attachmentDescriptor = AttachmentDescriptor(
                    mediaType = "application/json",
                    format = CredentialType.PRESENTATION_EXCHANGE_SUBMISSION.type,
                    data = AttachmentBase64(presentationSubmissionProof.base64UrlEncoded)
                )

                val fromDID = requestPresentation.to ?: createNewPeerDID(updateMediator = true)
                return Presentation(
                    body = Presentation.Body(),
                    attachments = arrayOf(attachmentDescriptor),
                    thid = requestPresentation.thid ?: requestPresentation.id,
                    from = fromDID,
                    to = requestPresentation.from
                )
            } else {
                val linkSecret = getLinkSecret()

                val presentationSubmissionProof = pollux.createAnoncredsPresentationSubmission(
                    presentationDefinitionRequest = presentationDefinitionRequestString,
                    credential = credential,
                    linkSecret = linkSecret
                )

                val attachmentDescriptor = AttachmentDescriptor(
                    mediaType = "application/json",
                    format = CredentialType.PRESENTATION_EXCHANGE_SUBMISSION.type,
                    data = AttachmentBase64(presentationSubmissionProof.base64UrlEncoded)
                )
                val fromDID = requestPresentation.to ?: createNewPeerDID(updateMediator = true)
                return Presentation(
                    body = Presentation.Body(),
                    attachments = arrayOf(attachmentDescriptor),
                    thid = requestPresentation.thid ?: requestPresentation.id,
                    from = fromDID,
                    to = requestPresentation.from
                )
            }
        } catch (e: Exception) {
            throw EdgeAgentError.CredentialNotValidForPresentationRequest()
        }
    }

    suspend fun handlePresentation(msg: Message): Boolean {
        if (msg.thid == null) {
            throw EdgeAgentError.MissingOrNullFieldError("thid", "presentation message")
        }
        val presentation = Presentation.fromMessage(msg)

        val presentationSubmissionString = presentation.attachments.firstNotNullOf { it.data.getDataAsJsonString() }

        val presentationDefinitionRequest =
            pluto.getMessageByThidAndPiuri(msg.thid, ProtocolType.DidcommRequestPresentation.value)
                .firstOrNull()
                ?.let { message ->
                    val requestPresentation = RequestPresentation.fromMessage(message)
                    requestPresentation.attachments.firstNotNullOf { it.data.getDataAsJsonString() }
                } ?: throw EdgeAgentError.MissingOrNullFieldError("thid", "presentation message")

        var isJwt = false
        var isAnoncreds = false
        try {
            Json.decodeFromString<JWTPresentationDefinitionRequest>(presentationDefinitionRequest)
            isJwt = true
        } catch (_: Exception) {
            try {
                Json.decodeFromString<AnoncredsPresentationDefinitionRequest>(presentationDefinitionRequest)
                isAnoncreds = true
            } catch (_: Exception) {
            }
        }

        val presentationSubmissionOptions =
            if (isJwt) {
                PresentationSubmissionOptionsJWT(presentationDefinitionRequest)
            } else if (isAnoncreds) {
                PresentationSubmissionOptionsAnoncreds(presentationDefinitionRequest)
            } else {
                throw Exception("Presentation definition request is neither JWT nor Anoncreds")
            }
        return pollux.verifyPresentationSubmission(
            presentationSubmissionString = presentationSubmissionString,
            options = presentationSubmissionOptions
        )
    }

    /**
     * This method provides a channel to listen for credentials that are revoked. As long as there is an
     * observer collecting from this flow the updates will keep happening.
     */
    fun observeRevokedCredentials(): Flow<List<Credential>> {
        return pluto.observeRevokedCredentials()
            .map { list ->
                list.map {
                    pollux.restoreCredential(it.restorationId, it.credentialData, it.revoked)
                }
            }
    }

    suspend fun isCredentialRevoked(credential: Credential) {
        edgeAgentScope.launch {
            val isRevoked = pollux.isCredentialRevoked(credential)
            if (isRevoked) {
                pluto.revokeCredential(credential.id)
            }
        }
    }

    /**
     * Performs a backup operation.
     *
     * @throws UnknownError.SomethingWentWrongError if something unexpected happens during the backup process.
     *
     * @return the encrypted backup string.
     */
    @Throws(UnknownError.SomethingWentWrongError::class)
    suspend fun backupWallet(plutoBackupTask: PlutoBackupTask = PlutoBackupTask(pluto)): String {
        // 1. Get the JWK
        val masterKey = createX25519PrivateKeyFrom(this.seed)
        if (masterKey.isExportable().not()) {
            throw UnknownError.SomethingWentWrongError("Key is not exportable")
        }
        val jwk = masterKey.getJwk().toNimbusJwk()

        // 2. Create an encrypter
        val encrypter: JWEEncrypter
        if (jwk is OctetKeyPair) {
            encrypter = X25519Encrypter(jwk.toPublicJWK())
        } else {
            throw UnknownError.SomethingWentWrongError("Unsupported JWK type for ECDH-ES encryption")
        }

        // 3. Set the JWE header (algorithm and encryption)
        // The following two line are needed because of a constrain in TS SDK
        val backupText = "backup"
        val apv = SHA256().digest(backupText.encodeToByteArray())

        val header = JWEHeader.Builder(JWEAlgorithm.ECDH_ES_A256KW, EncryptionMethod.A256CBC_HS512)
            .agreementPartyVInfo(Base64URL(apv.base64UrlEncoded))
            .build()

        val backup = plutoBackupTask.run().first()

        // 4. Create a JWE object
        val payloadString = Json.encodeToString(backup)
        val jweObject = JWEObject(header, Payload(payloadString))

        // 5. Perform the encryption
        jweObject.encrypt(encrypter)

        // 6. Serialize the JWE to a string
        val jweString = jweObject.serialize()

        return jweString
    }

    /**
     * Restores a Pluto instance from a JWE (JSON Web Encryption) string.
     *
     * @param jwe The JWE string that contains the encrypted backup.
     * @throws UnknownError.SomethingWentWrongError if the JWE algorithm or key type is unsupported.
     */
    @Throws(UnknownError.SomethingWentWrongError::class)
    suspend fun recoverWallet(jwe: String) {
        // 1. Get the JWK
        val masterKey = createX25519PrivateKeyFrom(this.seed)
        if (masterKey.isExportable().not()) {
            throw UnknownError.SomethingWentWrongError("Key is not exportable")
        }
        val jwk = masterKey.getJwk().toNimbusJwk()

        // 2. Parse the JWE string
        val jweObject = JWEObject.parse(jwe)

        // 3. Determine the algorithm and create a decrypter
        val alg = jweObject.header.algorithm
        val enc = jweObject.header.encryptionMethod
        val decrypter: JWEDecrypter
        if (alg == JWEAlgorithm.ECDH_ES_A256KW && enc == EncryptionMethod.A256CBC_HS512 && jwk is OctetKeyPair) {
            decrypter = X25519Decrypter(jwk) // ECDH decrypter for key agreement
        } else {
            throw UnknownError.SomethingWentWrongError("Unsupported JWE algorithm or key type")
        }

        // 4. Decrypt the JWE
        jweObject.decrypt(decrypter)

        // 5. Get the decrypted payload
        val json = jweObject.payload.toString()

        // 6. Parse the decrypted payload
        val backupObject = Json.decodeFromString<BackupV0_0_1>(json)

        // 7. Restore the pluto instance
        val restoreTask = PlutoRestoreTask(castor, pluto, backupObject)
        restoreTask.run()
    }

    /**
     * Creates a X25519PrivateKey using the provided seed and derivation path.
     *
     * @param seed The seed used to generate the private key.
     * @param derivationPath The derivation path used to derive the private key with a default value of "m/0'/0'/0'"
     * @return The generated X25519PrivateKey.
     * @throws UnknownError.SomethingWentWrongError if an exception occurs during the private key generation.
     */
    private fun createX25519PrivateKeyFrom(seed: Seed, derivationPath: String = "m/0'/0'/0'"): X25519PrivateKey {
        return try {
            apollo.createPrivateKey(
                mapOf(
                    TypeKey().property to KeyTypes.Curve25519,
                    CurveKey().property to Curve.X25519.value,
                    SeedKey().property to seed.value.base64UrlEncoded,
                    DerivationPathKey().property to derivationPath
                )
            ) as X25519PrivateKey
        } catch (ex: Exception) {
            throw UnknownError.SomethingWentWrongError(ex.localizedMessage, ex)
        }
    }

    /**
     * This method retrieves the link secret from Pluto.
     *
     * The method first retrieves the link secret using the `pluto.getLinkSecret()` function. If the link secret is not
     * found, a new `LinkSecret` object is created and stored in Pluto using the `pluto.storeLinkSecret()` function. The
     * newly created link secret object is then returned. If a link secret is found, a new `LinkSecret` object is created
     * using the existing link secret value and returned.
     *
     * @return The retrieved or newly created link secret object.
     */
    private suspend fun getLinkSecret(): String {
        val linkSecret = pluto.getLinkSecret().firstOrNull()
        return if (linkSecret == null) {
            val linkSecretObj = createLinkSecret()
            pluto.storeLinkSecret(linkSecretObj)
            linkSecretObj
        } else {
            linkSecret
        }
    }

    private fun generateNumericNonce(size: Int = 16): String {
        val random = SecureRandom()
        val nonce = StringBuilder(size)

        repeat(size) {
            val digit = random.nextInt(10) // Generates a number between 0 and 9
            nonce.append(digit)
        }

        return nonce.toString()
    }

    /**
     * Parses and validates a connectionless message from a JSON object. The method checks for the existence
     * of required fields (e.g., id, body, attachments, thid, from) and throws errors if any are missing.
     * It extracts necessary information from the message, including the attachment details, and returns
     * a ConnectionlessMessageData object containing the parsed information.
     *
     * @param messageJson The JsonObject representing the connectionless message.
     * @return A ConnectionlessMessageData object containing the parsed message data.
     * @throws EdgeAgentError.MissingOrNullFieldError if any required field is missing or null.
     */
    private fun parseAndValidateMessage(messageJson: JsonObject): ConnectionlessMessageData {
        // Perform validation
        if (!messageJson.containsKey("id")) throw EdgeAgentError.MissingOrNullFieldError("id", "Request")
        if (!messageJson.containsKey("body")) throw EdgeAgentError.MissingOrNullFieldError("body", "Request")
        if (!messageJson.containsKey("attachments")) {
            throw EdgeAgentError.MissingOrNullFieldError(
                "attachments",
                "Request"
            )
        }
        if (!messageJson.containsKey("thid")) throw EdgeAgentError.MissingOrNullFieldError("thid", "Request")
        if (!messageJson.containsKey("from")) throw EdgeAgentError.MissingOrNullFieldError("from", "Request")

        val messageId = messageJson["id"]!!.jsonPrimitive.content
        val messageBody = messageJson["body"]!!.toString()
        val messageThid = messageJson["thid"]!!.jsonPrimitive.content
        val messageFrom = messageJson["from"]!!.jsonPrimitive.content

        // Validate and parse the first attachment
        val attachmentJsonObject = messageJson["attachments"]!!.jsonArray.first().jsonObject
        if (!attachmentJsonObject.containsKey("id")) {
            throw EdgeAgentError.MissingOrNullFieldError(
                "id",
                "Request attachments"
            )
        }
        if (!attachmentJsonObject.containsKey("media_type")) {
            throw EdgeAgentError.MissingOrNullFieldError(
                "media_type",
                "Request attachments"
            )
        }
        if (!attachmentJsonObject.containsKey("data")) {
            throw EdgeAgentError.MissingOrNullFieldError(
                "data",
                "Request attachments"
            )
        }
        if (!attachmentJsonObject.containsKey("format")) {
            throw EdgeAgentError.MissingOrNullFieldError(
                "format",
                "Request attachments"
            )
        }

        val attachmentId = attachmentJsonObject["id"]!!.jsonPrimitive.content
        val attachmentMediaType = attachmentJsonObject["media_type"]!!.jsonPrimitive.content
        val attachmentData = attachmentJsonObject["data"]!!.jsonObject["json"]!!.toString()
        val attachmentFormat = attachmentJsonObject["format"]!!.jsonPrimitive.content

        val attachmentDescriptor = AttachmentDescriptor(
            id = attachmentId,
            mediaType = attachmentMediaType,
            data = AttachmentData.AttachmentJsonData(attachmentData),
            format = attachmentFormat
        )

        // Return the extracted data
        return ConnectionlessMessageData(
            messageId = messageId,
            messageBody = messageBody,
            attachmentDescriptor = attachmentDescriptor,
            messageThid = messageThid,
            messageFrom = messageFrom
        )
    }

    /**
     * Handles a connectionless invitation by parsing the invitation string, extracting the necessary
     * message data, and invoking the appropriate handler based on the type of the message.
     *
     * @param did The DID (Decentralized Identifier) associated with the invitation.
     * @param invitationString The JSON string representing the invitation.
     * @throws EdgeAgentError.MissingOrNullFieldError if any required field is missing or null.
     * @throws EdgeAgentError.UnknownInvitationTypeError if the invitation type is unknown.
     */
    private suspend fun connectionlessInvitation(outOfBandInvitation: OutOfBandInvitation): InvitationType {
        val now = Instant.fromEpochMilliseconds(getTimeMillis())
        val expiryDate = Instant.fromEpochSeconds(outOfBandInvitation.expiresTime)
        if (now > expiryDate) {
            throw EdgeAgentError.ExpiredInvitation()
        }

        val jsonString = outOfBandInvitation.attachments.firstNotNullOf { it.data.getDataAsJsonString() }
        val invitation = Json.parseToJsonElement(jsonString)
        if (invitation.jsonObject.containsKey("type")) {
            val type = invitation.jsonObject["type"]?.jsonPrimitive?.content
            val attachments = invitation.jsonObject["attachments"]!!.jsonArray
            if (attachments.isEmpty()) {
                throw Exception()
            }
            val connectionLessMessageData = parseAndValidateMessage(invitation.jsonObject)
            return when (type) {
                ProtocolType.DidcommOfferCredential.value -> {
                    handleConnectionlessOfferCredential(connectionLessMessageData)
                }

                ProtocolType.DidcommRequestPresentation.value -> {
                    handleConnectionlessRequestPresentation(connectionLessMessageData)
                }

                else -> {
                    throw EdgeAgentError.UnknownInvitationTypeError(type ?: "null")
                }
            }
        } else {
            throw EdgeAgentError.MissingOrNullFieldError("type", "connectionless invitation")
        }
    }

    /**
     * Handles a connectionless Offer Credential message by extracting the necessary data
     * from the ConnectionlessMessageData and storing the message using the Pluto service.
     *
     * @param connectionlessMessageData The parsed data from the connectionless message.
     */
    private suspend fun handleConnectionlessOfferCredential(
        connectionlessMessageData: ConnectionlessMessageData
    ): InvitationType {
        val did = createNewPeerDID(updateMediator = true)
        val offerCredential = OfferCredential(
            id = connectionlessMessageData.messageId,
            body = Json.decodeFromString(connectionlessMessageData.messageBody),
            attachments = arrayOf(connectionlessMessageData.attachmentDescriptor),
            thid = connectionlessMessageData.messageThid,
            from = DID(connectionlessMessageData.messageFrom),
            to = did
        )
        pluto.storeMessage(offerCredential.makeMessage())
        return ConnectionlessCredentialOffer(
            offerCredential = offerCredential
        )
    }

    /**
     * Handles a connectionless Request Presentation message by extracting the necessary data
     * from the ConnectionlessMessageData and storing the message using the Pluto service.
     *
     * @param connectionlessMessageData The parsed data from the connectionless message.
     */
    private suspend fun handleConnectionlessRequestPresentation(
        connectionlessMessageData: ConnectionlessMessageData
    ): InvitationType {
        val did = createNewPeerDID(updateMediator = true)
        val requestPresentation = RequestPresentation(
            id = connectionlessMessageData.messageId,
            body = Json.decodeFromString(connectionlessMessageData.messageBody),
            attachments = arrayOf(connectionlessMessageData.attachmentDescriptor),
            thid = connectionlessMessageData.messageThid,
            from = DID(connectionlessMessageData.messageFrom),
            to = did
        )

//        pluto.storeMessage(requestPresentation.makeMessage())
        return ConnectionlessRequestPresentation(
            requestPresentation = requestPresentation
        )
    }

    /**
     * Enumeration representing the current state of the agent.
     */
    enum class State {
        STOPPED,
        STARTING,
        RUNNING,
        STOPPING
    }
}
