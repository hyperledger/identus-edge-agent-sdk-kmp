package io.iohk.atala.prism.walletsdk.prismagent

/* ktlint-disable import-ordering */
import io.iohk.atala.prism.apollo.base64.base64UrlDecoded
import io.iohk.atala.prism.apollo.base64.base64UrlEncoded
import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Apollo
import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Castor
import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Mercury
import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Pluto
import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Pollux
import io.iohk.atala.prism.walletsdk.domain.models.Api
import io.iohk.atala.prism.walletsdk.domain.models.ApiImpl
import io.iohk.atala.prism.walletsdk.domain.models.AttachmentBase64
import io.iohk.atala.prism.walletsdk.domain.models.AttachmentDescriptor
import io.iohk.atala.prism.walletsdk.domain.models.AttachmentJsonData
import io.iohk.atala.prism.walletsdk.domain.models.Credential
import io.iohk.atala.prism.walletsdk.domain.models.Curve
import io.iohk.atala.prism.walletsdk.domain.models.DID
import io.iohk.atala.prism.walletsdk.domain.models.DIDDocument
import io.iohk.atala.prism.walletsdk.domain.models.DIDPair
import io.iohk.atala.prism.walletsdk.domain.models.KeyCurve
import io.iohk.atala.prism.walletsdk.domain.models.Message
import io.iohk.atala.prism.walletsdk.domain.models.PeerDID
import io.iohk.atala.prism.walletsdk.domain.models.PolluxError
import io.iohk.atala.prism.walletsdk.domain.models.PrismDIDInfo
import io.iohk.atala.prism.walletsdk.domain.models.Seed
import io.iohk.atala.prism.walletsdk.domain.models.Signature
import io.iohk.atala.prism.walletsdk.domain.models.httpClient
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.KeyPair
import io.iohk.atala.prism.walletsdk.domain.models.keyManagement.PrivateKey
import io.iohk.atala.prism.walletsdk.logger.LogComponent
import io.iohk.atala.prism.walletsdk.logger.Metadata
import io.iohk.atala.prism.walletsdk.logger.PrismLogger
import io.iohk.atala.prism.walletsdk.logger.PrismLoggerImpl
import io.iohk.atala.prism.walletsdk.prismagent.mediation.BasicMediatorHandler
import io.iohk.atala.prism.walletsdk.prismagent.mediation.MediationHandler
import io.iohk.atala.prism.walletsdk.prismagent.protocols.ProtocolType
import io.iohk.atala.prism.walletsdk.prismagent.protocols.connection.DIDCommConnectionRunner
import io.iohk.atala.prism.walletsdk.prismagent.protocols.findProtocolTypeByValue
import io.iohk.atala.prism.walletsdk.prismagent.protocols.issueCredential.IssueCredential
import io.iohk.atala.prism.walletsdk.prismagent.protocols.issueCredential.OfferCredential
import io.iohk.atala.prism.walletsdk.prismagent.protocols.issueCredential.RequestCredential
import io.iohk.atala.prism.walletsdk.prismagent.protocols.outOfBand.DIDCommInvitationRunner
import io.iohk.atala.prism.walletsdk.prismagent.protocols.outOfBand.InvitationType
import io.iohk.atala.prism.walletsdk.prismagent.protocols.outOfBand.OutOfBandInvitation
import io.iohk.atala.prism.walletsdk.prismagent.protocols.outOfBand.PrismOnboardingInvitation
import io.iohk.atala.prism.walletsdk.prismagent.protocols.proofOfPresentation.Presentation
import io.iohk.atala.prism.walletsdk.prismagent.protocols.proofOfPresentation.RequestPresentation
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.Url
import io.ktor.http.HttpMethod
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import java.net.UnknownHostException
import java.time.Duration

/* ktlint-disable import-ordering */

/**
 * Check if the passed URL is valid or not.
 * @param str string to check its URL validity
 * @return [Url] if valid, null if not valid
 */
private fun Url.Companion.parse(str: String): Url? {
    try {
        return Url(str)
    } catch (e: Throwable) {
        return null
    }
}

/**
 * PrismAgent class is responsible for handling the connection to other agents in the network using a provided Mediator
 * Service Endpoint and seed data.
 */

class PrismAgent {
    var state: State = State.STOPPED
        private set(value) {
            field = value
            prismAgentScope.launch {
                flowState.emit(value)
            }
        }
    val seed: Seed
    val apollo: Apollo
    val castor: Castor
    val pluto: Pluto
    val mercury: Mercury
    val pollux: Pollux
    var fetchingMessagesJob: Job? = null
    val flowState = MutableSharedFlow<State>()

