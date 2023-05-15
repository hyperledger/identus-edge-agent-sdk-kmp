package io.iohk.atala.prism.sampleapp

import android.app.Application
import android.content.Context
import io.iohk.atala.prism.walletsdk.apollo.ApolloImpl
import io.iohk.atala.prism.walletsdk.castor.CastorImpl
import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Apollo
import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Castor
import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Mercury
import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Pluto
import io.iohk.atala.prism.walletsdk.domain.models.ApiImpl
import io.iohk.atala.prism.walletsdk.domain.models.DID
import io.iohk.atala.prism.walletsdk.domain.models.Seed
import io.iohk.atala.prism.walletsdk.domain.models.httpClient
import io.iohk.atala.prism.walletsdk.mercury.MercuryImpl
import io.iohk.atala.prism.walletsdk.mercury.resolvers.DIDCommWrapper
import io.iohk.atala.prism.walletsdk.pluto.PlutoImpl
import io.iohk.atala.prism.walletsdk.pluto.data.DbConnection
import io.iohk.atala.prism.walletsdk.prismagent.PrismAgent
import io.iohk.atala.prism.walletsdk.prismagent.mediation.BasicMediatorHandler
import io.iohk.atala.prism.walletsdk.prismagent.mediation.MediationHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class SampleApplication : Application() {

    var apollo: Apollo? = null
        private set
        get() {
            return field ?: throw IllegalStateException("You haven't started the agent")
        }

    var castor: Castor? = null
        private set
        get() {
            return field ?: throw IllegalStateException("You haven't started the agent")
        }

    var pluto: Pluto? = null
        private set
        get() {
            return field ?: throw IllegalStateException("You haven't started the agent")
        }

    var mercury: Mercury? = null
        private set
        get() {
            return field ?: throw IllegalStateException("You haven't started the agent")
        }

    var handler: MediationHandler? = null
        private set
        get() {
            return field ?: throw IllegalStateException("You haven't started the agent")
        }

    var seed: Seed? = null
        private set
        get() {
            return field ?: throw IllegalStateException("You haven't started the agent")
        }

    var agent: PrismAgent? = null
        private set
        get() {
            return field
        }

    suspend fun getAgent(context: Context): PrismAgent {
        initializeApollo()
        initializePluto(context)
        initializeCastor()
        initializeMercury()
        initializeSeed()
        initializeHandler()
        initializeAgent()
        return agent!!
    }

    suspend fun startAgent() {
        try {
            agent?.let {
                it.start()
                it.startFetchingMessages()
            }
        } catch (e: Exception) {
            println("Start agent")
        }
    }

    fun stopAgent() {
        agent?.let {
            try {
                it.stopFetchingMessages()
                it.stop()
            } catch (e: Exception) {
                println("stop agent")
            }
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
        castor = CastorImpl(apollo!!)
    }

    private fun initializeMercury() {
        // This is just to make the code compile, it should be changed accordingly
        mercury = MercuryImpl(
            castor!!,
            DIDCommWrapper(castor!!, pluto!!, apollo!!),
            ApiImpl(httpClient())
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
        seed = apollo!!.createSeed(words, "")
    }

    private fun initializeHandler() {
        // Fabio's mediatorDID = DID("did:peer:2.Ez6LSghwSE437wnDE1pt3X6hVDUQzSjsHzinpX3XFvMjRAm7y.Vz6Mkhh1e5CEYYq6JBUcTZ6Cp2ranCWRrv7Yax3Le4N59R6dd.SeyJ0IjoiZG0iLCJzIjoiaHR0cHM6Ly9hbGljZS5kaWQuZm1ncC5hcHAvIiwiciI6W10sImEiOlsiZGlkY29tbS92MiJdfQ"),
        handler = BasicMediatorHandler(
            mediatorDID = DID("did:peer:2.Ez6LSiekedip45fb5uYRZ9DV1qVvf3rr6GpvTGLhw3nKJ9E7X.Vz6MksZCnX3hQVPP4wWDGe1Dzq5LCk5BnGUnPmq3YCfrPpfuk.SeyJpZCI6Im5ldy1pZCIsInQiOiJkbSIsInMiOiJodHRwczovL21lZGlhdG9yLmpyaWJvLmtpd2kiLCJhIjpbImRpZGNvbW0vdjIiXX0"),
            mercury = mercury!!,
            store = BasicMediatorHandler.PlutoMediatorRepositoryImpl(pluto!!)
        )
    }

    private fun initializeAgent() {
        agent = PrismAgent(
            apollo = apollo!!,
            castor = castor!!,
            pluto = pluto!!,
            mercury = mercury!!,
            seed = seed,
            mediatorHandler = handler!!
        )
    }
}
