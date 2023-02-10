package io.iohk.atala.prism.walletsdk.prismagent

import io.iohk.atala.prism.walletsdk.domain.buildingBlocks.Apollo
import io.iohk.atala.prism.walletsdk.domain.buildingBlocks.Castor
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

actual class PrismAgent actual constructor(
    apollo: Apollo,
    castor: Castor,
    pluto: Pluto,
    seed: Seed?,
    api: Api?
) {
    actual enum class State {
        STOPPED, STARTING, RUNNING, STOPPING
    }

    actual var state: State = State.STOPPED
    actual val seed: Seed
    actual val apollo: Apollo
    actual val castor: Castor
    actual val pluto: Pluto

    private val api: Api

    init {
        this.apollo = apollo
        this.castor = castor
        this.pluto = pluto
        this.seed = seed ?: apollo.createRandomSeed().second
        this.api = api ?: ApiImpl(
            HttpClient {
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

    suspend fun createNewPrismDID(
        keyPathIndex: Int? = null,
        alias: String? = null,
        services: Array<DIDDocument.Service> = emptyArray()
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
        updateMediator: Boolean
    ): DID {
        val keyAgreementKeyPair = apollo.createKeyPair(seed = seed, curve = KeyCurve(Curve.X25519))
        val authenticationKeyPair = apollo.createKeyPair(seed = seed, curve = KeyCurve(Curve.ED25519))

        val did = castor.createPeerDID(
            arrayOf(keyAgreementKeyPair, authenticationKeyPair),
            services = services
        )

        if (updateMediator) {
            // TODO: This still needs to be done update the key List
        }

        pluto.storePeerDID(
            did = did,
            privateKeys = arrayOf(keyAgreementKeyPair.privateKey, authenticationKeyPair.privateKey)
        )

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
            SendDID(invitation.from.toString())
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
                            routingKeys = arrayOf()
                        )
                    )
                ),
                true
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
