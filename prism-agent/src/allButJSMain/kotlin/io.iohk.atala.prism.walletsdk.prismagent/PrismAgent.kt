package io.iohk.atala.prism.walletsdk.prismagent

import io.iohk.atala.prism.walletsdk.domain.buildingBlocks.Apollo
import io.iohk.atala.prism.walletsdk.domain.buildingBlocks.Castor
import io.iohk.atala.prism.walletsdk.domain.buildingBlocks.Mercury
import io.iohk.atala.prism.walletsdk.domain.buildingBlocks.Pluto
import io.iohk.atala.prism.walletsdk.domain.models.Curve
import io.iohk.atala.prism.walletsdk.domain.models.DID
import io.iohk.atala.prism.walletsdk.domain.models.DIDDocument
import io.iohk.atala.prism.walletsdk.domain.models.KeyCurve
import io.iohk.atala.prism.walletsdk.domain.models.PrismAgentError
import io.iohk.atala.prism.walletsdk.domain.models.Seed
import io.iohk.atala.prism.walletsdk.domain.models.Signature
import io.iohk.atala.prism.walletsdk.prismagent.helpers.Api
import io.iohk.atala.prism.walletsdk.prismagent.helpers.ApiImpl
import io.iohk.atala.prism.walletsdk.prismagent.helpers.HttpClient
import io.iohk.atala.prism.walletsdk.prismagent.mediation.MediationHandler
import io.iohk.atala.prism.walletsdk.prismagent.models.InvitationType
import io.iohk.atala.prism.walletsdk.prismagent.models.OutOfBandInvitation
import io.iohk.atala.prism.walletsdk.prismagent.models.PrismOnboardingInvitation
import io.iohk.atala.prism.walletsdk.prismagent.protocols.ProtocolType
import io.iohk.atala.prism.walletsdk.prismagent.protocols.findProtocolTypeByValue
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpMethod
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

class PrismAgent {
    enum class State {
        STOPED, STARTING, RUNNING, STOPING
    }

    var state: State = State.STOPED
        private set
    val seed: Seed
    val apollo: Apollo
    val castor: Castor
    val pluto: Pluto
    val mercury: Mercury

    private val api: Api
    private val connectionManager: ConnectionManager

    internal constructor(
        apollo: Apollo,
        castor: Castor,
        pluto: Pluto,
        mercury: Mercury,
        connectionManager: ConnectionManager,
        seed: Seed?,
        api: Api?,
    ) {
        this.apollo = apollo
        this.castor = castor
        this.pluto = pluto
        this.mercury = mercury
        this.connectionManager = connectionManager
        this.seed = seed ?: apollo.createRandomSeed().second
        this.api = api ?: ApiImpl(
            HttpClient {
                install(ContentNegotiation) {
                    json(
                        Json {
                            ignoreUnknownKeys = true
                            prettyPrint = true
                            isLenient = true
                        },
                    )
                }
            },
        )
    }

    constructor(
        apollo: Apollo,
        castor: Castor,
        pluto: Pluto,
        mercury: Mercury,
        seed: Seed? = null,
        api: Api? = null,
        mediatorHandler: MediationHandler,
    ) {
        this.apollo = apollo
        this.castor = castor
        this.pluto = pluto
        this.mercury = mercury
        this.seed = seed ?: apollo.createRandomSeed().second
        this.api = api ?: ApiImpl(
            HttpClient {
                install(ContentNegotiation) {
                    json(
                        Json {
                            ignoreUnknownKeys = true
                            prettyPrint = true
                            isLenient = true
                        },
                    )
                }
            },
        )
        this.connectionManager = ConnectionManager(mercury, castor, pluto, mediatorHandler)
    }

    @Throws()
    suspend fun start() {
        if (state != State.STOPED) { return }
        state = State.STARTING
        try {
            connectionManager.startMediator()
        } catch (error: PrismAgentError.noMediatorAvailableError) {
            val hostDID = createNewPeerDID(
                arrayOf(
                    DIDDocument.Service(
                        "#didcomm-1",
                        arrayOf("DIDCommMessaging"),
                        DIDDocument.ServiceEndpoint(connectionManager.mediationHandler.mediatorDID.toString()),
                    ),
                ),
                false,
            )
            connectionManager.registerMediator(hostDID)
        }
        if (connectionManager.mediationHandler.mediator != null) {
            state = State.RUNNING
        } else {
            throw PrismAgentError.mediationRequestFailedError()
        }
    }

