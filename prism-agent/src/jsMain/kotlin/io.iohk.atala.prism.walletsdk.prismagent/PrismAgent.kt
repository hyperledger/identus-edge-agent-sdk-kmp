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
import kotlin.js.Promise

@JsExport
final class PrismAgent {
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
        this.seed = seed ?: apollo.createRandomSeed().seed
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

    @JsName(name = "NewName")
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
        this.seed = seed ?: apollo.createRandomSeed().seed
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

    fun start(): Promise<State> {
        if (state != State.STOPED) { return Promise.Companion.resolve(state) }
        state = State.STARTING
        try {
            connectionManager.startMediator()
            state = State.RUNNING
            return Promise.resolve(state)
        } catch (error: PrismAgentError.noMediatorAvailableError) {
            return createNewPeerDID(
                arrayOf(
                    DIDDocument.Service(
                        "#didcomm-1",
                        arrayOf("DIDCommMessaging"),
                        DIDDocument.ServiceEndpoint(connectionManager.mediationHandler.mediatorDID.toString()),
                    ),
                ),
                false,
            ).then {
                connectionManager.registerMediator(it)
            }.then {
                if (connectionManager.mediationHandler.mediator != null) {
                    state = State.RUNNING
                    state
                } else {
                    throw PrismAgentError.mediationRequestFailedError()
                }
            }
        }
    }

//    fun createNewPrismDID(
//        keyPathIndex: Int? = null,
//        alias: String? = null,
//        services: Array<DIDDocument.Service> = emptyArray(),
//    ): DID {
//        val index = keyPathIndex ?: pluto.getPrismLastKeyPathIndex()
//        val keyPair = apollo.createKeyPair(seed = seed, curve = KeyCurve(Curve.SECP256K1, index))
//        val did = castor.createPrismDID(masterPublicKey = keyPair.publicKey, services = services)
//        pluto.storePrivateKeys(keyPair.privateKey, did, index)
//        pluto.storePrismDID(did = did, keyPathIndex = index, alias = alias)
//        return did
//    }

    fun createNewPeerDID(
        services: Array<DIDDocument.Service> = emptyArray(),
        updateMediator: Boolean,
    ): Promise<DID> {
        val keyAgreementKeyPair = apollo.createKeyPair(seed = seed, curve = KeyCurve(Curve.X25519))
        val authenticationKeyPair = apollo.createKeyPair(seed = seed, curve = KeyCurve(Curve.ED25519))

        val did = castor.createPeerDID(
            arrayOf(keyAgreementKeyPair, authenticationKeyPair),
            services = services,
        )

        if (updateMediator) {
            return connectionManager
                .mediationHandler
                .updateKeyListWithDIDs(arrayOf(did))
                .then {
                    pluto.storePeerDIDAndPrivateKeys(
                        did = did,
                        privateKeys = listOf(keyAgreementKeyPair.privateKey, authenticationKeyPair.privateKey),
                    )
                }
                .then { did }
        } else {
            pluto.storePeerDIDAndPrivateKeys(
                did = did,
                privateKeys = listOf(keyAgreementKeyPair.privateKey, authenticationKeyPair.privateKey),
            )
            return Promise.resolve(did)
        }
    }

    fun parseInvitation(str: String): Promise<InvitationType> {
        val json = Json.decodeFromString<JsonObject>(str)
        val typeString: String = if (json.containsKey("type")) {
            json["type"].toString().trim('"')
        } else {
            ""
        }

        return when (findProtocolTypeByValue(typeString)) {
            ProtocolType.PrismOnboarding -> parsePrismInvitation(str)
            ProtocolType.Didcomminvitation -> Promise.resolve(parseOOBInvitation(str))
            else -> Promise.reject(PrismAgentError.unknownInvitationTypeError())
        }
    }

    fun acceptInvitation(invitation: PrismOnboardingInvitation) {
        @Serializable
        data class SendDID(val did: String)

        val response = api.request(
            HttpMethod.Post.toString(),
            invitation.onboardEndpoint,
            arrayOf(),
            arrayOf(),
            SendDID(invitation.from.toString()),
        ).then {
            if (it.status != 200) {
                throw PrismAgentError.failedToOnboardError()
            }
        }
    }

//    fun signWith(did: DID, message: ByteArray): Signature {
//        val privateKey = pluto.getDIDPrivateKeysByDID(did).first() ?: throw PrismAgentError.cannotFindDIDPrivateKey()
//        return apollo.signMessage(privateKey, message)
//    }

    fun parsePrismInvitation(str: String): Promise<PrismOnboardingInvitation> {
        try {
            val prismOnboarding = PrismOnboardingInvitation.prismOnboardingInvitationFromJsonString(str)
            val url = prismOnboarding.onboardEndpoint
            return createNewPeerDID(
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
            ).then {
                prismOnboarding.from = it
                prismOnboarding
            }
        } catch (e: Exception) {
            throw PrismAgentError.unknownInvitationTypeError()
        }
    }

    fun parseOOBInvitation(str: String): OutOfBandInvitation {
        try {
            return Json.decodeFromString(str)
        } catch (e: Exception) {
            throw PrismAgentError.unknownInvitationTypeError()
        }
    }
}