    private val prismAgentScope: CoroutineScope = CoroutineScope(Dispatchers.Default)
    private val api: Api
    private var connectionManager: ConnectionManager
    private var logger: PrismLogger

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
        logger: PrismLogger = PrismLoggerImpl(LogComponent.PRISM_AGENT)
    ) {
        prismAgentScope.launch {
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
    }

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
        logger: PrismLogger = PrismLoggerImpl(LogComponent.PRISM_AGENT)
    ) {
        prismAgentScope.launch {
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
        // Pairing will be removed in the future
        this.connectionManager =
            ConnectionManager(mercury, castor, pluto, mediatorHandler, mutableListOf())
    }

    init {
        flowState.onSubscription {
            if (flowState.subscriptionCount.value <= 0) {
                state = State.STOPPED
            } else {
                throw Exception("Agent state only accepts one subscription.")
            }
        }
    }

    // Prism agent actions
    /**
     * Start the [PrismAgent] and Mediator services.
     *
     * @throws [PrismAgentError.MediationRequestFailedError] failed to connect to mediator.
     * @throws [UnknownHostException] if unable to connect to the mediator.
     */
    @Throws(PrismAgentError.MediationRequestFailedError::class, UnknownHostException::class)
    suspend fun start() {
        if (state != State.STOPPED) {
            return
        }
        logger.info(message = "Starting agent")
        state = State.STARTING
        try {
            connectionManager.startMediator()
        } catch (error: PrismAgentError.NoMediatorAvailableError) {
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
            throw PrismAgentError.MediationRequestFailedError()
        }
    }

    /**
     * Stops the [PrismAgent].
     * The function sets the state of [PrismAgent] to [State.STOPPING].
     * All ongoing events that was created by the [PrismAgent] are stopped.
     * After all the events are stopped the state of the [PrismAgent] is set to [State.STOPPED].
     */
    fun stop() {
        if (state != State.RUNNING) {
            return
        }
        logger.info(message = "Stoping agent")
        state = State.STOPPING
        fetchingMessagesJob?.cancel()
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
        val keyPair = apollo.createKeyPair(seed = seed, curve = KeyCurve(Curve.SECP256K1, index))
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
            alias = alias,
            listOf(privateKey)
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
    suspend fun createNewPeerDID(
        services: Array<DIDDocument.Service> = emptyArray(),
        updateMediator: Boolean
    ): DID {
        val keyAgreementKeyPair =
            apollo.createKeyPair(seed = seed, curve = KeyCurve(Curve.X25519))
        val authenticationKeyPair =
            apollo.createKeyPair(seed = seed, curve = KeyCurve(Curve.ED25519))

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
     * @param did The DID to register.
     * @param privateKeys The list of private keys associated with the peer DID.
     * @param updateMediator Determines whether to update the mediator with the specified DID.
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
        // and store the private keys with the corresponding `id` of the one created on the document.
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
                pluto.storePrivateKeys(keyAgreementKeyPair.privateKey, did, 0, it.id.toString())
            } else if (it.type.contains("Ed25519")) {
                pluto.storePrivateKeys(
                    authenticationKeyPair.privateKey,
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
            ConnectionManager(mercury, castor, pluto, mediatorHandler, mutableListOf())
    }

    /**
     * Sets up a mediator DID for communication with the specified DID.
     *
     * @param did The DID of the mediator to set up.
     */
    suspend fun setupMediatorDID(did: DID) {
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
        pluto.storeDIDPair(pair.host, pair.receiver, pair.name ?: "")
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
        return connectionManager?.sendMessage(message)
    }

    // Credentials related actions

    /**
     * This function will use the provided DID to sign a given message.
     *
     * @param did The DID which will be used to sign the message.
     * @param message The message to be signed.
     * @return The signature of the message.
     */
    @Throws(PrismAgentError.CannotFindDIDPrivateKey::class)
    suspend fun signWith(did: DID, message: ByteArray): Signature {
        val privateKey =
            pluto.getDIDPrivateKeysByDID(did).first().first()
                ?: throw PrismAgentError.CannotFindDIDPrivateKey(did.toString())
        return apollo.signMessage(privateKey, message)
    }

    /**
     * This function prepares a request credential from an offer given the subject DID.
     * @param did Subject DID.
     * @param offer Received offer credential.
     * @return Created request credential.
     * @throws [PolluxError.InvalidPrismDID] if there is a problem creating the request credential.
     **/
    @Throws(PolluxError.InvalidPrismDID::class)
    suspend fun prepareRequestCredentialWithIssuer(
        did: DID,
        offer: OfferCredential
    ): RequestCredential {
        if (did.method != "prism") {
            throw PolluxError.InvalidPrismDID()
        }
        val privateKeyKeyPath = pluto.getPrismDIDKeyPathIndex(did).first()
        val privateKey =
            apollo.createKeyPair(seed, KeyCurve(Curve.SECP256K1, privateKeyKeyPath)).privateKey
        val offerDataString = offer.attachments.mapNotNull {
            when (it.data) {
                is AttachmentJsonData -> it.data.data
                else -> null
            }
        }.first()
        val offerJsonObject = Json.parseToJsonElement(offerDataString).jsonObject
        val jwtString = pollux.createRequestCredentialJWT(did, privateKey, offerJsonObject)
        val attachmentDescriptor =
            AttachmentDescriptor(
                mediaType = JWT_MEDIA_TYPE,
                data = AttachmentBase64(jwtString.base64UrlEncoded)
            )
        return RequestCredential(
            from = offer.to,
            to = offer.from,
            thid = offer.thid,
            body = RequestCredential.Body(
                offer.body.goalCode,
                offer.body.comment,
                offer.body.formats
            ),
            attachments = arrayOf(attachmentDescriptor)
        )
    }

    /**
     * This function parses an issued credential message, stores, and returns the verifiable credential.
     * @param message Issue credential Message.
     * @return The parsed verifiable credential.
     * @throws PrismAgentError if there is a problem parsing the credential.
     */
    fun processIssuedCredentialMessage(message: IssueCredential): Credential {
        val attachment = message.attachments.firstOrNull()?.data as? AttachmentBase64

        return attachment?.let {
            val credential = pollux.parseVerifiableCredential(it.base64.base64UrlDecoded)
            val storableCredential = pollux.credentialToStorableCredential(credential)
            pluto.storeCredential(storableCredential)
            return credential
        } ?: throw UnknownError("Cannot find attachment base64 in message")
    }

    // Message Events
    /**
     * Start fetching the messages from the mediator.
     */
    @JvmOverloads
    fun startFetchingMessages(requestInterval: Int = 5) {
        if (fetchingMessagesJob == null) {
            logger.info(message = "Start streaming new unread messages")
            fetchingMessagesJob = prismAgentScope.launch {
                while (true) {
                    connectionManager.awaitMessages().collect { array ->
                        val messagesIds = mutableListOf<String>()
                        val messages = mutableListOf<Message>()
                        array.map { pair ->
                            messagesIds.add(pair.first)
                            messages.add(pair.second)
                        }
                        if (messagesIds.isNotEmpty()) {
                            connectionManager.mediationHandler.registerMessagesAsRead(
                                messagesIds.toTypedArray()
                            )
                            pluto.storeMessages(messages)
                        }
                    }
                    delay(Duration.ofSeconds(requestInterval.toLong()).toMillis())
                }
            }
        }
        fetchingMessagesJob?.let {
            if (it.isActive) return
            it.start()
        }
    }

    /**
     * Stop fetching messages
     */
    fun stopFetchingMessages() {
        logger.info(message = "Stop streaming new unread messages")
        fetchingMessagesJob?.cancel()
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
     * @throws [PrismAgentError.UnknownInvitationTypeError] if the invitation is not a valid Prism or OOB type
     */
    @Throws(PrismAgentError.UnknownInvitationTypeError::class)
    suspend fun parseInvitation(str: String): InvitationType {
        Url.parse(str)?.let {
            return parseOOBInvitation(it)
        } ?: run {
            try {
                val json = Json.decodeFromString<JsonObject>(str)
                val typeString: String = if (json.containsKey("type")) {
                    json["type"].toString().trim('"')
                } else {
                    ""
                }

                val invite: InvitationType = when (findProtocolTypeByValue(typeString)) {
                    ProtocolType.PrismOnboarding -> parsePrismInvitation(str)
                    ProtocolType.Didcomminvitation -> parseOOBInvitation(str)
                    else ->
                        throw PrismAgentError.UnknownInvitationTypeError()
                }

                return invite
            } catch (e: SerializationException) {
                throw PrismAgentError.UnknownInvitationTypeError()
            }
        }
    }

    /**
     * Parses the given string as a Prism Onboarding invitation
     * @param str The string to parse
     * @return The parsed Prism Onboarding invitation
     * @throws [PrismAgentError.UnknownInvitationTypeError] if the string is not a valid Prism Onboarding invitation
     */
    @Throws(PrismAgentError.UnknownInvitationTypeError::class)
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
            throw PrismAgentError.UnknownInvitationTypeError()
        }
    }

    /**
     * Parses the given string as an Out-of-Band invitation
     * @param str The string to parse
     * @returns The parsed Out-of-Band invitation
     * @throws [PrismAgentError.UnknownInvitationTypeError] if the string is not a valid URL
     */
    @Throws(PrismAgentError.UnknownInvitationTypeError::class)
    private suspend fun parseOOBInvitation(str: String): OutOfBandInvitation {
        return try {
            Json.decodeFromString(str)
        } catch (ex: SerializationException) {
            throw PrismAgentError.UnknownInvitationTypeError()
        }
    }

    /**
     * Parses the given URL as an Out-of-Band invitation
     * @param url The URL to parse
     * @return The parsed Out-of-Band invitation
     * @throws [PrismAgentError.UnknownInvitationTypeError] if the URL is not a valid Out-of-Band invitation
     */
    private suspend fun parseOOBInvitation(url: Url): OutOfBandInvitation {
        return DIDCommInvitationRunner(url).run()
    }

    /**
     * Accepts an Out-of-Band (DIDComm) invitation and establishes a new connection
     * @param invitation The Out-of-Band invitation to accept
     * @throws [PrismAgentError.NoMediatorAvailableError] if there is no mediator available or other errors occur during the acceptance process
     */
    suspend fun acceptOutOfBandInvitation(invitation: OutOfBandInvitation) {
        val ownDID = createNewPeerDID(updateMediator = true)
        val pair = DIDCommConnectionRunner(invitation, pluto, ownDID, connectionManager).run()
        connectionManager.addConnection(pair)
    }

    /**
     * Accepts a Prism Onboarding invitation and performs the onboarding process
     * @param invitation The Prism Onboarding invitation to accept
     * @throws [PrismAgentError.FailedToOnboardError] if failed to on board
     */
    @Throws(PrismAgentError.FailedToOnboardError::class)
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
            throw PrismAgentError.FailedToOnboardError(response.status, response.jsonString)
        }
    }

    /**
     * This method returns a list of all the VerifiableCredentials stored locally.
     */
    suspend fun getAllCredentials(): Flow<List<Credential>> {
        return pluto.getAllCredentials()
            .map { list ->
                list.map {
                    pollux.restoreCredential(it.restorationId, it.credentialData)
                }
            }
    }

    // Proof related actions

    /**
     * This function creates a Presentation from a request verification.
     * @param request Request message received.
     * @param credential Verifiable Credential to present.
     * @return Presentation message prepared to send.
     * @throws PrismAgentError if there is a problem creating the presentation.
     **/
    @Throws(PolluxError.InvalidPrismDID::class)
    suspend fun preparePresentationForRequestProof(
        request: RequestPresentation,
        credential: Credential
    ): Presentation {
        val subjectDID = credential.subject?.let {
            DID(it)
        } ?: DID("")
        if (subjectDID.method != PRISM) {
            throw PolluxError.InvalidPrismDID()
        }

        val privateKeyKeyPath = pluto.getPrismDIDKeyPathIndex(subjectDID).first()
        val privateKey =
            apollo.createKeyPair(seed, KeyCurve(Curve.SECP256K1, privateKeyKeyPath)).privateKey
        val requestData = request.attachments.mapNotNull {
            when (it.data) {
                is AttachmentJsonData -> it.data.data
                else -> null
            }
        }.first()
        val requestJsonObject = Json.parseToJsonElement(requestData).jsonObject
        val jwtString = pollux.createVerifiablePresentationJWT(
            subjectDID,
            privateKey,
            credential,
            requestJsonObject
        )
        val attachmentDescriptor =
            AttachmentDescriptor(
                mediaType = JWT_MEDIA_TYPE,
                data = AttachmentBase64(jwtString.base64UrlEncoded)
            )
        return Presentation(
            from = request.to,
            to = request.from,
            thid = request.thid,
            body = Presentation.Body(request.body.goalCode, request.body.comment),
            attachments = arrayOf(attachmentDescriptor)
        )
    }

    /**
     * Enumeration representing the current state of the agent.
     */
    enum class State {
        STOPPED, STARTING, RUNNING, STOPPING
    }
}
