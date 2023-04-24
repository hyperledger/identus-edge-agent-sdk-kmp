package io.iohk.atala.prism.sampleapp

import android.content.Context
import androidx.lifecycle.ViewModel
import io.iohk.atala.prism.walletsdk.apollo.ApolloImpl
import io.iohk.atala.prism.walletsdk.castor.CastorImpl
import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Apollo
import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Castor
import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Mercury
import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Pluto
import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Pollux
import io.iohk.atala.prism.walletsdk.domain.models.DID
import io.iohk.atala.prism.walletsdk.domain.models.Seed
import io.iohk.atala.prism.walletsdk.mercury.Api
import io.iohk.atala.prism.walletsdk.mercury.MercuryImpl
import io.iohk.atala.prism.walletsdk.mercury.resolvers.DIDCommWrapper
import io.iohk.atala.prism.walletsdk.pluto.PlutoImpl
import io.iohk.atala.prism.walletsdk.pluto.data.DbConnection
import io.iohk.atala.prism.walletsdk.pollux.PolluxImpl
import io.iohk.atala.prism.walletsdk.prismagent.PrismAgent
import io.iohk.atala.prism.walletsdk.prismagent.mediation.DefaultMediationHandler
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
    private lateinit var pollux: Pollux

    init {
    }

    fun startAgent(context: Context) {
        GlobalScope.launch {
            initializeApollo()
            initializePluto(context)
            initializeCastor()
            initializePollux()
            initializeMercury()
            initializeSeed()
            initializeHandler()
            initializeAgent()

            agent.start()
//            val prismDID = agent.createNewPrismDID()
//            println("Prism DID: $prismDID")
        }
    }

    private suspend fun initializePluto(context: Context) {
        pluto = PlutoImpl(DbConnection())
        (pluto as PlutoImpl).start(context)
    }

    private fun initializeApollo() {
        apollo = ApolloImpl()
    }

    private fun initializeCastor() {
        castor = CastorImpl(apollo)
    }

    private fun initializePollux() {
        pollux = PolluxImpl(castor)
    }

    private fun initializeMercury() {
        // This is just to make the code compile, it should be changed accordingly
        mercury = MercuryImpl(
            castor,
            DIDCommWrapper(CastorImpl(ApolloImpl()), PlutoImpl(DbConnection())),
            object : Api {
                override fun request(httpMethod: String, url: String, body: Any): ByteArray? {
                    TODO("Not yet implemented")
                }
            }
        )
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

    private fun initializeHandler() {
        handler = DefaultMediationHandler(
            mediatorDID = DID("did:peer:2.Ez6LScuuNiWo8rwnpYy5dXbq7JnVDv6yCgsAz6viRUWCUbCJk.Vz6MkfzL1tPPvpXioYDwuGQRdpATV1qb4x7mKmcXyhCmLcUGK.SeyJpZCI6Im5ldy1pZCIsInQiOiJkbSIsInMiOiJodHRwczovL21lZGlhdG9yLmpyaWJvLmtpd2kiLCJhIjpbImRpZGNvbW0vdjIiXX0"),
            mercury = mercury,
            store = DefaultMediationHandler.PlutoMediatorRepositoryImpl(pluto),
        )
    }

    private fun initializeAgent() {
        agent = PrismAgent(
            apollo = apollo,
            castor = castor,
            pluto = pluto,
            mercury = mercury,
            pollux = pollux,
            seed = seed,
            mediatorHandler = handler
        )
    }
}
