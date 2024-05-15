package io.iohk.atala.prism.walletsdk.prismagent.mediation

import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Pluto
import io.iohk.atala.prism.walletsdk.domain.models.Mediator

/**
 * An interface representing a store for storing and retrieving mediators. The MediatorStore protocol defines methods
 * for storing and retrieving mediators.
 */
interface MediatorRepository {

    /**
     * Stores a mediator in the [Pluto] store.
     *
     * @param mediator The [Mediator] object to store.
     */
    fun storeMediator(mediator: Mediator)

    /**
     * Fetches all the mediators from the [Pluto] store.
     *
     * @return An array of [Mediator] objects.
     */
    suspend fun getAllMediators(): List<Mediator>
}