    suspend fun createNewPrismDID(
        keyPathIndex: Int? = null,
        alias: String? = null,
        services: Array<DIDDocument.Service> = emptyArray(),
    ): DID {
        val index = keyPathIndex ?: pluto.getPrismLastKeyPathIndex()
        val keyPair = apollo.createKeyPair(seed = seed, curve = KeyCurve(Curve.SECP256K1, index))
        val did = castor.createPrismDID(masterPublicKey = keyPair.publicKey, services = services)
        pluto.storePrivateKeys(keyPair.privateKey, did, index)
        pluto.storePrismDID(did = did, keyPathIndex = index, alias = alias)
        return did
    }

    suspend fun createNewPeerDID(
        services: Array<DIDDocument.Service> = emptyArray(),
        updateMediator: Boolean,
    ): DID {
        val keyAgreementKeyPair = apollo.createKeyPair(seed = seed, curve = KeyCurve(Curve.X25519))
        val authenticationKeyPair = apollo.createKeyPair(seed = seed, curve = KeyCurve(Curve.ED25519))

        val did = castor.createPeerDID(
            arrayOf(keyAgreementKeyPair, authenticationKeyPair),
            services = services,
        )

        if (updateMediator) {
            // TODO: This still needs to be done update the key List
        }

        pluto.storePeerDID(
            did = did,
            privateKeys = arrayOf(keyAgreementKeyPair.privateKey, authenticationKeyPair.privateKey),
        )

        // The next logic is a bit tricky, so it's not forgotten this is a reminder.
        // The next few lines are needed because of DIDComm library, the library will need
        // to get the secret(private key) that is pair of the public key within the DIDPeer Document
        // to this end the library will give you the id of the public key that is `did:{method}:{methodId}#ecnumbasis`.
        // So the code below after the did is created, it will retrieve the document and
        // and store the private keys with the corresponding `id` of the one created on the document.
        // So when the secret resolver asks for the secret we can identify it.
        val document = castor.resolveDID(did.toString())

        val verificationMethods = document.coreProperties.firstOrNull {
            it is DIDDocument.VerificationMethods
        } as? DIDDocument.VerificationMethods

        verificationMethods?.values?.forEach {
            if(it.type.contains("X25519")) {
                pluto.storePrivateKeys(keyAgreementKeyPair.privateKey, did, 0, it.id.toString())
            } else if(it.type.contains("Ed25519")) {
                pluto.storePrivateKeys(authenticationKeyPair.privateKey, did, 0, it.id.toString())
            }
        }

        return did
    }

    @Throws(PrismAgentError.unknownInvitationTypeError::class)
    suspend fun parseInvitation(str: String): InvitationType {
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
                throw PrismAgentError.unknownInvitationTypeError()
        }

        return invite
    }

    suspend fun acceptInvitation(invitation: PrismOnboardingInvitation) {
        @Serializable
        data class SendDID(val did: String)

        val response = api.request(
            HttpMethod.Post.toString(),
            invitation.onboardEndpoint,
            arrayOf(),
            arrayOf(),
            SendDID(invitation.from.toString()),
        )

        if (response.status != 200) {
            throw PrismAgentError.failedToOnboardError()
        }
    }

    suspend fun signWith(did: DID, message: ByteArray): Signature {
        val privateKey = pluto.getDIDPrivateKeysByDID(did)?.firstOrNull() ?: throw PrismAgentError.cannotFindDIDPrivateKey()
        return apollo.signMessage(privateKey, message)
    }

    private suspend fun parsePrismInvitation(str: String): PrismOnboardingInvitation {
        try {
            val prismOnboarding = PrismOnboardingInvitation.prismOnboardingInvitationFromJsonString(str)
            val url = prismOnboarding.onboardEndpoint
            val did = createNewPeerDID(
                arrayOf(
                    DIDDocument.Service(
                        id = "#didcomm-1",
                        type = arrayOf("DIDCommMessaging"),
                        serviceEndpoint = DIDDocument.ServiceEndpoint(
                            uri = url,
                            accept = arrayOf("DIDCommMessaging"),
                            routingKeys = arrayOf(),
                        ),
                    ),
                ),
                true,
            )
            prismOnboarding.from = did
            return prismOnboarding
        } catch (e: Exception) {
            throw PrismAgentError.unknownInvitationTypeError()
        }
    }

    private fun parseOOBInvitation(str: String): OutOfBandInvitation {
        try {
            return Json.decodeFromString(str)
        } catch (e: Exception) {
            throw PrismAgentError.unknownInvitationTypeError()
        }
    }
}
