package io.iohk.atala.prism.walletsdk.prismagent

import io.iohk.atala.prism.domain.buildingBlocks.Apollo
import io.iohk.atala.prism.domain.buildingBlocks.Castor
import io.iohk.atala.prism.domain.buildingBlocks.Pluto
import io.iohk.atala.prism.domain.models.Curve
import io.iohk.atala.prism.domain.models.DID
import io.iohk.atala.prism.domain.models.DIDDocument
import io.iohk.atala.prism.domain.models.KeyCurve
import io.iohk.atala.prism.domain.models.PrismAgentError
import io.iohk.atala.prism.domain.models.Seed
import io.iohk.atala.prism.domain.models.Signature
import io.iohk.atala.prism.walletsdk.prismagent.helpers.ApiImpl
import io.iohk.atala.prism.walletsdk.prismagent.helpers.HttpClient
import io.iohk.atala.prism.walletsdk.prismagent.protocols.prismOnboarding.PrismOnboardingInvitation
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpMethod
import io.ktor.http.Url
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

final class PrismAgent {
    enum class State {
        STOPED, STARTING, RUNNING, STOPING
    }

    sealed class InvitationType

    data class PrismOnboardingInvitation(val from: String, val endpoint: String, val ownDID: DID) : InvitationType()

    val seed: Seed
    var state = State.STOPED

    private val apollo: Apollo
    private val castor: Castor
    private val pluto: Pluto
    private val api: ApiImpl

    constructor(
        apollo: Apollo,
        castor: Castor,
        pluto: Pluto,
        seed: Seed? = null,
        api: ApiImpl? = null
    ) {
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
        var index = 0
        val newDID = pluto
            .getPrismLastKeyPathIndex()
            .map {
                index = keyPathIndex ?: it
                val keyPair = apollo.createKeyPair(seed = seed, curve = KeyCurve(Curve.SECP256K1, index))
                val did = castor.createPrismDID(masterPublicKey = keyPair.publicKey, services = services)
                pluto.storePrivateKeys(keyPair.privateKey, did, index)
                return@map did
            }
            .first()
        pluto.storePrismDID(did = newDID, keyPathIndex = index, alias = alias)
        return newDID
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
        val invite = try {
            parsePrismInvitation(str)
        } catch (e: Throwable) {
            println(e)
            throw PrismAgentError.unknownInvitationTypeError()
        }
        return invite
    }

    suspend fun acceptInvitation(invitation: PrismOnboardingInvitation) {
        @Serializable
        data class SendDID(val did: String)

        var response = api.request(
            HttpMethod.Post,
            Url(invitation.endpoint),
            mapOf(),
            mapOf(),
            SendDID(invitation.ownDID.toString())
        )

        if (response.status.value != 200) {
            throw PrismAgentError.failedToOnboardError()
        }
    }

    suspend fun signWith(did: DID, message: ByteArray): Signature {
        return pluto.getDIDPrivateKeysByDID(did)
            .firstOrNull()
            ?.let { privateKeys ->
                apollo.signMessage(privateKeys.first(), message)
            } ?: throw PrismAgentError.cannotFindDIDPrivateKey()
    }

    private suspend fun parsePrismInvitation(str: String): PrismOnboardingInvitation {
        val prismOnboarding = PrismOnboardingInvitation(str)
        val url = prismOnboarding.body.onboardEndpoint
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

        return PrismOnboardingInvitation(
            from = prismOnboarding.body.from,
            endpoint = url,
            ownDID = did
        )
    }
}
