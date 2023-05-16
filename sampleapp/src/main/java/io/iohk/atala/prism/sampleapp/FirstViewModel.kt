package io.iohk.atala.prism.sampleapp

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.iohk.atala.prism.walletsdk.apollo.ApolloImpl
import io.iohk.atala.prism.walletsdk.castor.CastorImpl
import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Apollo
import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Castor
import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Mercury
import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Pluto
import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Pollux
import io.iohk.atala.prism.walletsdk.domain.models.ApiImpl
import io.iohk.atala.prism.walletsdk.domain.models.DID
import io.iohk.atala.prism.walletsdk.domain.models.DIDDocument
import io.iohk.atala.prism.walletsdk.domain.models.Message
import io.iohk.atala.prism.walletsdk.domain.models.PrismAgentError
import io.iohk.atala.prism.walletsdk.domain.models.Seed
import io.iohk.atala.prism.walletsdk.domain.models.httpClient
import io.iohk.atala.prism.walletsdk.mercury.MercuryImpl
import io.iohk.atala.prism.walletsdk.mercury.resolvers.DIDCommWrapper
import io.iohk.atala.prism.walletsdk.pluto.PlutoImpl
import io.iohk.atala.prism.walletsdk.pluto.data.DbConnection
import io.iohk.atala.prism.walletsdk.pollux.PolluxImpl
import io.iohk.atala.prism.walletsdk.prismagent.PrismAgent
import io.iohk.atala.prism.walletsdk.prismagent.mediation.BasicMediatorHandler
import io.iohk.atala.prism.walletsdk.prismagent.mediation.MediationHandler
import io.iohk.atala.prism.walletsdk.prismagent.protocols.outOfBand.OutOfBandInvitation
import io.iohk.atala.prism.walletsdk.prismagent.protocols.outOfBand.PrismOnboardingInvitation
import kotlinx.coroutines.launch
import java.lang.Exception
import kotlin.jvm.Throws

class FirstViewModel : ViewModel() {

    private lateinit var apollo: Apollo
    private lateinit var castor: Castor
    private lateinit var pluto: Pluto
    private lateinit var mercury: Mercury
    private lateinit var handler: MediationHandler
    private lateinit var seed: Seed
    private lateinit var agent: PrismAgent
    private lateinit var pollux: Pollux
    private val messageList: MutableLiveData<List<Message>> = MutableLiveData(listOf())
    private val notification: MutableLiveData<String> = MutableLiveData("")
    private val agentState: MutableLiveData<String> = MutableLiveData("")

    fun startAgent(context: Context) {
        viewModelScope.launch {
            initializeApollo()
            initializePluto(context)
            initializeCastor()
            initializePollux()
            initializeMercury()
            initializeSeed()
            initializeHandler()
            initializeAgent()

            agent.start()
            agent.startFetchingMessages()
            agent.handleReceivedMessagesEvents().collect { messages ->
                messageList.postValue(messages)
            }
        }
    }

    fun stopAgent() {
        if (this::agent.isInitialized) {
            viewModelScope.launch {
                agent.stopFetchingMessages()
                agent.stop()
            }
        }
    }

    fun messageListStream(): LiveData<List<Message>> {
        return messageList
    }

    fun notificationListStream(): LiveData<String> {
        return notification
    }

    fun agentStateStream(): LiveData<String> {
        return agentState
    }

    @Throws(Exception::class, PrismAgentError.UnknownInvitationTypeError::class)
    fun parseAndAcceptOOB(oobUrl: String) {
        if (this::agent.isInitialized.not()) {
            throw Exception("Agent has not been started")
        }
        viewModelScope.launch {
            when (val invitation = agent.parseInvitation(oobUrl)) {
                is OutOfBandInvitation -> {
                    agent.acceptOutOfBandInvitation(invitation)
                }
                is PrismOnboardingInvitation -> {
                    agent.acceptInvitation(invitation)
                }
                else -> {
                    throw PrismAgentError.UnknownInvitationTypeError()
                }
            }
        }
    }

    fun sendTestMessage(did: DID) {
        viewModelScope.launch {
//            val senderPeerDid = agent.createNewPeerDID(
//                emptyArray(),
//                true
//            )
            val message = Message(
                piuri = "https://didcomm.org/basicmessage/2.0/message", // TODO: This should be on ProtocolTypes as an enum
                from = did,
                to = did,
                body = "{\"msg\":\"This is a test message\"}"
            )

            println("Send message")
            mercury.sendMessage(message)
        }
    }

    fun createPeerDid() {
        viewModelScope.launch {
            val did = agent.createNewPeerDID(
                arrayOf(
                    DIDDocument.Service(
                        "#didcomm-1",
                        arrayOf("DIDCommMessaging"),
                        DIDDocument.ServiceEndpoint(handler.mediatorDID.toString()),
                    ),
                ),
                true
            )
            println(did.toString())
            sendTestMessage(did)
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
            DIDCommWrapper(castor, pluto, apollo),
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
        seed = apollo.createSeed(words, "")
    }

    private fun initializeHandler() {
        // Fabio's mediatorDID = DID("did:peer:2.Ez6LSghwSE437wnDE1pt3X6hVDUQzSjsHzinpX3XFvMjRAm7y.Vz6Mkhh1e5CEYYq6JBUcTZ6Cp2ranCWRrv7Yax3Le4N59R6dd.SeyJ0IjoiZG0iLCJzIjoiaHR0cHM6Ly9hbGljZS5kaWQuZm1ncC5hcHAvIiwiciI6W10sImEiOlsiZGlkY29tbS92MiJdfQ"),
        handler = BasicMediatorHandler(
            mediatorDID = DID("did:peer:2.Ez6LSiekedip45fb5uYRZ9DV1qVvf3rr6GpvTGLhw3nKJ9E7X.Vz6MksZCnX3hQVPP4wWDGe1Dzq5LCk5BnGUnPmq3YCfrPpfuk.SeyJpZCI6Im5ldy1pZCIsInQiOiJkbSIsInMiOiJodHRwczovL21lZGlhdG9yLmpyaWJvLmtpd2kiLCJhIjpbImRpZGNvbW0vdjIiXX0"),
            mercury = mercury,
            store = BasicMediatorHandler.PlutoMediatorRepositoryImpl(pluto),
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
        viewModelScope.launch {
            agent.getFlowState().collect {
                agentState.postValue(it.name)
            }
        }
    }
}
