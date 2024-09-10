import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.jwk.JWK
import eu.europa.ec.eudi.openid4vci.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.hyperledger.identus.walletsdk.apollo.ApolloImpl
import org.hyperledger.identus.walletsdk.apollo.utils.Ed25519PrivateKey
import org.hyperledger.identus.walletsdk.apollo.utils.Secp256k1KeyPair
import org.hyperledger.identus.walletsdk.apollo.utils.Secp256k1PrivateKey
import org.hyperledger.identus.walletsdk.castor.CastorImpl
import org.hyperledger.identus.walletsdk.domain.buildingblocks.Apollo
import org.hyperledger.identus.walletsdk.domain.buildingblocks.Castor
import org.hyperledger.identus.walletsdk.domain.buildingblocks.Pluto
import org.hyperledger.identus.walletsdk.domain.buildingblocks.Pollux
import org.hyperledger.identus.walletsdk.domain.models.*
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.PrivateKey
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.StorableKey
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.StorablePrivateKey
import org.hyperledger.identus.walletsdk.edgeagent.EdgeAgent.State
import org.hyperledger.identus.walletsdk.edgeagent.EdgeAgentError
import org.hyperledger.identus.walletsdk.logger.LogComponent
import org.hyperledger.identus.walletsdk.logger.PrismLogger
import org.hyperledger.identus.walletsdk.logger.PrismLoggerImpl
import org.hyperledger.identus.walletsdk.pollux.PolluxImpl
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import org.hyperledger.identus.apollo.base64.base64UrlEncoded
import org.hyperledger.identus.apollo.utils.KMMECSecp256k1PrivateKey
import org.hyperledger.identus.apollo.utils.decodeHex
import java.net.URI


