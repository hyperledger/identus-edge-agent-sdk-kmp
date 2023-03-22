package io.iohk.atala.prism.sampleapp

import androidx.lifecycle.ViewModel
import io.iohk.atala.prism.walletsdk.apollo.ApolloImpl
import io.iohk.atala.prism.walletsdk.castor.CastorImpl
import io.iohk.atala.prism.walletsdk.domain.buildingBlocks.Apollo
import io.iohk.atala.prism.walletsdk.domain.buildingBlocks.Castor
import io.iohk.atala.prism.walletsdk.domain.buildingBlocks.Mercury
import io.iohk.atala.prism.walletsdk.domain.buildingBlocks.Pluto
import io.iohk.atala.prism.walletsdk.domain.models.Seed
import io.iohk.atala.prism.walletsdk.mercury.MercuryImpl
import io.iohk.atala.prism.walletsdk.pluto.PlutoImpl
import io.iohk.atala.prism.walletsdk.pluto.data.DbConnection
import io.iohk.atala.prism.walletsdk.prismagent.PrismAgent
import io.iohk.atala.prism.walletsdk.prismagent.mediation.MediationHandler
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class FirstViewModel : ViewModel() {

    private lateinit var apollo: Apollo
    private lateinit var castor: Castor
    private lateinit var pluto: Pluto
    private lateinit var mercury: Mercury
    private lateinit var handler: MediationHandler
    private lateinit var seed: Seed
    private lateinit var agent: PrismAgent

    fun startAgent() {
        initializeApollo()
        initializePluto()
        initializeCastor()
        initializeMercury()
        initializeSeed()
        initializeAgent()

        GlobalScope.launch {
            agent.start()
        }
    }

    private fun initializePluto() {
        pluto = PlutoImpl(DbConnection())
    }

    private fun initializeApollo() {
        apollo = ApolloImpl()
    }

    private fun initializeCastor() {
        castor = CastorImpl(apollo)
    }

    private fun initializeMercury() {
        mercury = MercuryImpl(castor, pluto)
    }

    private fun initializeSeed() {
        val words = arrayOf(
            "trumpet",
            "mass",
            "anger",
            "eyebrow",
            "gadget",
            "sword",
            "debate",
            "spend",
            "move",
            "noble",
            "motor",
            "common",
            "junk",
            "feed",
            "alone",
            "whip",
            "feed",
            "front",
            "radio",
            "rookie",
            "settle",
            "provide",
            "admit",
            "peanut"
        )
        seed = apollo.createSeed(words, "")
    }

    private fun initializeAgent() {
        agent = PrismAgent(
            apollo = apollo,
            castor = castor,
            pluto = pluto,
            mercury = mercury,
            seed = seed,
            mediatorHandler = handler
        )
    }
}
