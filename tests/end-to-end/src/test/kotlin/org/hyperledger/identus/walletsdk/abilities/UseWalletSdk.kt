package org.hyperledger.identus.walletsdk.abilities

import com.jayway.jsonpath.JsonPath
import io.iohk.atala.automation.utils.Logger
import io.restassured.RestAssured
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.serenitybdd.screenplay.Ability
import net.serenitybdd.screenplay.Actor
import net.serenitybdd.screenplay.Question
import net.serenitybdd.screenplay.SilentInteraction
import org.hyperledger.identus.walletsdk.apollo.ApolloImpl
import org.hyperledger.identus.walletsdk.castor.CastorImpl
import org.hyperledger.identus.walletsdk.configuration.DbConnectionInMemory
import org.hyperledger.identus.walletsdk.configuration.Environment
import org.hyperledger.identus.walletsdk.domain.models.ApiImpl
import org.hyperledger.identus.walletsdk.domain.models.DID
import org.hyperledger.identus.walletsdk.domain.models.Message
import org.hyperledger.identus.walletsdk.domain.models.Seed
import org.hyperledger.identus.walletsdk.domain.models.httpClient
import org.hyperledger.identus.walletsdk.edgeagent.EdgeAgent
import org.hyperledger.identus.walletsdk.edgeagent.helpers.AgentOptions
import org.hyperledger.identus.walletsdk.edgeagent.helpers.Experiments
import org.hyperledger.identus.walletsdk.edgeagent.mediation.BasicMediatorHandler
import org.hyperledger.identus.walletsdk.edgeagent.protocols.ProtocolType
import org.hyperledger.identus.walletsdk.mercury.MercuryImpl
import org.hyperledger.identus.walletsdk.mercury.resolvers.DIDCommWrapper
import org.hyperledger.identus.walletsdk.pluto.PlutoImpl
import org.hyperledger.identus.walletsdk.pollux.PolluxImpl
import org.hyperledger.identus.walletsdk.workflow.EdgeAgentWorkflow
import java.util.Base64
import java.util.Collections


class UseWalletSdk : Ability {
    companion object {
        private fun `as`(actor: Actor): UseWalletSdk {
            if (actor.abilityTo(UseWalletSdk::class.java) != null) {
                val ability = actor.abilityTo(UseWalletSdk::class.java)
                if (!ability.isInitialized) {
                    ability.initialize()
                }
            }
            return actor.abilityTo(UseWalletSdk::class.java) ?: throw ActorCannotUseWalletSdk(actor)
        }

        fun credentialOfferStackSize(): Question<Int> {
            return Question.about("credential offer stack").answeredBy {
                `as`(it).context.credentialOfferStack.size
            }
        }

        fun issuedCredentialStackSize(): Question<Int> {
            return Question.about("issued credential stack").answeredBy {
                `as`(it).context.issuedCredentialStack.size
            }
        }

        fun proofOfRequestStackSize(): Question<Int> {
            return Question.about("proof of request stack").answeredBy {
                `as`(it).context.proofRequestStack.size
            }
        }

        fun revocationStackSize(): Question<Int> {
            return Question.about("revocation messages stack").answeredBy {
                `as`(it).context.revocationNotificationStack.size
            }
        }

        fun execute(callback: suspend (sdk: SdkContext) -> Unit): SilentInteraction {
            return object : SilentInteraction() {
                override fun <T : Actor> performAs(actor: T) {
                    val asActor = `as`(actor)
                    runBlocking {
                        callback(asActor.context)
                    }
                }
            }
        }

        fun stop(): SilentInteraction {
            return object : SilentInteraction() {
                override fun <T : Actor> performAs(actor: T) {
                    val walletSdk = `as`(actor)
                    runBlocking {
                        walletSdk.context.sdk.stopFetchingMessages()
                        walletSdk.context.sdk.stop()
                        walletSdk.isInitialized = false
                    }
                }

            }
        }
    }

    private val logger = Logger.get<EdgeAgentWorkflow>()
    lateinit var context: SdkContext
    private val receivedMessages = mutableListOf<String>()
    private var isInitialized = false

    fun initialize() {
        createSdk()
        startPluto()
        startSdk()
        listenToMessages()
        isInitialized = true
    }

    fun recoverWallet(seed: Seed, jwe: String) {
        createSdk(seed)
        startPluto()
        runBlocking {
            context.sdk.recoverWallet(jwe)
        }
        startSdk()
        listenToMessages()
        isInitialized = true
    }

    private fun createSdk(initialSeed: Seed? = null) {
        val api = ApiImpl(httpClient())
        val apollo = ApolloImpl()
        val castor = CastorImpl(apollo)
        val pluto = PlutoImpl(DbConnectionInMemory())
        val pollux = PolluxImpl(apollo, castor, api)
        val didcommWrapper = DIDCommWrapper(castor, pluto, apollo)
        val mercury = MercuryImpl(castor, didcommWrapper, api)

        val mediatorDid = DID(getMediatorDidThroughOob())

        val store = BasicMediatorHandler.PlutoMediatorRepositoryImpl(pluto)
        val handler = BasicMediatorHandler(mediatorDid, mercury, store)
        val seed = initialSeed ?: apollo.createRandomSeed().seed

        val sdk = EdgeAgent(
            apollo = apollo,
            castor = castor,
            pluto = pluto,
            mercury = mercury,
            pollux = pollux,
            seed = seed,
            api = api,
            mediatorHandler = handler,
            agentOptions = AgentOptions(
                experiments = Experiments(
                    liveMode = false
                )
            )
        )

        this.context = SdkContext(sdk)
    }

    private fun startPluto() {
        runBlocking {
            context.sdk.pluto.start(this)
        }
    }

    private fun startSdk() {
        runBlocking {
            context.sdk.start()
        }
        context.sdk.startFetchingMessages(1)
    }

    private fun listenToMessages() {
        CoroutineScope(Dispatchers.Default).launch {
            context.sdk.handleReceivedMessagesEvents().collect { messageList: List<Message> ->
                messageList.forEach { message ->
                    if (receivedMessages.contains(message.id)) {
                        return@forEach
                    }
                    receivedMessages.add(message.id)
                    when (message.piuri) {
                        ProtocolType.DidcommOfferCredential.value -> context.credentialOfferStack.add(message)
                        ProtocolType.DidcommIssueCredential.value -> context.issuedCredentialStack.add(message)
                        ProtocolType.DidcommRequestPresentation.value -> context.proofRequestStack.add(message)
                        ProtocolType.PrismRevocation.value -> context.revocationNotificationStack.add(message)
                        else -> logger.debug("other message: ${message.piuri}")
                    }
                }
            }
        }
    }

    private fun getMediatorDidThroughOob(): String {
        val response = RestAssured.get(Environment.mediatorOobUrl)
        val oob = response.body.asString()
        val encodedData = oob.split("?_oob=")[1]
        val decodedData = String(Base64.getDecoder().decode(encodedData))
        val json = JsonPath.parse(decodedData)
        return json.read("from")
    }
}

data class SdkContext(
    val sdk: EdgeAgent,
    val credentialOfferStack: MutableList<Message> = Collections.synchronizedList(mutableListOf()),
    val proofRequestStack: MutableList<Message> = Collections.synchronizedList(mutableListOf()),
    val issuedCredentialStack: MutableList<Message> = Collections.synchronizedList(mutableListOf()),
    val revocationNotificationStack: MutableList<Message> = Collections.synchronizedList(mutableListOf())
)

class ActorCannotUseWalletSdk(actor: Actor) :
    Throwable("The actor [${actor.name}] does not have the ability to use wallet-sdk")
