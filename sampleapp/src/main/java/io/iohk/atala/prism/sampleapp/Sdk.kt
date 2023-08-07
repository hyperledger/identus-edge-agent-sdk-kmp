package io.iohk.atala.prism.sampleapp

import android.content.Context
import io.iohk.atala.prism.walletsdk.apollo.ApolloImpl
import io.iohk.atala.prism.walletsdk.castor.CastorImpl
import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Apollo
import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Castor
import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Mercury
import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Pluto
import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Pollux
import io.iohk.atala.prism.walletsdk.domain.models.ApiImpl
import io.iohk.atala.prism.walletsdk.domain.models.DID
import io.iohk.atala.prism.walletsdk.domain.models.Seed
import io.iohk.atala.prism.walletsdk.domain.models.httpClient
import io.iohk.atala.prism.walletsdk.mercury.MercuryImpl
import io.iohk.atala.prism.walletsdk.mercury.resolvers.DIDCommWrapper
import io.iohk.atala.prism.walletsdk.pluto.PlutoImpl
import io.iohk.atala.prism.walletsdk.pluto.data.DbConnection
import io.iohk.atala.prism.walletsdk.pollux.PolluxImpl
import io.iohk.atala.prism.walletsdk.prismagent.PrismAgent
import io.iohk.atala.prism.walletsdk.prismagent.PrismAgentError
import io.iohk.atala.prism.walletsdk.prismagent.mediation.BasicMediatorHandler
import io.iohk.atala.prism.walletsdk.prismagent.mediation.MediationHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.UnknownHostException

class Sdk() {

    private var apollo: Apollo? = null
        get() {
            return field ?: throw IllegalStateException("You haven't started the agent")
        }

    private var castor: Castor? = null
        get() {
            return field ?: throw IllegalStateException("You haven't started the agent")
        }

    var pluto: Pluto? = null
        get() {
            return field ?: throw IllegalStateException("You haven't started the agent")
        }

    var mercury: Mercury? = null
        get() {
            return field ?: throw IllegalStateException("You haven't started the agent")
        }

    var handler: MediationHandler? = null
        private set
        get() {
            return field ?: throw IllegalStateException("You haven't started the agent")
        }

    private var seed: Seed? = null
        private set
        get() {
            return field ?: throw IllegalStateException("You haven't started the agent")
        }

    private var pollux: Pollux? = null
        private set
        get() {
            return field ?: throw IllegalStateException("You haven't started the agent")
        }

    var agent: PrismAgent? = null
        private set
        get() {
            return field
        }

    @Throws(PrismAgentError.MediationRequestFailedError::class, UnknownHostException::class)
    suspend fun startAgent() {
        agent?.let {
            it.start()
            it.startFetchingMessages()
        }
    }

    fun stopAgent() {
        agent?.let {
            it.stopFetchingMessages()
            it.stop()
        }
    }

    private suspend fun initializeComponents(context: Context) {
        initializeApollo()
        initializePluto(context)
        initializeCastor()
        initializePollux()
        initializeMercury()
        initializeSeed()
        initializeHandler()
        initializeAgent()
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

    private fun initializePollux() {
        pollux = PolluxImpl(castor!!)
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
        handler = BasicMediatorHandler(
            // Roots ID
            mediatorDID = DID("did:peer:2.Ez6LSms555YhFthn1WV8ciDBpZm86hK9tp83WojJUmxPGk1hZ.Vz6MkmdBjMyB4TS5UbbQw54szm8yvMMf1ftGV2sQVYAxaeWhE.SeyJpZCI6Im5ldy1pZCIsInQiOiJkbSIsInMiOiJodHRwczovL21lZGlhdG9yLnJvb3RzaWQuY2xvdWQiLCJhIjpbImRpZGNvbW0vdjIiXX0"),
            // Prism Mediator
            // mediatorDID = DID("did:peer:2.Ez6LSghwSE437wnDE1pt3X6hVDUQzSjsHzinpX3XFvMjRAm7y.Vz6Mkhh1e5CEYYq6JBUcTZ6Cp2ranCWRrv7Yax3Le4N59R6dd.SeyJ0IjoiZG0iLCJzIjoiaHR0cHM6Ly9zaXQtcHJpc20tbWVkaWF0b3IuYXRhbGFwcmlzbS5pbyIsInIiOltdLCJhIjpbImRpZGNvbW0vdjIiXX0"),
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
            pollux = pollux!!,
            seed = seed,
            mediatorHandler = handler!!
        )
    }

    companion object {
        private var instance: Sdk? = null

        fun getInstance(context: Context): Sdk {
            if (instance == null) {
                instance = Sdk()
                CoroutineScope(Dispatchers.Default).launch {
                    instance!!.initializeComponents(context)
                }
            }
            return instance!!
        }
    }
}
