package io.iohk.atala.prism.abilities

import com.jayway.jsonpath.JsonPath
import io.iohk.atala.automation.utils.Logger
import io.iohk.atala.prism.configuration.Environment
import io.iohk.atala.prism.walletsdk.apollo.ApolloImpl
import io.iohk.atala.prism.walletsdk.castor.CastorImpl
import io.iohk.atala.prism.walletsdk.domain.models.ApiImpl
import io.iohk.atala.prism.walletsdk.domain.models.DID
import io.iohk.atala.prism.walletsdk.domain.models.Message
import io.iohk.atala.prism.walletsdk.domain.models.httpClient
import io.iohk.atala.prism.walletsdk.mercury.MercuryImpl
import io.iohk.atala.prism.walletsdk.mercury.resolvers.DIDCommWrapper
import io.iohk.atala.prism.walletsdk.pluto.PlutoImpl
import io.iohk.atala.prism.walletsdk.pluto.data.DbConnection
import io.iohk.atala.prism.walletsdk.pollux.PolluxImpl
import io.iohk.atala.prism.walletsdk.prismagent.PrismAgent
import io.iohk.atala.prism.walletsdk.prismagent.mediation.BasicMediatorHandler
import io.iohk.atala.prism.walletsdk.prismagent.protocols.ProtocolType
import io.iohk.atala.prism.workflow.EdgeAgentWorkflow
import io.restassured.RestAssured
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.serenitybdd.screenplay.Ability
import net.serenitybdd.screenplay.Actor
import net.serenitybdd.screenplay.Question
import net.serenitybdd.screenplay.SilentInteraction
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

        val sdk = PrismAgent(
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
                    when (message.piuri) {
                        ProtocolType.DidcommOfferCredential.value -> context.credentialOfferStack.add(message)
                        ProtocolType.DidcommIssueCredential.value -> context.issuedCredentialStack.add(message)
                        ProtocolType.DidcommRequestPresentation.value -> context.proofRequestStack.add(message)
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
    val sdk: PrismAgent,
    val credentialOfferStack: MutableList<Message> = Collections.synchronizedList(mutableListOf()),
    val proofRequestStack: MutableList<Message> = Collections.synchronizedList(mutableListOf()),
    val issuedCredentialStack: MutableList<Message> = Collections.synchronizedList(mutableListOf())
)

class ActorCannotUseWalletSdk(actor: Actor) :
    Throwable("The actor [${actor.name}] does not have the ability to use wallet-sdk")