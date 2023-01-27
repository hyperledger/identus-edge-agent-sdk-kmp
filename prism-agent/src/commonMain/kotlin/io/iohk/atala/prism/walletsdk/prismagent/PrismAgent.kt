package io.iohk.atala.prism.walletsdk.prismagent

import io.iohk.atala.prism.domain.buildingBlocks.Apollo
import io.iohk.atala.prism.domain.buildingBlocks.Castor
import io.iohk.atala.prism.domain.buildingBlocks.Pluto
import io.iohk.atala.prism.domain.models.DID
import io.iohk.atala.prism.domain.models.DIDDocument
import io.iohk.atala.prism.domain.models.KeyCurve
import io.iohk.atala.prism.domain.models.PrismAgentError
import io.iohk.atala.prism.domain.models.Seed
import io.iohk.atala.prism.walletsdk.prismagent.protocols.PrismOnboarding.PrismOnboardingInvitation
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

final class PrismAgent {
    enum class State {
        STOPED, STARTING, RUNNING, STOPING
    }

    sealed class InvitationType {
        class PrismOnboarding(val from: String, val endpoint: String, val ownDID: DID)

        data class OnboardingPrism(val prismOnboarding: PrismOnboarding) : InvitationType()
    }

    val seed: Seed
    var state = State.STOPED

    private val apollo: Apollo
    private val castor: Castor
    private val pluto: Pluto

    constructor(
        apollo: Apollo,
        castor: Castor,
        pluto: Pluto,
        seed: Seed? = null,
    ) {
        this.apollo = apollo
        this.castor = castor
        this.pluto = pluto
        this.seed = seed ?: apollo.createRandomSeed().second
    }

    suspend fun createNewPrismDID(
        keyPathIndex: Int? = null,
        alias: String? = null,
        services: Array<DIDDocument.Service> = emptyArray()
    ): DID {
        var index = 0
        val newDID = pluto
            .getPrismLastKeyPairIndex()
            .map {
                index = keyPathIndex ?: it
                val keyPair = apollo.createKeyPair(seed = seed, curve = KeyCurve.SECP256K1)
                castor.createPrismDID(masterPublicKey = keyPair.publicKey, services = services)
            }
            .first()
        pluto.storePrismDID(did = newDID, keyPairIndex = index, alias = alias)
        return newDID
    }

    suspend fun createNewPeerDID(
        services: Array<DIDDocument.Service> = emptyArray(),
        updateMediator: Boolean
    ): DID {
        val keyAgreementKeyPair = apollo.createKeyPair(seed = seed, curve = KeyCurve.X25519)
        val authenticationKeyPair = apollo.createKeyPair(seed = seed, curve = KeyCurve.ED25519)

        val did = castor.createPeerDID(
            arrayOf(keyAgreementKeyPair, authenticationKeyPair),
            services = services
        )

        if (updateMediator) {
            // TODO: This still needs to be done update the key List
        }

        pluto.storePeerDID(did = did, privateKeys = arrayOf(keyAgreementKeyPair.privateKey, authenticationKeyPair.privateKey))

        return did
    }

    @Throws(PrismAgentError.unknownInvitationTypeError::class)
    suspend fun parseInvitation(str: String): InvitationType {
        return try {
            InvitationType.OnboardingPrism(parsePrismInvitation(str))
        } catch (e: Throwable) {
            throw PrismAgentError.unknownInvitationTypeError()
        }
    }

    private suspend fun parsePrismInvitation(str: String): InvitationType.PrismOnboarding {
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

        return InvitationType.PrismOnboarding(
            from = prismOnboarding.body.from,
            endpoint = url,
            ownDID = did
        )
    }
}
