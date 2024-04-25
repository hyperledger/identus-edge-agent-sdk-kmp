package io.iohk.atala.prism.sampleapp

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
import io.iohk.atala.prism.walletsdk.prismagent.helpers.AgentOptions
import io.iohk.atala.prism.walletsdk.prismagent.helpers.Experiments
import io.iohk.atala.prism.walletsdk.prismagent.mediation.BasicMediatorHandler
import io.iohk.atala.prism.walletsdk.prismagent.mediation.MediationHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.UnknownHostException

class Sdk {
    private val apollo: Apollo = createApollo()
    private val castor: Castor = createCastor()
    private var pollux: Pollux = createPollux()
    private val seed: Seed = createSeed()
    private val agentStatusStream: MutableLiveData<PrismAgent.State> = MutableLiveData()

    val pluto: Pluto = createPluto()
    val mercury: Mercury = createMercury()

    lateinit var handler: MediationHandler
    lateinit var agent: PrismAgent

    @Throws(PrismAgentError.MediationRequestFailedError::class, UnknownHostException::class)
    suspend fun startAgent(mediatorDID: String, context: Application) {
        handler = createHandler(mediatorDID)
        agent = createAgent(handler)

        CoroutineScope(Dispatchers.Default).launch {
            agent.flowState.collect {
                agentStatusStream.postValue(it)
            }
        }

        (pluto as PlutoImpl).start(context)
        agent.start()
        agent.startFetchingMessages()

        agentStatusStream.postValue(PrismAgent.State.RUNNING)
    }

    fun stopAgent() {
        agent.let {
            it.stopFetchingMessages()
            it.stop()
        }
    }

    fun agentStatusStream(): LiveData<PrismAgent.State> {
        return agentStatusStream
    }

    private fun createPluto(): Pluto {
        return PlutoImpl(DbConnection())
    }

    private fun createApollo(): Apollo {
        return ApolloImpl()
    }

    private fun createCastor(): Castor {
        return CastorImpl(apollo)
    }

    private fun createMercury(): MercuryImpl {
        // This is just to make the code compile, it should be changed accordingly
        return MercuryImpl(castor, DIDCommWrapper(castor, pluto, apollo), ApiImpl(httpClient()))
    }

    private fun createPollux(): PolluxImpl {
        return PolluxImpl(castor)
    }

    private fun createSeed(): Seed {
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
        return apollo.createSeed(words, "")
    }

    private fun createHandler(mediatorDID: String): MediationHandler {
        return BasicMediatorHandler(
            mediatorDID = DID(mediatorDID),
            mercury = mercury,
            store = BasicMediatorHandler.PlutoMediatorRepositoryImpl(pluto)
        )
    }

    private fun createAgent(handler: MediationHandler): PrismAgent {
        return PrismAgent(
            apollo = apollo,
            castor = castor,
            pluto = pluto,
            mercury = mercury,
            pollux = pollux,
            seed = seed,
            mediatorHandler = handler,
            agentOptions = AgentOptions(Experiments(liveMode = false))
        )
    }

    companion object {
        private lateinit var instance: Sdk

        @JvmStatic
        fun getInstance(): Sdk {
            if (!this::instance.isInitialized) {
                instance = Sdk()
            }
            return instance
        }
    }
}
