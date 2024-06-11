package org.hyperledger.identus.walletsdk.abilities

import com.jayway.jsonpath.JsonPath
import io.iohk.atala.automation.utils.Logger
import org.hyperledger.identus.walletsdk.configuration.Environment

import org.hyperledger.identus.walletsdk.workflow.EdgeAgentWorkflow
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
import org.hyperledger.identus.walletsdk.domain.models.ApiImpl
import org.hyperledger.identus.walletsdk.domain.models.DID
import org.hyperledger.identus.walletsdk.domain.models.Message
import org.hyperledger.identus.walletsdk.domain.models.httpClient
import org.hyperledger.identus.walletsdk.edgeagent.EdgeAgent
import org.hyperledger.identus.walletsdk.edgeagent.mediation.BasicMediatorHandler
import org.hyperledger.identus.walletsdk.edgeagent.protocols.ProtocolType
import org.hyperledger.identus.walletsdk.mercury.MercuryImpl
import org.hyperledger.identus.walletsdk.mercury.resolvers.DIDCommWrapper
import org.hyperledger.identus.walletsdk.pluto.PlutoImpl
import org.hyperledger.identus.walletsdk.pluto.data.DbConnection
import org.hyperledger.identus.walletsdk.pollux.PolluxImpl
import java.util.*


class UseWalletSdk : Ability {
    companion object {
        private fun `as`(actor: Actor): UseWalletSdk {
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
                `as`(it).context.revocationStack.size
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
                    }
                }

            }
        }
    }

    private val logger = Logger.get<EdgeAgentWorkflow>()
    private val context: SdkContext
    private val receivedMessages = mutableListOf<String>()

    init {
        val apollo = ApolloImpl()
        val castor = CastorImpl(apollo)
        val pluto = PlutoImpl(DbConnection())
        val pollux = PolluxImpl(castor)
        val didcommWrapper = DIDCommWrapper(castor, pluto, apollo)
        val api = ApiImpl(httpClient())
        val mercury = MercuryImpl(castor, didcommWrapper, api)

        val mediatorDid = DID(getMediatorDidThroughOob())

        val store = BasicMediatorHandler.PlutoMediatorRepositoryImpl(pluto)
        val handler = BasicMediatorHandler(mediatorDid, mercury, store)
        val seed = apollo.createRandomSeed().seed

        val sdk = EdgeAgent(
            apollo,
            castor,
            pluto,
            mercury,
            pollux,
            seed,
            api,
            handler
        )

        this.context = SdkContext(sdk)

        runBlocking {
            pluto.start(this)
            sdk.start()
            sdk.startFetchingMessages(1)
        }

        CoroutineScope(Dispatchers.Default).launch {
            sdk.handleReceivedMessagesEvents().collect { messageList: List<Message> ->
                messageList.forEach { message ->
                    if (receivedMessages.contains(message.id)) {
                        return@forEach
                    }
                    receivedMessages.add(message.id)
                    when (message.piuri) {
                        ProtocolType.DidcommOfferCredential.value -> context.credentialOfferStack.add(message)
                        ProtocolType.DidcommIssueCredential.value -> context.issuedCredentialStack.add(message)
                        ProtocolType.DidcommRequestPresentation.value -> context.proofRequestStack.add(message)
                        //ProtocolType.re
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
    val revocationStack: MutableList<Message> = Collections.synchronizedList(mutableListOf())
)

class ActorCannotUseWalletSdk(actor: Actor) :
    Throwable("The actor [${actor.name}] does not have the ability to use wallet-sdk")
