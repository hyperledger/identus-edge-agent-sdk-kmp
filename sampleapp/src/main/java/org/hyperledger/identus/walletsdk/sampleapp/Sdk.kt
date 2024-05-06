package org.hyperledger.identus.walletsdk.sampleapp

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.hyperledger.identus.walletsdk.apollo.ApolloImpl
import org.hyperledger.identus.walletsdk.castor.CastorImpl
import org.hyperledger.identus.walletsdk.domain.buildingblocks.Apollo
import org.hyperledger.identus.walletsdk.domain.buildingblocks.Castor
import org.hyperledger.identus.walletsdk.domain.buildingblocks.Mercury
import org.hyperledger.identus.walletsdk.domain.buildingblocks.Pluto
import org.hyperledger.identus.walletsdk.domain.buildingblocks.Pollux
import org.hyperledger.identus.walletsdk.domain.models.ApiImpl
import org.hyperledger.identus.walletsdk.domain.models.DID
import org.hyperledger.identus.walletsdk.domain.models.Seed
import org.hyperledger.identus.walletsdk.domain.models.httpClient
import org.hyperledger.identus.walletsdk.edgeagent.PrismAgent
import org.hyperledger.identus.walletsdk.edgeagent.PrismAgentError
import org.hyperledger.identus.walletsdk.edgeagent.helpers.AgentOptions
import org.hyperledger.identus.walletsdk.edgeagent.helpers.Experiments
import org.hyperledger.identus.walletsdk.edgeagent.mediation.BasicMediatorHandler
import org.hyperledger.identus.walletsdk.edgeagent.mediation.MediationHandler
import org.hyperledger.identus.walletsdk.mercury.MercuryImpl
import org.hyperledger.identus.walletsdk.mercury.resolvers.DIDCommWrapper
import org.hyperledger.identus.walletsdk.pluto.PlutoImpl
import org.hyperledger.identus.walletsdk.pluto.data.DbConnection
import org.hyperledger.identus.walletsdk.pollux.PolluxImpl
import java.net.UnknownHostException
import org.hyperledger.identus.walletsdk.castor.resolvers.PrismDIDApiResolver

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
        val castor = CastorImpl(apollo)
        val prismDIDApiResolver = PrismDIDApiResolver(this.apollo, "https://sit-prism-agent-issuer.atalaprism.io/prism-agent")
        castor.addResolver(prismDIDApiResolver)
        return castor
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
            mediatorHandler = handler
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
