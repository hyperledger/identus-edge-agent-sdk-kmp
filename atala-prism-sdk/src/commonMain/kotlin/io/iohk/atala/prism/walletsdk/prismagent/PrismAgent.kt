package io.iohk.atala.prism.walletsdk.prismagent

/* ktlint-disable import-ordering */
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
import io.iohk.atala.prism.walletsdk.domain.models.Curve
import io.iohk.atala.prism.walletsdk.domain.models.DID
import io.iohk.atala.prism.walletsdk.domain.models.DIDDocument
import io.iohk.atala.prism.walletsdk.domain.models.KeyCurve
import io.iohk.atala.prism.walletsdk.domain.models.Message
import io.iohk.atala.prism.walletsdk.domain.models.PolluxError
import io.iohk.atala.prism.walletsdk.domain.models.PrismAgentError
import io.iohk.atala.prism.walletsdk.domain.models.Seed
import io.iohk.atala.prism.walletsdk.domain.models.Signature
import io.iohk.atala.prism.walletsdk.domain.models.VerifiableCredential
import io.iohk.atala.prism.walletsdk.domain.models.httpClient
import io.iohk.atala.prism.walletsdk.prismagent.mediation.MediationHandler
import io.iohk.atala.prism.walletsdk.prismagent.protocols.ProtocolType
import io.iohk.atala.prism.walletsdk.prismagent.protocols.connection.DIDCommConnectionRunner
import io.iohk.atala.prism.walletsdk.prismagent.protocols.findProtocolTypeByValue
import io.iohk.atala.prism.walletsdk.prismagent.protocols.outOfBand.DIDCommInvitationRunner
import io.iohk.atala.prism.walletsdk.prismagent.protocols.outOfBand.InvitationType
import io.iohk.atala.prism.walletsdk.prismagent.protocols.outOfBand.OutOfBandInvitation
import io.iohk.atala.prism.walletsdk.prismagent.protocols.outOfBand.PrismOnboardingInvitation
import io.iohk.atala.prism.walletsdk.prismagent.protocols.issueCredential.OfferCredential
import io.iohk.atala.prism.walletsdk.prismagent.protocols.issueCredential.RequestCredential
import io.iohk.atala.prism.walletsdk.prismagent.protocols.proofOfPresentation.Presentation
import io.iohk.atala.prism.walletsdk.prismagent.protocols.proofOfPresentation.RequestPresentation
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpMethod
import io.ktor.http.Url
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import java.time.Duration
import kotlin.jvm.Throws
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
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

class PrismAgent {
    var state: State = State.STOPPED
        private set
    val seed: Seed
    val apollo: Apollo
    val castor: Castor
    val pluto: Pluto
    val mercury: Mercury
    val pollux: Pollux
    lateinit var fetchingMessagesJob: Job

    private val prismAgentScope: CoroutineScope = CoroutineScope(Dispatchers.Default)
    private val api: Api
    private val connectionManager: ConnectionManager
    private val flowState = MutableSharedFlow<State>()

    constructor(
        apollo: Apollo,
        castor: Castor,
        pluto: Pluto,
        mercury: Mercury,
        pollux: Pollux,
        connectionManager: ConnectionManager,
        seed: Seed?,
        api: Api?
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
        mediatorHandler: MediationHandler
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
        // Pairing will be removed in the future
        this.connectionManager = ConnectionManager(mercury, castor, pluto, mediatorHandler, mutableListOf())
    }

    fun getFlowState(): Flow<State> {
        return flowState
    }

    @Throws(PrismAgentError.MediationRequestFailedError::class)
    suspend fun start() {
        if (state != State.STOPPED) {
            return
        }
        state = State.STARTING
        flowState.emit(State.STARTING)
        try {
            connectionManager.startMediator()
        } catch (error: PrismAgentError.NoMediatorAvailableError) {
            val hostDID = createNewPeerDID(
                emptyArray(),
                false,
            )
            connectionManager.registerMediator(hostDID)
        }
        if (connectionManager.mediationHandler.mediator != null) {
            flowState.emit(State.RUNNING)
            state = State.RUNNING
        } else {
            throw PrismAgentError.MediationRequestFailedError()
        }
    }

    suspend fun stop() {
        if (state != State.RUNNING) {
            return
        }
        flowState.emit(State.STOPPING)
        state = State.STOPPING
        fetchingMessagesJob?.cancel()
        flowState.emit(State.STOPPED)
        state = State.STOPPED
    }

    @JvmOverloads
    suspend fun createNewPrismDID(
        keyPathIndex: Int? = null,
        alias: String? = null,
        services: Array<DIDDocument.Service> = emptyArray()
    ): DID {
        val index = keyPathIndex ?: pluto.getPrismLastKeyPathIndex().first()
        val keyPair = apollo.createKeyPair(seed = seed, curve = KeyCurve(Curve.SECP256K1, index))
        val did = castor.createPrismDID(masterPublicKey = keyPair.publicKey, services = services)
        pluto.storePrismDIDAndPrivateKeys(did = did, keyPathIndex = index, alias = alias, listOf(keyPair.privateKey))
        return did
    }

