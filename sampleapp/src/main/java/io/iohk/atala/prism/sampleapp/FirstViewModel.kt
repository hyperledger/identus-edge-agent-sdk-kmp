package io.iohk.atala.prism.sampleapp

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.iohk.atala.prism.walletsdk.apollo.ApolloImpl
import io.iohk.atala.prism.walletsdk.castor.CastorImpl
import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Apollo
import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Castor
import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Mercury
import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Pluto
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
import io.iohk.atala.prism.walletsdk.prismagent.models.OutOfBandInvitation
import io.iohk.atala.prism.walletsdk.prismagent.models.PrismOnboardingInvitation
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class FirstViewModel : ViewModel() {

    private lateinit var apollo: Apollo
    private lateinit var castor: Castor
    private lateinit var pluto: Pluto
    private lateinit var mercury: Mercury
    private lateinit var handler: MediationHandler
    private lateinit var seed: Seed
    private lateinit var agent: PrismAgent
    private val messageList: MutableLiveData<List<Message>> = MutableLiveData(listOf())
    private val notification: MutableLiveData<String> = MutableLiveData("")

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
            createPeerDid()
//            agent.startFetchingMessages()
//            agent.handleReceivedMessagesEvents().collect { messages ->
//                messages.map {
//                    if (it.piuri == ProtocolType.PrismOnboarding.value) {
//                    } else if (it.piuri == ProtocolType.Didcomminvitation.value) {
//                    }
//                }
//            }
        }
    }

    fun stopAgent() {
        agent?.let {
            GlobalScope.launch {
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

    fun parseAndAcceptOOB(oobUrl: String) {
        GlobalScope.launch {
            val invitationType = agent.parseInvitation(oobUrl)
            var invitation: Any
            if (invitationType is OutOfBandInvitation) {
                invitation = Json.decodeFromString<OutOfBandInvitation>(oobUrl)
//                agent.acceptOutOfBandInvitation(invitation)
            } else if (invitationType is PrismOnboardingInvitation) {
                invitation = Json.decodeFromString<PrismOnboardingInvitation>(oobUrl)
                agent.acceptInvitation(invitation)
            } else {
                throw PrismAgentError.UnknownInvitationTypeError()
            }
        }
    }

    fun sendTestMessage(did: DID) {
        GlobalScope.launch {
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
        GlobalScope.launch {
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
        // old one: DID("did:peer:2.Ez6LScuuNiWo8rwnpYy5dXbq7JnVDv6yCgsAz6viRUWCUbCJk.Vz6MkfzL1tPPvpXioYDwuGQRdpATV1qb4x7mKmcXyhCmLcUGK.SeyJpZCI6Im5ldy1pZCIsInQiOiJkbSIsInMiOiJodHRwczovL21lZGlhdG9yLmpyaWJvLmtpd2kiLCJhIjpbImRpZGNvbW0vdjIiXX0"),
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
    }
}
