package io.iohk.atala.prism.walletsdk.prismagent

import io.iohk.atala.prism.domain.buildingBlocks.Apollo
import io.iohk.atala.prism.domain.buildingBlocks.Castor
import io.iohk.atala.prism.domain.buildingBlocks.Pluto
import io.iohk.atala.prism.domain.models.Curve
import io.iohk.atala.prism.domain.models.DID
import io.iohk.atala.prism.domain.models.DIDDocument
import io.iohk.atala.prism.domain.models.KeyCurve
import io.iohk.atala.prism.domain.models.Seed
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

final class PrismAgent {
    enum class State {
        STOPED, STARTING, RUNNING, STOPING
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
            .getPrismLastKeyPathIndex()
            .map {
                index = keyPathIndex ?: it
                val keyPair = apollo.createKeyPair(seed = seed, curve = KeyCurve(Curve.SECP256K1, index))
                castor.createPrismDID(masterPublicKey = keyPair.publicKey, services = services)
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

        pluto.storePeerDID(did = did, privateKeys = arrayOf(keyAgreementKeyPair.privateKey, authenticationKeyPair.privateKey))

        return did
    }
}