    @JvmOverloads
    suspend fun createNewPeerDID(
        services: Array<DIDDocument.Service> = emptyArray(),
        updateMediator: Boolean
    ): DID {
        val keyAgreementKeyPair = apollo.createKeyPair(seed = seed, curve = KeyCurve(Curve.X25519))
        val authenticationKeyPair = apollo.createKeyPair(seed = seed, curve = KeyCurve(Curve.ED25519))

        var tmpServices = arrayOf<DIDDocument.Service>()
        if (updateMediator) {
            tmpServices = services.plus(
                DIDDocument.Service(
                    id = "#didcomm-1",
                    type = arrayOf(
                        "DIDCommMessaging"
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

        if (updateMediator) {
            connectionManager.mediationHandler.updateKeyListWithDIDs(arrayOf(did))
        }

        pluto.storePeerDIDAndPrivateKeys(
            did = did,
            privateKeys = listOf(keyAgreementKeyPair.privateKey, authenticationKeyPair.privateKey)
        )

        // The next logic is a bit tricky, so it's not forgotten this is a reminder.
        // The next few lines are needed because of DIDComm library, the library will need
        // to get the secret(private key) that is pair of the public key within the DIDPeer Document
        // to this end the library will give you the id of the public key that is `did:{method}:{methodId}#ecnumbasis`.
        // So the code below after the did is created, it will retrieve the document and
        // and store the private keys with the corresponding `id` of the one created on the document.
        // So when the secret resolver asks for the secret we can identify it.
        val document = castor.resolveDID(did.toString())

        val listOfVerificationMethods: MutableList<DIDDocument.VerificationMethod> = mutableListOf()
        document.coreProperties.forEach {
            if (it is DIDDocument.Authentication) {
                listOfVerificationMethods.addAll(it.verificationMethods)
            }
            if (it is DIDDocument.KeyAgreement) {
                listOfVerificationMethods.addAll(it.verificationMethods)
            }
        }
        val verificationMethods = DIDDocument.VerificationMethods(listOfVerificationMethods.toTypedArray())

        verificationMethods.values.forEach {
            if (it.type.contains("X25519")) {
                pluto.storePrivateKeys(keyAgreementKeyPair.privateKey, did, 0, it.id.toString())
            } else if (it.type.contains("Ed25519")) {
                pluto.storePrivateKeys(authenticationKeyPair.privateKey, did, 0, it.id.toString())
            }
        }
        return did
    }

    @Throws(PrismAgentError.CannotFindDIDPrivateKey::class)
    suspend fun signWith(did: DID, message: ByteArray): Signature {
        val privateKey =
            pluto.getDIDPrivateKeysByDID(did).first().first() ?: throw PrismAgentError.CannotFindDIDPrivateKey()
        return apollo.signMessage(privateKey, message)
    }

    @OptIn(DelicateCoroutinesApi::class)
    @JvmOverloads
    fun startFetchingMessages(requestInterval: Int = 5) {
        if (fetchingMessagesJob == null) {
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
                            connectionManager.mediationHandler.registerMessagesAsRead(messagesIds.toTypedArray())
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

    fun stopFetchingMessages() {
        fetchingMessagesJob?.cancel()
    }

    fun handleMessagesEvents(): Flow<List<Message>> {
        return pluto.getAllMessages()
    }

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
        val offerDataString = offer.attachments.mapNotNull {
            when (it.data) {
                is AttachmentJsonData -> it.data.data
                else -> null
            }
        }.first()
        val offerJsonObject = Json.parseToJsonElement(offerDataString).jsonObject
        val jwtString = pollux.createRequestCredentialJWT(did, privateKey, offerJsonObject)
        val attachmentDescriptor =
            AttachmentDescriptor(mediaType = "prism/jwt", data = AttachmentBase64(jwtString.base64UrlEncoded))
        return RequestCredential(
            from = offer.to,
            to = offer.from,
            thid = offer.thid,
            body = RequestCredential.Body(offer.body.goalCode, offer.body.comment, offer.body.formats),
            attachments = arrayOf(attachmentDescriptor)
        )
    }

    @Throws(PolluxError.InvalidPrismDID::class)
    suspend fun preparePresentationForRequestProof(
        request: RequestPresentation,
        credential: VerifiableCredential
    ): Presentation {
        val subjectDID = DID(credential.credentialSubject)
        if (subjectDID.method != "prism") {
            throw PolluxError.InvalidPrismDID()
        }
        val privateKeyKeyPath = pluto.getPrismDIDKeyPathIndex(subjectDID).first()
        val privateKey = apollo.createKeyPair(seed, KeyCurve(Curve.SECP256K1, privateKeyKeyPath)).privateKey
        val requestData = request.attachments.mapNotNull {
            when (it.data) {
                is AttachmentJsonData -> it.data.data
                else -> null
            }
        }.first()
        val requestJsonObject = Json.parseToJsonElement(requestData).jsonObject
        val jwtString = pollux.createVerifiablePresentationJWT(subjectDID, privateKey, credential, requestJsonObject)
        val attachmentDescriptor =
            AttachmentDescriptor(mediaType = "prism/jwt", data = AttachmentBase64(jwtString.base64UrlEncoded))
        return Presentation(
            from = request.to,
            to = request.from,
            thid = request.thid,
            body = Presentation.Body(request.body.goalCode, request.body.comment),
            attachments = arrayOf(attachmentDescriptor)
        )
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
                        id = "#didcomm-1",
                        type = arrayOf("DIDCommMessaging"),
                        serviceEndpoint = DIDDocument.ServiceEndpoint(
                            uri = url,
                            accept = arrayOf("DIDCommMessaging"),
                            routingKeys = arrayOf()
                        )
                    )
                ),
                false,
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
            throw PrismAgentError.FailedToOnboardError()
        }
    }

    enum class State {
        STOPPED, STARTING, RUNNING, STOPPING
    }
}