open class OIDCAgent(
    val apollo: Apollo,
    val castor: Castor,
    val pluto: Pluto,
    val seed: Seed,
    val api: Api,
    val pollux: Pollux = PolluxImpl(apollo, castor),
    private val logger: PrismLogger = PrismLoggerImpl(LogComponent.PRISM_AGENT),

    ) {
    var state: State = State.STOPPED

    companion object {
        fun initialize(
            pluto: Pluto,
            api: Api?,
            apollo: Apollo = ApolloImpl(),
            castor: Castor = CastorImpl(apollo),
            seed: Seed = apollo.createRandomSeed().seed
        ): OIDCAgent {
            return OIDCAgent(
                apollo,
                castor,
                pluto,
                seed,
                api ?: ApiImpl(
                    httpClient {
                        install(ContentNegotiation) {
                            json(
                                Json {
                                    ignoreUnknownKeys = true
                                    prettyPrint = true
                                    isLenient = true
                                }
                            )
                        }
                    }
                ),
            )
        }
    }

    suspend fun start() {
        if (state != State.STOPPED) {
            return
        }
        logger.info(message = "Starting agent")
        state = State.STARTING
        pluto.start()
        state = State.RUNNING
        logger.info(message = "Agent running")
    }

    suspend fun stop() {
        if (state != State.RUNNING) {
            return
        }
        logger.info(message = "Stoping agent")
        state = State.STOPPING
        state = State.STOPPED
        logger.info(message = "Agent not running")
    }

    suspend fun isCredentialRevoked(credential: Credential): Boolean {
        return pollux.isCredentialRevoked(credential)
    }

    fun getAllCredentials(): Flow<List<Credential>> {
        return pluto.getAllCredentials()
            .map { list ->
                list.map {
                    pollux.restoreCredential(it.restorationId, it.credentialData, it.revoked)
                }
            }
    }

    /**
     * This method create a new Prism DID, that can be used to identify the agent and interact with other agents.
     *
     * @param keyPathIndex key path index used to identify the DID.
     * @param alias An alias that can be used to identify the DID.
     * @param services an array of services associated to the DID.
     * @return The new created [DID]
     */
    @JvmOverloads
    suspend fun createNewPrismDID(
        keyPathIndex: Int? = null,
        alias: String? = null,
        services: Array<DIDDocument.Service> = emptyArray()
    ): DID {
        val index = keyPathIndex ?: (pluto.getPrismLastKeyPathIndex().first() + 1)
        val keyPair = Secp256k1KeyPair.generateKeyPair(seed, KeyCurve(Curve.SECP256K1, index))
        val did = castor.createPrismDID(masterPublicKey = keyPair.publicKey, services = services)
        registerPrismDID(did, index, alias, keyPair.privateKey)
        return did
    }

    /**
     * This function receives a Prism DID and its information and stores it into the local database.
     *
     * @param did The DID to be stored
     * @param keyPathIndex The index associated with the PrivateKey
     * @param alias The alias associated with the DID if any
     * @param privateKey The private key used to create the PrismDID
     */
    private fun registerPrismDID(
        did: DID,
        keyPathIndex: Int,
        alias: String? = null,
        privateKey: PrivateKey
    ) {
        pluto.storePrismDIDAndPrivateKeys(
            did = did,
            keyPathIndex = keyPathIndex,
            alias = alias ?: did.alias,
            listOf(privateKey as StorableKey)
        )
    }

    /**
     * This function will use the provided DID to sign a given message.
     *
     * @param did The DID which will be used to sign the message.
     * @param message The message to be signed.
     * @return The signature of the message.
     */
    @Throws(EdgeAgentError.CannotFindDIDPrivateKey::class)
    suspend fun signWith(did: DID, message: ByteArray): Signature {
        val storablePrivateKey: StorablePrivateKey = pluto.getDIDPrivateKeysByDID(did).first().first()
            ?: throw EdgeAgentError.CannotFindDIDPrivateKey(did.toString())
        val privateKey = apollo.restorePrivateKey(storablePrivateKey.restorationIdentifier, storablePrivateKey.data)

        val returnByteArray: ByteArray =
            when (privateKey.getCurve()) {
                Curve.ED25519.value -> {
                    val ed = privateKey as Ed25519PrivateKey
                    ed.sign(message)
                }

                Curve.SECP256K1.value -> {
                    val secp = privateKey as Secp256k1PrivateKey
                    secp.sign(message)
                }

                else -> {
                    throw ApolloError.InvalidKeyCurve(
                        privateKey.getCurve()
                    )
                }
            }

        return Signature(returnByteArray)
    }

    suspend fun parseCredentialOffer(
        offerUri: String
    ): CredentialOffer {
        return CredentialOfferRequestResolver().resolve(offerUri).getOrThrow()
    }

    suspend fun createAuthorizationRequest(
        clientId: String,
        redirectUri: String,
        offer: CredentialOffer
    ) : Pair<Issuer, AuthorizationRequestPrepared> {
        val openId4VCIConfig = OpenId4VCIConfig(
            clientId =clientId,
            authFlowRedirectionURI = URI.create(redirectUri),
            keyGenerationConfig = KeyGenerationConfig.ecOnly(
                com.nimbusds.jose.jwk.Curve.SECP256K1
            ),
            credentialResponseEncryptionPolicy = CredentialResponseEncryptionPolicy.SUPPORTED,
            parUsage = ParUsage.Never,
        )
        val issuer = Issuer.make(
            openId4VCIConfig,
            offer
        ).getOrThrow()

        return Pair(
            issuer,
            issuer.prepareAuthorizationRequest().getOrThrow()
        )
    }

    suspend fun handleTokenRequest(
        authorizationRequest: AuthorizationRequestPrepared,
        issuer: Issuer,
        callbackUrl: String?
    ) {
        if (callbackUrl == null) {
            throw Exception("Callback URL must not be null")
        }
        return with(issuer) {
            runBlocking {
                val url = URI(callbackUrl);
                val paramsMap = url.query.split("&").associate {
                    val (key, value) = it.split("=")
                    key to value
                }
                val code = paramsMap["code"] ?: throw Exception("Invalid code");
                val state = paramsMap["state"] ?: throw Exception("Invalid code");
                val response = authorizationRequest.authorizeWithAuthorizationCode(
                    AuthorizationCode(code),
                    state
                ).getOrThrow()
            }
        }
    }

    private fun popSigner(): PopSigner {
        val privateKeyHex = "d93c6485e30aad4d6522313553e58d235693f7007b822676e5e1e9a667655b69"
        val did = "did:prism:4a2bc09be65136f604d1564e2fced1a1cdbce9deb9b64ee396afc95fc0b01c59:CnsKeRI6CgZhdXRoLTEQBEouCglzZWNwMjU2azESIQOx16yykO2nDcmM-NeQeVipxmuaF38KasIA8gycJCHWJhI7CgdtYXN0ZXIwEAFKLgoJc2VjcDI1NmsxEiECKrfbf1_p7YT5aRJspBLct5zDyL6aicEam1Gycq5xKy0"
        val kid = "$did#auth-1"
        val privateKey = KMMECSecp256k1PrivateKey.secp256k1FromByteArray(privateKeyHex.decodeHex())
        val point = privateKey.getPublicKey().getCurvePoint()
        val jwk = JWK.parse(
            mapOf(
                "kty" to "EC",
                "crv" to "secp256k1",
                "x" to point.x.base64UrlEncoded,
                "y" to point.y.base64UrlEncoded,
                "d" to privateKey.raw.base64UrlEncoded,
            ),
        )
        return PopSigner.jwtPopSigner(
            privateKey = jwk,
            algorithm = JWSAlgorithm.ES256K,
            publicKey = JwtBindingKey.Did(identity = kid),
        )
    }

    fun sendCredentialRequest(
        issuer: Issuer,
        offer: CredentialOffer,
        authorizedRequest: AuthorizedRequest
    ): List<IssuedCredential> {
        //TODO: match the configuration identifier for theat specific offer
        //TODO: We currently don't support having multiple we should check this!!
        val requestPayload = IssuanceRequestPayload.ConfigurationBased(
            offer.credentialConfigurationIdentifiers.first(), null
        )
        val authorizedSubmissionOutcome = with(issuer) {
            when (authorizedRequest) {
                is AuthorizedRequest.NoProofRequired -> throw Exception("Not supported yet")
                is AuthorizedRequest.ProofRequired -> runBlocking {
                    authorizedRequest.requestSingle(
                        requestPayload,
                        //TODO: We must be able to get or create a new set of keys for the signer
                        popSigner(),
                    )
                }
            }.getOrThrow()
        }
        return when(val submissionOutcome = authorizedSubmissionOutcome.second) {
            is SubmissionOutcome.Success -> submissionOutcome.credentials
            else ->  throw Exception("Issuance failed. $submissionOutcome")
        }
    }



}

