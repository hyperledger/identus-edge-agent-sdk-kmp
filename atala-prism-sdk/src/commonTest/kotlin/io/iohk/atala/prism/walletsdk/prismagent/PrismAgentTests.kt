package io.iohk.atala.prism.walletsdk.prismagent

/* ktlint-disable import-ordering */
import anoncreds_wrapper.LinkSecret
import io.iohk.atala.prism.apollo.derivation.MnemonicHelper
import io.iohk.atala.prism.walletsdk.apollo.ApolloImpl
import io.iohk.atala.prism.walletsdk.apollo.utils.Secp256k1KeyPair
import io.iohk.atala.prism.walletsdk.castor.CastorImpl
import io.iohk.atala.prism.walletsdk.domain.models.Api
import io.iohk.atala.prism.walletsdk.domain.models.AttachmentBase64
import io.iohk.atala.prism.walletsdk.domain.models.AttachmentDescriptor
import io.iohk.atala.prism.walletsdk.domain.models.ClaimType
import io.iohk.atala.prism.walletsdk.domain.models.CredentialType
import io.iohk.atala.prism.walletsdk.domain.models.Curve
import io.iohk.atala.prism.walletsdk.domain.models.DID
import io.iohk.atala.prism.walletsdk.domain.models.DIDDocument
import io.iohk.atala.prism.walletsdk.domain.models.KeyCurve
import io.iohk.atala.prism.walletsdk.domain.models.Seed
import io.iohk.atala.prism.walletsdk.domain.models.Signature
import io.iohk.atala.prism.walletsdk.logger.PrismLoggerMock
import io.iohk.atala.prism.walletsdk.mercury.ApiMock
import io.iohk.atala.prism.walletsdk.pollux.PolluxImpl
import io.iohk.atala.prism.walletsdk.pollux.models.CredentialRequestMeta
import io.iohk.atala.prism.walletsdk.pollux.models.LinkSecretBlindingData
import io.iohk.atala.prism.walletsdk.prismagent.protocols.ProtocolType
import io.iohk.atala.prism.walletsdk.prismagent.protocols.issueCredential.CredentialPreview
import io.iohk.atala.prism.walletsdk.prismagent.protocols.issueCredential.IssueCredential
import io.iohk.atala.prism.walletsdk.prismagent.protocols.issueCredential.OfferCredential
import io.iohk.atala.prism.walletsdk.prismagent.protocols.outOfBand.OutOfBandInvitation
import io.iohk.atala.prism.walletsdk.prismagent.protocols.outOfBand.PrismOnboardingInvitation
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/* ktlint-disable import-ordering */

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class PrismAgentTests {

    lateinit var apolloMock: ApolloMock
    lateinit var castorMock: CastorMock
    lateinit var plutoMock: PlutoMock
    lateinit var mercuryMock: MercuryMock
    lateinit var polluxMock: PolluxMock
    lateinit var mediationHandlerMock: MediationHandlerMock
    lateinit var connectionManager: ConnectionManager
    lateinit var json: Json

    @BeforeTest
    fun setup() {
        apolloMock = ApolloMock()
        castorMock = CastorMock()
        plutoMock = PlutoMock()
        mercuryMock = MercuryMock()
        polluxMock = PolluxMock()
        mediationHandlerMock = MediationHandlerMock()
        // Pairing will be removed in the future
        connectionManager = ConnectionManager(mercuryMock, castorMock, plutoMock, mediationHandlerMock, mutableListOf())
        json = Json {
            ignoreUnknownKeys = true
            prettyPrint = true
            isLenient = true
        }
    }

    @Test
    fun testCreateNewPrismDID_shouldCreateNewDID_whenCalled() = runTest {
        val seed = Seed(MnemonicHelper.createRandomSeed())
        val validDID = DID("did", "test", "123")
        castorMock.createPrismDIDReturn = validDID
        val agent = PrismAgent(
            apollo = apolloMock,
            castor = castorMock,
            pluto = plutoMock,
            mercury = mercuryMock,
            pollux = polluxMock,
            connectionManager = connectionManager,
            seed = seed,
            api = null,
            logger = PrismLoggerMock()
        )
        plutoMock.getPrismLastKeyPathIndexReturn = flow { emit(0) }
        val newDID = agent.createNewPrismDID()
        assertEquals(newDID, validDID)
        assertEquals(newDID, plutoMock.storedPrismDID.first())
        assertTrue { plutoMock.wasGetPrismLastKeyPathIndexCalled }
        assertTrue { plutoMock.wasStorePrismDIDAndPrivateKeysCalled }
    }

    @Test
    fun testCreateNewPeerDID_shouldCreateNewDID_whenCalled() = runTest {
        val validDID = DID("did", "test", "123")
        castorMock.createPeerDIDReturn = validDID
        val agent = PrismAgent(
            apolloMock,
            castorMock,
            plutoMock,
            mercuryMock,
            polluxMock,
            connectionManager,
            null,
            null,
            logger = PrismLoggerMock()
        )

        val newDID = agent.createNewPeerDID(services = emptyArray(), updateMediator = false)

        assertEquals(newDID, validDID)
        assertEquals(newDID, plutoMock.storedPeerDID.first())
        assertTrue { plutoMock.wasStorePeerDIDAndPrivateKeysCalled }
    }

    @Test
    fun testCreateNewPeerDID_whenUpdateMediatorFalse_thenShouldUseProvidedServices() = runTest {
        val apollo = ApolloImpl()
        val castor = CastorImpl(apollo = apollo, logger = PrismLoggerMock())
        val agent = PrismAgent(
            apollo,
            castor,
            plutoMock,
            mercuryMock,
            polluxMock,
            connectionManager,
            null,
            null,
            logger = PrismLoggerMock()
        )

        val seAccept = arrayOf("someAccepts")
        val seRoutingKeys = arrayOf("someRoutingKey")
        val service = DIDDocument.Service(
            id = "DIDCommV2",
            type = arrayOf(DIDCOMM_MESSAGING),
            serviceEndpoint = DIDDocument.ServiceEndpoint(
                uri = "localhost:8082",
                accept = seAccept,
                routingKeys = seRoutingKeys
            )
        )
        val newDID = agent.createNewPeerDID(services = arrayOf(service), updateMediator = false)

        val document = castor.resolveDID(newDID.toString())
        val services = document.services
        assertTrue(services.isNotEmpty())
        assertEquals(service.type.first(), services.first().type.first())
        assertEquals(service.serviceEndpoint, services.first().serviceEndpoint)
    }

    @Test
    fun testPrismAgentOnboardingInvitation_shouldAcceptOnboardingInvitation_whenStatusIs200() = runTest {
        val agent = PrismAgent(
            apollo = apolloMock,
            castor = castorMock,
            pluto = plutoMock,
            mercury = mercuryMock,
            pollux = polluxMock,
            connectionManager = connectionManager,
            seed = null,
            api = ApiMock(HttpStatusCode.OK, "{\"success\":\"true\"}"),
            logger = PrismLoggerMock()
        )
        val invitationString = """
            {
                "type":"${ProtocolType.PrismOnboarding.value}",
                "onboardEndpoint":"http://localhost/onboarding",
                "from":"did:prism:b6c0c33d701ac1b9a262a14454d1bbde3d127d697a76950963c5fd930605:Cj8KPRI7CgdtYXN0ZXIwEAFKLgoJc2VmsxEiECSTjyV7sUfCr_ArpN9rvCwR9fRMAhcsr_S7ZRiJk4p5k"
            }
        """
        val invitation = agent.parseInvitation(invitationString)
        assertEquals(PrismOnboardingInvitation::class, invitation::class)
        agent.acceptInvitation(invitation as PrismOnboardingInvitation)
    }

    @Test
    fun testPrismAgentOnboardingInvitation_shouldRejectOnboardingInvitation_whenStatusIsNot200() = runTest {
        val api = ApiMock(HttpStatusCode.BadRequest, "{\"success\":\"true\"}")
        val agent = PrismAgent(
            apollo = apolloMock,
            castor = castorMock,
            pluto = plutoMock,
            mercury = mercuryMock,
            pollux = polluxMock,
            connectionManager = connectionManager,
            seed = null,
            api = api,
            logger = PrismLoggerMock()
        )
        val invitationString = """
            {
                "type":"${ProtocolType.PrismOnboarding.value}",
                "onboardEndpoint":"http://localhost/onboarding",
                "from":"did:prism:b6c0c33d701ac1b9a262a14454d1bbde3d127d697a76950963c5fd930605:Cj8KPRI7CgdtYXN0ZXIwEAFKLgoJc2VmsxEiECSTjyV7sUfCr_ArpN9rvCwR9fRMAhcsr_S7ZRiJk4p5k"
            }
        """
        val invitation = agent.parseInvitation(invitationString)
        assertFailsWith<PrismAgentError.FailedToOnboardError> {
            agent.acceptInvitation(invitation as PrismOnboardingInvitation)
        }
    }

    @Test
    fun testPrismAgentOnboardingInvitation_shouldRejectOnboardingInvitation_whenBodyIsWrong() = runTest {
        val agent = PrismAgent(
            apollo = apolloMock,
            castor = castorMock,
            pluto = plutoMock,
            mercury = mercuryMock,
            pollux = polluxMock,
            connectionManager = connectionManager,
            seed = null,
            api = ApiMock(HttpStatusCode.OK, "{\"success\":\"true\"}"),
            logger = PrismLoggerMock()
        )
        val invitationString = """
            {
                "type":"${ProtocolType.PrismOnboarding.value}",
                "errorField":"http://localhost/onboarding",
                "from":"did:prism:b6c0c33d701ac1b9a262a14454d1bbde3d127d697a76950963c5fd930605:Cj8KPRI7CgdtYXN0ZXIwEAFKLgoJc2VmsxEiECSTjyV7sUfCr_ArpN9rvCwR9fRMAhcsr_S7ZRiJk4p5k"
            }
        """
        assertFailsWith<io.iohk.atala.prism.walletsdk.domain.models.UnknownError.SomethingWentWrongError> {
            agent.parseInvitation(invitationString)
        }
    }

    @Test
    fun testPrismAgentSignWith_whenNoPrivateKeyAvailable_thenThrowCannotFindDIDPrivateKey() = runTest {
        val agent = PrismAgent(
            apollo = apolloMock,
            castor = castorMock,
            pluto = plutoMock,
            mercury = mercuryMock,
            pollux = polluxMock,
            connectionManager = connectionManager,
            seed = null,
            api = null,
            logger = PrismLoggerMock()
        )

        plutoMock.getDIDPrivateKeysReturn = flow { emit(listOf(null)) }

        val did = DID("did", "peer", "asdf1234asdf1234")
        val messageString = "This is a message"
        assertFalse { plutoMock.wasGetDIDPrivateKeysByDIDCalled }
        assertFailsWith(PrismAgentError.CannotFindDIDPrivateKey::class, null) {
            agent.signWith(did, messageString.toByteArray())
        }
    }

    @Test
    fun testPrismAgentSignWith_whenPrivateKeyAvailable_thenSignatureReturned() = runTest {
        val agent = PrismAgent(
            apollo = apolloMock,
            castor = castorMock,
            pluto = plutoMock,
            mercury = mercuryMock,
            pollux = polluxMock,
            connectionManager = connectionManager,
            seed = null,
            api = null,
            logger = PrismLoggerMock()
        )

        val privateKeys = listOf(
            Secp256k1KeyPair.generateKeyPair(
                seed = Seed(MnemonicHelper.createRandomSeed()),
                curve = KeyCurve(Curve.SECP256K1)
            ).privateKey
        )
        plutoMock.getDIDPrivateKeysReturn = flow { emit(privateKeys) }

        val did = DID("did", "peer", "asdf1234asdf1234")
        val messageString = "This is a message"

        assertEquals(Signature::class, agent.signWith(did, messageString.toByteArray())::class)
        assertTrue { plutoMock.wasGetDIDPrivateKeysByDIDCalled }
    }

    @Test
    fun testParseInvitation_whenOutOfBand_thenReturnsOutOfBandInvitationObject() = runTest {
        val agent = PrismAgent(
            apollo = apolloMock,
            castor = castorMock,
            pluto = plutoMock,
            mercury = mercuryMock,
            pollux = polluxMock,
            connectionManager = connectionManager,
            seed = null,
            api = null,
            logger = PrismLoggerMock()
        )

        val invitationString = """
            {
              "type": "https://didcomm.org/out-of-band/2.0/invitation",
              "id": "1234-1234-1234-1234",
              "from": "did:peer:asdf42sf",
              "body": {
                "goal_code": "issue-vc",
                "goal": "To issue a Faber College Graduate credential",
                "accept": [
                  "didcomm/v2",
                  "didcomm/aip2;env=rfc587"
                ]
              }
            }
        """

        val invitation = agent.parseInvitation(invitationString.trim())
        assert(invitation is OutOfBandInvitation)
        val oobInvitation: OutOfBandInvitation = invitation as OutOfBandInvitation
        assertEquals("https://didcomm.org/out-of-band/2.0/invitation", oobInvitation.type.value)
        assertEquals(DID("did:peer:asdf42sf").toString(), oobInvitation.from)
        assertEquals(
            OutOfBandInvitation.Body(
                "issue-vc",
                "To issue a Faber College Graduate credential",
                listOf("didcomm/v2", "didcomm/aip2;env=rfc587")
            ),
            oobInvitation.body
        )
    }

    @Test
    fun testParseInvitation_whenOutOfBandWrongBody_thenThrowsUnknownInvitationTypeError() = runTest {
        val agent = PrismAgent(
            apollo = apolloMock,
            castor = castorMock,
            pluto = plutoMock,
            mercury = mercuryMock,
            pollux = polluxMock,
            connectionManager = connectionManager,
            seed = null,
            api = null,
            logger = PrismLoggerMock()
        )

        val invitationString = """
            {
              "type": "https://didcomm.org/out-of-band/2.0/invitation",
              "id": "1234-1234-1234-1234",
              "from": "did:peer:asdf42sf",
              "wrongBody": {}
            }
        """

        assertFailsWith<SerializationException> {
            agent.parseInvitation(invitationString.trim())
        }
    }

    @Test
    fun testStartPrismAgent_whenCalled_thenStatusIsRunning() = runTest {
        val getLinkSecretReturn = flow<String> { "linkSecret" }
        plutoMock.getLinkSecretReturn = getLinkSecretReturn
        val agent = PrismAgent(
            apollo = apolloMock,
            castor = castorMock,
            pluto = plutoMock,
            mercury = mercuryMock,
            pollux = polluxMock,
            connectionManager = connectionManager,
            seed = null,
            api = null,
            logger = PrismLoggerMock()
        )
        assertEquals(PrismAgent.State.STOPPED, agent.state)
        agent.start()
        assertEquals(PrismAgent.State.RUNNING, agent.state)
    }

    @Test
    fun testStopPrismAgent_whenCalled_thenStatusIsStopped() = runTest {
        val agent = PrismAgent(
            apollo = apolloMock,
            castor = castorMock,
            pluto = plutoMock,
            mercury = mercuryMock,
            pollux = polluxMock,
            connectionManager = connectionManager,
            seed = null,
            api = null,
            logger = PrismLoggerMock()
        )
        agent.stop()
        assertEquals(PrismAgent.State.STOPPED, agent.state)
    }

    @Test
    fun test_OOPInvitationInURLFormat() = runTest {
        val oob =
            "https://my.domain.com/path?_oob=eyJpZCI6ImQzNjM3NzlhLWYyMmItNGFiNC1hYjY0LTkxZjkxNjgzNzYwNyIsInR5cGUiOiJodHRwczovL2RpZGNvbW0ub3JnL291dC1vZi1iYW5kLzIuMC9pbnZpdGF0aW9uIiwiZnJvbSI6ImRpZDpwZWVyOjIuRXo2TFNjcGZReGJ2VEhLaGpvbzVvMzlmc254VEp1RTRobVp3ckROUE5BVzI0dmFORi5WejZNa3UzSkpVTDNkaHpYQXB0RWpuUDFpNkF0TDlTNGlwRTNYOHM3MWV4MW9WVGNHLlNleUowSWpvaVpHMGlMQ0p6SWpvaWFIUjBjSE02THk5ck9ITXRaR1YyTG1GMFlXeGhjSEpwYzIwdWFXOHZjSEpwYzIwdFlXZGxiblF2Wkdsa1kyOXRiU0lzSW5JaU9sdGRMQ0poSWpwYkltUnBaR052YlcwdmRqSWlYWDAiLCJib2R5Ijp7ImdvYWxfY29kZSI6ImlvLmF0YWxhcHJpc20uY29ubmVjdCIsImdvYWwiOiJFc3RhYmxpc2ggYSB0cnVzdCBjb25uZWN0aW9uIGJldHdlZW4gdHdvIHBlZXJzIHVzaW5nIHRoZSBwcm90b2NvbCAnaHR0cHM6Ly9hdGFsYXByaXNtLmlvL21lcmN1cnkvY29ubmVjdGlvbnMvMS4wL3JlcXVlc3QnIiwiYWNjZXB0IjpbXX19"
        val agent = PrismAgent(
            apollo = apolloMock,
            castor = castorMock,
            pluto = plutoMock,
            mercury = mercuryMock,
            pollux = polluxMock,
            connectionManager = connectionManager,
            seed = null,
            api = null,
            logger = PrismLoggerMock()
        )
        val x = agent.parseInvitation(oob)
        assert(x is OutOfBandInvitation)
        assert((x as OutOfBandInvitation).type == ProtocolType.Didcomminvitation)
    }

    @AndroidIgnore
    @Test
    fun testAnoncreds_whenOfferReceived_thenProcessed() = runTest {
        val fromDID = DID("did:prism:asdf42sf")
        val toDID = DID("did:prism:asdf42sf")

        val apiMock: Api = ApiMock(
            HttpStatusCode(200, "Ok"),
            getCredentialDefinitionResponse
        )
        val pollux = PolluxImpl(castorMock, apiMock)
        plutoMock.getLinkSecretReturn = flow { emit(LinkSecret().getValue()) }

        val agent = PrismAgent(
            apollo = apolloMock,
            castor = castorMock,
            pluto = plutoMock,
            mercury = mercuryMock,
            pollux = pollux,
            connectionManager = connectionManager,
            seed = null,
            api = ApiMock(HttpStatusCode.OK, "{\"success\":\"true\"}"),
            logger = PrismLoggerMock()
        )

        val attachmentDescriptor =
            AttachmentDescriptor(
                mediaType = "application/json",
                format = CredentialType.ANONCREDS_OFFER.type,
                data = AttachmentBase64(
                    "eyJzY2hlbWFfaWQiOiJodHRwOi8vaG9zdC5kb2NrZXIuaW50ZXJuYWw6ODAwMC9wcmlzbS1hZ2VudC9zY2hlbWEtcmVnaXN0cnkvc2NoZW1hcy81ZTBkNWE5My00YmZkLTMxMTEtYTk1Ni01ZDViYzgyZjc2Y2MiLCJjcmVkX2RlZl9pZCI6Imh0dHA6Ly8xOTIuMTY4LjY4LjEwMjo4MDAwL3ByaXNtLWFnZW50L2NyZWRlbnRpYWwtZGVmaW5pdGlvbi1yZWdpc3RyeS9kZWZpbml0aW9ucy81ZTI5NWNmMi00MjI2LTNjN2QtYjI3Zi04N2JlMTYwNjQ3NmQvZGVmaW5pdGlvbiIsImtleV9jb3JyZWN0bmVzc19wcm9vZiI6eyJjIjoiMTAyNTI4NDk1NTgzODg5MzkwMDcxMzk2OTI1ODg2NzI1MDk2MDMwNjY4ODgwOTMzNzQ5NTUzODEyNTY3MzE0NjQ0ODE0ODU2Nzk2NjI4IiwieHpfY2FwIjoiMTA4NzM1NDQ3NTAzNTQwNDc2MDIyOTAzNTYxNDY1Nzk4NzExMDA5MTU3MDcwOTY1MjMzNzIyNTAzMzg1NDY2NjIyNjYxNDgyMDkzMjAyMjg2ODcxMDQ5MDM3MDA5NzA1ODg3OTUzMDY5NjczNjA4Mzg5MTQ4NzY5ODI1NjMyNTU3NTk0Mzg3NDI2MjkzODY5OTIxNTg4MTM0MzI1OTczODg5NDY1NjYzNzM4ODA4ODg5NTE3Njc4MzIwOTYxNTk0NDg5MjYxNzI3ODAxMTc1OTMxMTQ2MjcwNDk3MzE5NTc4MTc4NDg3Nzg3MTAxNzg1ODMwMjE2NjgzODk2ODE1ODUyNjgzNDc5NjUxMzQ5NDY3MzUzNzM4MjM0NjE4NTMyODIyMzU1MTQ3ODcxOTU5NTU5ODgzNTUxOTQ5OTkwODY0ODEzNTYyODU3NzU5NDU4MTg1MDI5MDI3NTI1NTMzMDQyMDQ5MjEwMDA4NTg3MTc2MzQwNTg4Nzc2Mjc0MzgxMDU2MzM5ODE0OTAzMTMyNjI2MzIyMzc3MjAwMjYwMjM4NDcwNjcyMzI4NDQ4NzIyOTk4NDc1MDIwMzA4MjY2NDIzODUyNzA3Njk2OTQyNjk3NDc3MzI0MDUzOTM0MjQ3NDU4MDcxNjkyMDM5OTQ4MDkxMDg5MjA0NDQ2MDkyODY0Njc1NTQyMDA0Nzk1MDUyNjMzNzczMTQyMDc4Mzg0MTA1MDQzMDY4NjQ3Nzg3NzY3MDE1MDc5MjE1OTEzNTIzODIxOTU4NDQ2NTAxODY2ODY1OTE1NzEwNjY1NDU1MzU4ODEwNTY0OTk3MDUwNjAzNDc4NDE0MzA4NDQzMzc3Njg3ODUyMzc4NDgwNzg3MTQ1MDY2MTQ5MzA5MTM0NzMyNDg4NzQzMTg1NDgxMzU5MzA1NTM3MiIsInhyX2NhcCI6W1siZGF0ZW9maXNzdWFuY2UiLCIxODg0NTYwNDI1ODExNzgxNjE2Njc5NjAyMzA5ODExOTI5NzEyNDA4MDA0MzkxMDEyNzk3ODAxODA0MzU0MzQ0MTI3NDI2MTk0ODg5NDI3MjU3NjQzMzg2MTkxNTY0NDM5ODUzOTQ2NTg4MzkyNjMwNzEwMzA1OTM2OTAxNDA3NjYwOTk2NDc5ODkxNTgzNDE2NDkxODYyNzA3MTczOTA2OTg4ODEzNDE5ODQyMDg1MDM1MTg1OTA0MzQ2MTU4OTM2NDIyOTY5OTMwODg4Njg3NTc0MDcyMzY5MDc2NjkxNTk4MjkyMDkwNDA3Mjg5ODM2NDc2ODQyNTc2MjI3MzAzNDAwOTI4MjkwMDM0NTYwNjk5NTg1NTMzMzkxNDg0OTMwMDQ3NTEwMzMxNDQxNjAyMzEwNjM3NDg2MjY2OTIyNTIyNTIxMzQ2ODM5ODkxMDg1ODM3NTczMzAzMjkyNzMyNTA5Mzc0MTI0ODgzMTA0MjEzNzYzMDA3NzMwOTY4Mzg4MDc0NzE3ODc5NDM5NjE5Njg1NzY4MjU1OTg4MjY0NjMxMTk1MTU3MjE3NjExMTE0NzE3Njc4NjUzMTc0NDc2MTc4MjA4NTIwNzk2MDkwNzI2MzE1MjIzMDU4MTk4NjM3NjU4NDU2NTA3NDcyNzA4MDI0OTgzMjUzNjQyMDAwNDI0MjQyNTUyMDcwNzUzMTg3MDI3MTE0MTI1NzQ0MzExNzMxOTM1NjgwNDE1MTk5NzI4OTE2NjgxNTY2MzY2NjkxNDQyMzE4OTE3NDAxMzA0MzMzNzI5ODU0MzQ2MTAwNDI2NDE1MzEwMzYwNjQ4NDg3NDYzNzgxNjI5NDQ2ODU3ODg2NjgzNTIzODk2ODcyMTExNDg3MjUwNjgwMDQzOTIxMzgyMDM1MDkxMTg4NzE1MDAwNTIzIl0sWyJlbWFpbGFkZHJlc3MiLCIxNTUwMTI2ODAwMjA1MzI5Mzg3NzcyNjA4OTY3NDk0NDIyOTc0MjU1MTI1NDU0NTEyOTc3NjExNTgyNDE2Nzc1MDg2OTA1NjIzNDUzMTIxNDg0NzM0MzkyNDQ4NzEyNTQyNTYwODUyMjUzNzcyODI2NTYyNjg5NzY1NjYwMzM3OTUyNTEyODQ4MDA1Mzk1NTQxMzcwMTI1Mzk4NDg4OTIxNDM4NDUwNzMxMzYwNTcwMzQ2MjMyNDEyODgzMzQ5NjU4MjMxOTQ5OTgwMTUwMTI2NTQ3Nzg0NTM2ODE2NzM0Njc2Mzg1NDA2OTM1MDQ4NDQ0NDM1ODY2MzYwNTk2MjE3NTM4Njg1NTg5MDE3NDY1NTg2MzI1MDQxNzc1OTcwMzQ1NjY1MjM3MzcyMDA3MjAxNTczNDAwODk3NDA3MDA0OTY1Nzk1MDE3MDQ2NDI5MzM5MzMwMTg2NjAwNzE1MjE3Mjg3ODk3Mjg1MDk1MTEwMzExMDYxODU1OTE1NjQ3Nzk3MTkyNTc4NDI1MDgzMjAwMTIwNDEyNTU4NDM2MjQyNTc0ODIzNjE4NTAxODY5NTQxNzAwMTU5NjMyNDkwNDgxMzY3ODU2NjA5MTAwNjA4MzI0MDcyODk3MjA3NDQxNzgwNDc1Njg4MTk4ODI3MDE1MjE2MDA5OTA4NjY0NTYwODk5MDMzODYxNjk1NTg2OTE1NzI4NjYwMDkzMjM3Njk4NzUzODMwOTAxODQ2MDE1OTU0NzEzNTc5MTc5NDgyMDMyNjIxMTEyNjA3NDcyOTM5NzYyNjM4OTgzMDkyNDE0NTgzMzMyODI2MjUzMTE3NDMwNTAwOTI2NTcwODcxNjI2MzYyMTQxNTM0NzUwMDQwMTkxNzEzMzE4OTQ1NTIwNDUzOTI5Mjk5Nzk3ODc2NDEyNzI3MzgiXSxbImZhbWlseW5hbWUiLCIyMDM1NjQzMjQ2ODM3MTA2MDc3MzEzNTkwOTY1Nzc2ODc1ODkwMjM0MTI0MDQ2MjcyOTc0OTQ0MDMxOTIxNzQ4NjQ1ODA0NDc2MjExODc4NTA1MTEzODE5NzYzMjM3MjczMTM4MjgzNDE1MTE2ODYyMzMwNTg0Mjc4NzcxNjg5NjMyNTIyNzE4NjYzMzkwOTg2MjI0NTA3NDg3OTc3MDk2MDMxMTAzMDY1MDQzNjI5Nzk3NjAzMjM2MTc1ODczOTA0MzUwMjMyNjU1Mjc2NzEzNjIxOTUwNzExNjExMzMzODM5OTE0OTU2MzEyNzM4NDkyNjI5NjQ3Nzk2NTk4NDUzOTM2MTY1NjIwNDI1MTc4OTU0NzE2MjE1OTUzNzQyNjM4NDk0NzA3NzMxNjM2NTc0OTI0Njc4MTk5NDQzMjY1MDU4NDM4MTc3NjE0NTM3NjE5NTI0NzY0MDkxNTY1NzUxODg0OTkzMTU4ODc3NjIxMjMxNzc5MDkzOTIyNzQ0NzM4Nzk5OTEwOTg0OTczOTMyMzYwNzM0OTA3NTQ1NTAxOTE2OTUyOTU3MTEwNTczMjQzODA5MDA1NzE5MjU5MTg1MzgwODI4NTU2MzgwMDA1NzU4MTkzNjIwMzc5MzA5NTk1NTgyMTAxMDEwOTg2MDkyNTE3NjM4Njc3NDYwMTQ5MzQ5ODc5NjYyMzc0MTQ0NzUwMDkyODAzNTE1OTUwMzc0MzkzMTAzMTY5MDYzNTE4ODczNTMzMjAxMjAxMjg4NzU3NjczNzk2OTAzOTAzMTEyNzYyMDgxNjE0MzY1NjgwNjEyNzQ1OTEyMTQ4Mjg0NTQ2MzgxOTA4NDAzNzExMDYwNzc0NDQzNDQyMjc3NTU5MjcyMjkzMDg2MzEzMzg5MDQ4ODA0MzA5OTg0NjgyMDA4MDgxMTYiXSxbImRyaXZpbmdsaWNlbnNlaWQiLCIyMzg0Nzk3NTUyNDQ2NTQ0Nzk3NjQyNTc0NjMyMDU3MjgwODIyODkwODgxNjY4NDQyMTczNDY5OTQ4MDYwOTU0ODU2MjY4MDU2MzQ4MTEyMjQ3MzA4NzYwMDQwNTAxODY4NzE3NDQ4MTI1MDI0NDc2MTcwOTU5Njg0Njc1NzY0MjAwMjc5MTcyMzAwOTgzMTQ1MjgzMDAwMzQzNjIxOTEzNTYwNjA5MzQzNTQyNDM2ODQzNDI0NzkwMzUxNjgwMjg3MDAwNzc4ODM5ODQwMDI0NjA4NDg4NTA5MDQ3MjU3MDI0ODU1Njc5ODk3ODc3NTk5NjU2MTc2NDAwMzk1MTQ0MzY5NjkyNzU4MzIxOTc0MzQyODk2MzY4NzEwMTA3ODIyMTQ4NTM2MzIwMDI0NjYwMzUzNTk1MzA5MDgxMTIwNDQyMTgzNDQ3NzE5NzA5Nzc4MjQxNTI2NDQ4MjU5NTY1NTYyNDUxODYyMDk2OTQzMTUwMzk4ODQ1OTM0ODA3OTM4NjE0MTE4Mzk2MjA5NTc5NDM0OTU2MDk2NzY3OTAzMTE4MDc4MTk1NjE1NzI4MDgwNjA3NzMyOTczMTg2NzU0MTA3Mjc2MTM4OTk1NDQzMzQxNzAxMDQ5OTY3NTc0ODA1MjM2MjEyNTIyNDc0MDI5NTcyOTc0OTk4MjA2NDExMDM5MzQxODY4MzIwMTgwMDU5MzQ4ODE4MDQ5NTgyNjU4ODg0OTI2Nzk2OTAzMzc1OTY4NjMwNjA0MDgwOTE2MTc1ODIzMDIyNjc3ODc5MzE4MDgyODUwNzgwMzQyNDU0ODI4OTEzMTA4Nzk1MDg4NjA1Mzk1NjUxMzM0NTMyNDU3NzUxNjQxNTUwOTE5NDQ4NDAzODM1OTI0NDg5NzYzMjE4NDUyNzUzMzY0NjAzMzE3NzgxMDA0Il0sWyJkcml2aW5nY2xhc3MiLCI3MTMwODIxMTg0NjAwNjAwNjE1Nzk1NDU5NjkyNDYwMzE1NzM4ODA5NTU0MjA0NTQ4OTI1NjA2MzkyNzQ2MjE3MDQyMDI1OTExNjE2NTM4ODQ0ODgyMDgwNjM5MTQ0MjAwMDA2NzIwMzY4MjUzNzMzMzM0ODk1NTQ0NjU5ODU1ODk3MzQzNjc1MzMxNjUwMDI2NjgwNzc1MjgxMzgxMzY0MzcyNTE3ODgzMDI5NTE1MDM2MjQ1ODIyOTUwNzU3MDYwNTk2Nzc5MDE0ODUzODIzMTczNTU0NjczMTk0NjAxODU0MTk2NDQ1NDI3MzIxNTk1Mjk4NTg0OTI5MDM3NTMyMzk3NDMxODE1MDk0Mzk3MjQyODUwMTQ3NjY3NTc0NDM1OTY5ODMzMzE2NDc1MTY0MDM4NjIxMzk1Mzk0ODAxOTg3MzIyOTc0NDY0MjYxNTQyODMxNzI0OTQwNDQ5NjQyMjI5OTA3MzIwNzY2Njg0Nzk0NTgwMDExNzc4NzM0NzI5OTk0MjkxMjAxMDA3NTY5NTE5ODg0ODU3MzU2OTY5NzIwMDUwMTk5OTE3MDk3MjIzOTU3Mjc0MzI2MjkxNjc2Mjc1OTIzOTI1NTQ2MTMxODIxNDE2NDc3MDE3OTk2NzE1OTU3MzAxOTM4MzMyNzU0MzE5OTA0ODkxNDExNDU0MjkzNjMwMDU1NTg4MTUwNjA3ODQ4MTM1MDU1Njk5OTM2OTAxNzAzNTAwNjM2MzgxNDQ5ODAzMTM2NzAyODQwNDcyMjY1MDAzNTczNzk4MDM1NzYyNjIzMzAwOTQzNTQ3Mjk1NDA4OTkxODgyNDMxODEwNzIwNzE3NDE3NjIxNjYxNzUyNzEzNzM2ODQ2MDY4NzExNDk4OTkxMjc2NDcyMDgxNDAzMjEyMzUwNDEzNDA0MjQyNDQiXSxbIm1hc3Rlcl9zZWNyZXQiLCIxNDQ0ODk0NjYyMjg1MjAwNjE4ODEwMjI2MTUxMTk3NjIwMjc0OTgwNTk0NzkzMzU3ODY0MDg1NDA0MzcxNzY2MTA0NzI5OTg0ODI3MjgyNzA4NTkzODUyMDU1NDUwNTUzOTc3MzA3MDQwMTY5NDA1ODgwMTEwMzMxMDU3MDMxNjc4OTM2NDQ2MDQ3NDE4NzE0NDc5OTA3Mjg5MTExMTkxMzA5MDQ2MTMzMzAxMTIzNTk4MTAyMzg0NjQ5ODA3NTQwOTY4NjQ5NzU2MjY4NDAwNDc2NDk5ODY2NDg1MjMxNjkzMDEyMjUxNzA5ODE3OTE3MTMyNjUxNjExNDM0NzkzMTI4NDM5MjQ1NjY3ODE1NjQ2NDUxOTg1ODAyNTg3NDYwMDk4ODk2MDc3OTA3ODUwNTE4NzI4MzMxNzAyMTQ2OTk0NzQzMDUxMDM5ODkwNjA3MzE5NzYyNjkyNTY4NjcwOTY5NDA1OTU2NDU5ODMxMTU5Njg2MTk1OTM3ODM0OTUyNzM2NjY4MDU5NDUxNDM5MTQ3NDI0Njg0ODY2OTg1ODE0Nzg5MzY5NTE0NDczOTExNTA1NjM1OTUwMjg1OTU0NzI3ODE4OTk2MjI4NDI4NTU5NjY3MjM5MDcxNTkwMTI2NzYxOTQ4MDQ5NTE2OTY1MTg4OTE1MTkzMTYxMjk4MDU4NDAwMjk3MDQ2MzQ0NTg2ODk5OTQ2MTA0MDM0ODc2MjkxMjU1NDMxNzYyMTcyMTc3MTEwODk5OTgxOTU1MjM0MDE0NzM2ODgzNTczMjQ1OTA3MTUxNzAzMjM1MDY2MjU0NDU2NzU5NDUxNDIzODcxNTA5NzgwMzA5NDAxODYzNDc4OTgxMjU0MDMwNjE2NDYxNTg4NjU0NjgxMDg5OTk4OTE4NjA2NTc4MjM0ODc5OTg1NjcxIl1dfSwibm9uY2UiOiIxMTE2NzA0MDg4NDcyOTA3NTIzNjkwNDkwIn0="
                )
            )

        val offerCredential = OfferCredential(
            body = OfferCredential.Body(
                credentialPreview = CredentialPreview(
                    attributes = arrayOf(
                        CredentialPreview.Attribute(name = "Name", value = "Value", mediaType = "application/json")
                    )
                )
            ),
            attachments = arrayOf(attachmentDescriptor),
            thid = "1",
            from = fromDID,
            to = toDID
        )
        val requestCredential = agent.prepareRequestCredentialWithIssuer(did = toDID, offer = offerCredential)
        assertEquals(offerCredential.from, requestCredential.to)
        assertEquals(offerCredential.to, requestCredential.from)
        assertTrue(requestCredential.attachments.size == 1)
        assertEquals(requestCredential.attachments[0].format, CredentialType.ANONCREDS_REQUEST.type)
        assertEquals(offerCredential.thid, requestCredential.thid)
    }

    @AndroidIgnore
    @Test
    fun testAnoncreds_whenCredentialIssued_thenProcessed() = runTest {
        val fromDID = DID("did:prism:asdf42sf")
        val toDID = DID("did:prism:asdf42sf")

        val apiMock: Api = ApiMock(
            HttpStatusCode(200, "Ok"),
            getCredentialDefinitionResponse
        )
        val pollux = PolluxImpl(castorMock, apiMock)
        plutoMock.getLinkSecretReturn = flow { emit(LinkSecret().getValue()) }
        val meta = CredentialRequestMeta(
            linkSecretBlindingData = LinkSecretBlindingData(
                vPrime = "25640768589781180388780947458530942508097609060195936083325202836425537796105863532996457182896416190370043209557677698887790935151362153536943154068082466343529339252470449056527102073900035205398743827912718037139005903291819127500631482122295491777147526837712271367909449810555177615439256541701422814752128559601153332207720895418174855363389532697304935246097129194680107532713993463598420823365761867328806906368762890406604820633668919158697683127114469035627228895027952792675790305070772499052052690434104276748788760647551842035459213572765697025729553350526825112536685989553872204362324245819081933885546131268965572563884162204",
                vrPrime = null
            ),
            linkSecretName = "1",
            nonce = "519571990522308752875135"
        )
        plutoMock.getCredentialMetadataReturn = flow { emit(meta) }

        val agent = PrismAgent(
            apollo = apolloMock,
            castor = castorMock,
            pluto = plutoMock,
            mercury = mercuryMock,
            pollux = pollux,
            connectionManager = connectionManager,
            seed = null,
            api = ApiMock(HttpStatusCode.OK, "{\"success\":\"true\"}"),
            logger = PrismLoggerMock()
        )

        val attachmentDescriptor =
            AttachmentDescriptor(
                mediaType = "application/json",
                format = CredentialType.ANONCREDS_ISSUE.type,
                data = AttachmentBase64(
                    "eyJzY2hlbWFfaWQiOiJodHRwOi8vaG9zdC5kb2NrZXIuaW50ZXJuYWw6ODAwMC9wcmlzbS1hZ2VudC9zY2hlbWEtcmVnaXN0cnkvc2NoZW1hcy81ZTBkNWE5My00YmZkLTMxMTEtYTk1Ni01ZDViYzgyZjc2Y2MiLCJjcmVkX2RlZl9pZCI6Imh0dHA6Ly8xOTIuMTY4LjY4LjEwMjo4MDAwL3ByaXNtLWFnZW50L2NyZWRlbnRpYWwtZGVmaW5pdGlvbi1yZWdpc3RyeS9kZWZpbml0aW9ucy81ZTI5NWNmMi00MjI2LTNjN2QtYjI3Zi04N2JlMTYwNjQ3NmQvZGVmaW5pdGlvbiIsInJldl9yZWdfaWQiOm51bGwsInZhbHVlcyI6eyJkcml2aW5nTGljZW5zZUlEIjp7InJhdyI6IjEyMzQ1IiwiZW5jb2RlZCI6IjEyMzQ1In0sImVtYWlsQWRkcmVzcyI6eyJyYXciOiJhbGljZUB3b25kZXJsYW5kLmNvbSIsImVuY29kZWQiOiI3NTEwNzA0NjM0MDE2NTY3NzAxMTk0MjA3NTY0NDAwOTA2NjU0MTY2MTE0ODgyMjU4OTAzMzY3OTg2MTIwOTE4NjQ5Mjc2ODY2OTkyNCJ9LCJkYXRlT2ZJc3N1YW5jZSI6eyJyYXciOiIyMDIwLTExLTEzVDIwOjIwOjM5KzAwOjAwIiwiZW5jb2RlZCI6IjUzODY4NTU5NTMxNzQ4NDY3MDk3NTIwODU5MDUzMDgxODM1NzQ3NDM1NjExNjA2ODQyMDQxMTQ3NTgyMDA0ODc0MzQ4MDQ2MTY0Njg1In0sImZhbWlseU5hbWUiOnsicmF3IjoiV29uZGVybGFuZCIsImVuY29kZWQiOiIxNjc5MDg0OTMxMjM3NDc5NDczNjgxMzM3NzU2NzI1Mzg1MTM3MzYwNzk3MDQ3MzM3NzcwMTQ3NzI2OTE5NDAxOTU1NzY1NDU2MjAzNSJ9LCJkcml2aW5nQ2xhc3MiOnsicmF3IjoiMyIsImVuY29kZWQiOiIzIn19LCJzaWduYXR1cmUiOnsicF9jcmVkZW50aWFsIjp7Im1fMiI6IjExMTI0OTQwODk5NTQ2MzQ0NTY2MjA3NjExNTQyODEwMzAyNDE1ODY5Njg4MDE3NTMyNDYwMzM3MDY0NTk2Mjk1NjIxNzkwMDk2MTUxNSIsImEiOiIxNjk5NjYwOTQ5MDE3NjQ2MzAzOTIxMTc4MjgyNDkxODM5MjE4MzUwNjk5MjM4Nzk2NDQxOTg4OTI0NzA1MzI0NjMzNTQ0MzM0Mzc1ODQwNjgyODY3Nzc3MDQ5NDY2ODM3NjcyMjQxMzgxMjgwMjE0OTIxMDI1NDcyNTAwNDAwMjYyMjA1NTE1MzQxOTQxOTEyMjc2ODIxMTcwNzYwNDAwNzkzMzA3NDY3MDQzOTAzMTAzMDI3MTI2NjU4MDM3MDg1MTExMDU1NDczOTI2MTc2ODU2ODY1MTg3Njc3OTAzMTQzODkzNzg2NTU0NjA2MDQ1MDYyMDQxMzAxMzM3MDQ0NTQzMTk2NTM0NzMwNjczODc4Njg3MjUxMjc3NzEyMzEwODUzNDU4MTYxMjg1NzE1MDIxNTk4OTkxODgxNjE5OTI0NTU5NzY0NTU3NDQzMjUxMDcwNzg5NDAzMTQ2MzMxODE4Njk4NTU1MzIzMDU0NTUzNDI0NzkyNjk5OTE2MzIzODg2NzA0MjE0NDY0ODcwMTI5NTkwODcwODIxOTc1MDkwNjQ4MTkzNjExNjY5MjMxMTk2NzY2Nzg5ODU5NDQ3MTMwOTMwNDIxMjI0NTE0MTI1ODYyMzI5OTU5NTIyNjkxMTE0Mzc3MzA4MzYyNjY2OTY2NzQ3NTQxNjU2NzkzOTc4ODc4Mzc0NTQ4MzcwODgyMDMzMTk2MDQxMjE5NDQ1MjAxOTIxNDYzNzEwMjM4OTUyMjkzNDkyODg0OTU5MDgxMDU3MDc2NzAwOTI1OTI2NTYyODg4MTcwODk5NzIzNDA2MjMzMzc0OSIsImUiOiIyNTkzNDQ3MjMwNTUwNjIwNTk5MDcwMjU0OTE0ODA2OTc1NzE5MzgyNzc4ODk1MTUxNTIzMDYyNDk3Mjg1ODMxMDU2NjU4MDA3MTMzMDY3NTkxNDk5ODE2OTA1NTkxOTM5ODcxNDMwMTIzNjc5MTMyMDYyOTkzMjM4OTk2OTY5NDIyMTMyMzU5NTY3NDI5MzAxMzgxMDE1NDA1MjExMzIyOTAyMjgyNzk3NjM0NDM3NTAxNDkiLCJ2IjoiOTI3MjIxODI0NzE5ODYxNzc0Nzk0ODU4NTgzNDAwNjY1MDM4NDQ2MjE4ODE4MDU2MDY3NjYwNzA5MTE5NDg1NTg4OTY3MDY0OTU1NTM0MjA2MDkyOTQ1MjU3NzgwMzMzMzQwMTAyNTA0MzI3OTMzNjYwNzA5NTUzMzk5MzE4OTE4NTQxNjUxMTgxMDM2NjgwOTMzNzM0MjUyNDA2NDYzMTg2OTQxODk3MDQ1NTA1OTUyMDYwNzI5NTExODM3NzA1OTM3MjQxMzYwMzcyNjM4NTEwODk1MDMxNzc5NjMxODUyNjQyOTIyNTQwNzkxOTc2NzgzMDc2OTk3MTI5MzIyNjA1NjAzNzIyMjA4ODMwOTAyNzcwNTM5Njk2ODA3MTI0MzQ0MzY0NDk1ODAzNDgxNTg4MzczMTk5Mjg3OTgyMjM2MTE5MjIwODU3OTk2NzQwNzI3MjA2OTc4NjMyMjU1NDY0NTk5NDE2NTMwOTYwOTc0ODg0NjczMDgzMzk5NjY1NDI2Mjg1NzY0NDI0NjMwMDMxODA1ODEwNjE5MDI3Njc3MjY4NDAzNzA3NzIzOTAzNDMzOTAxMzg3NDkyMzEyNzA3Nzk4MTcxMzk5ODI0NjM5NDQ1MjI4ODAxNDQ0OTIwMzk5OTkwMzAzMDYzNzg1NDg1NDU3NDIyODQwNjI1NjYyMDg5MzM3NzYxNTIzODUyMTUzMTMwMTIzMzA2NzExNjkzMTkxNTI4MTUzNzk4MjQ0ODE5OTAzMTQ4NjM0MTYzMDg2NDQ4ODkyOTc0ODgzMzQyMzUzMDI0MTA4MjM3MzY1MjI0NTI5MTA5NzEyNjE4ODc4MzY3OTc4Njg3NTY3NTM1MTEzODg1NzU4MTg0ODU2NTU1NzI3MTY0MTk0MTAyODg3MDQ1OTg4NDc2NTI0OTE2NTA0NzM5NDY1Mzk1MjQzMDc0NTY4OTU2MzExODY3OTA5ODc4NDE2MDMwODY1MjY4NDQzNTg2NzI5NjQ3NzA2NjUzNzcwNDU1NjcwOTc4NzkyNDE0Mjc5MzM1NDk0NTE1OTUxMTQ4ODg3MjAzMDg2NjYyMTEwODc1NjA5NjM4NjMxNCJ9LCJyX2NyZWRlbnRpYWwiOm51bGx9LCJzaWduYXR1cmVfY29ycmVjdG5lc3NfcHJvb2YiOnsic2UiOiIyMzQ0OTA4OTM4ODc3NDk2OTAwMTU1NzIzNTE5NzU1OTc2NDEzMzYzMDI4NzE1NTY4Nzc3MzgwNDM4NTA5NDQ0MDE0MjIzMTk5OTg4NDA1NDM0OTU3NzkzMzY5NzE0ODg3OTA4MDI1MDgwNjc0NDQ2MDQ2ODIyNDM2NTQ5Mzg1OTk3NzA5OTc3MTYwMDE1MTM0MTAzMjQ4OTkyNTI3OTYyNTk2OTIwMTA3ODUyNzYzMDUxNDUyMjM5MzEwOTQyODU1OTk3OTEwNzA0ODgxMTQ1OTE4NjI4MTU2NjA4NDUzNzc5MzcyMDIyMzI5ODM3MzYzOTQ2MDgzNTQ2OTE5NjY1OTA4MzY5NzAxMTc2NTk2Njk5MzQ1MDU2NzQ1MjI3MjY0MzczMTYyMTM4NTE0MDQxMTU3NjY5ODAzNjQ5ODgzNjg0NTA4NjQyNTc4OTM2MDc2Mjg2NDk1NzgzMTY2MTM4NjAyNjIzMDU4Nzk2OTk4NzA5NzYyODAzNzYxNzkyODI2NDUzNjIzODg4MjQ3ODkzMzc1MDQxODY0NTU0MDIwMzY5OTY0MzQwMDQ4Njg2NzAzMDM5NzkyODkzMzA1NzQ2MDE4NDkyMTIxMjk5ODI4MTYwNTU1Njg5MDQ4MzgyNTQ4OTU4NjY2OTI2MzMwMzU3OTczMDc0OTY2MzA5OTUwMDczMTMwMjA0NjgxNjI2MjQ5ODIxNTA0MjY0MTU3ODkwMzg3MTQ4OTk4MjU5NTc1MTE3NDI2MTM1MjU5MjMwMTE5MTY3NDE2MTAxNTIzMTA0NTQ1NjU4MTkyMjE2NzY5NTA5MTkxMzMxOSIsImMiOiIzNjU3Nzg5MjU2Mjk4NTIzNDUxNDYzODU4ODIxODcwNDgzOTI5MDAyNTI5NTUxMTI5ODc1NTI3NzY5NDQ2NjE5MDUyODMxMzE3ODkifSwicmV2X3JlZyI6bnVsbCwid2l0bmVzcyI6bnVsbH0"
                )
            )

        val issuedCredential = IssueCredential(
            body = IssueCredential.Body(),
            attachments = arrayOf(attachmentDescriptor),
            thid = "1",
            from = fromDID,
            to = toDID
        )

        val credential = agent.processIssuedCredentialMessage(issuedCredential)

        credential.claims.map {
            if (it.key == "emailAddress") {
                assertEquals("alice@wonderland.com", (it.value as ClaimType.StringValue).value)
            }
            if (it.key == "familyName") {
                assertEquals("Wonderland", (it.value as ClaimType.StringValue).value)
            }
            if (it.key == "dateOfIssuance") {
                assertEquals("2020-11-13T20:20:39+00:00", (it.value as ClaimType.StringValue).value)
            }
            if (it.key == "drivingLicenseID") {
                assertEquals("12345", (it.value as ClaimType.StringValue).value)
            }
            if (it.key == "drivingClass") {
                assertEquals("3", (it.value as ClaimType.StringValue).value)
            }
        }
    }

    val getCredentialDefinitionResponse =
        "{\"schemaId\":\"http://host.docker.internal:8000/prism-agent/schema-registry/schemas/5e0d5a93-4bfd-3111-a956-5d5bc82f76cc\",\"type\":\"CL\",\"tag\":\"licence\",\"value\":{\"primary\":{\"n\":\"105195159277979097653318357586659371305119697478469834190626350283715795188687389523188659352120689851168860621983864738336838773213022505168653440146374011050277159372491059901432822905781969400722059341786498751125483895348734607382548396665339315322605154516776326303787844694026898270194867398625429469096229269732265502538641116512214652017416624138065704599041020588805936844771273861390913500753293895219370960892829297672575154196820931047049021760519166121287056337193413235473255257349024671869248216238831094979209384406168241010010012567685965827447177652200129684927663161550376084422586141212281146491949\",\"s\":\"85376740935726732134199731472843597191822272986425414914465211197069650618238336366149699822721009443794877925725075553195071288777117865451699414058058985000654277974066307286552934230286237253977472401290858765904161191229985245519871949378628131263513153683765553672655918133136828182050729012388157183851720391379381006921499997765191873729408614024320763554099291141052786589157823043612948619201525441997065264492145372001259366749278235381762443117203343617927241093647322654346302447381494008414208398219626199373278313446814209403507903682881070548386699522575055488393512785511441688197244526708647113340516\",\"r\":{\"dateofissuance\":\"16159515692057558658031632775257139859912833740243870833808276956469677196577164655991169139545328065546186056342530531355718904597216453319851305621683589202769847381737819412615902541110462703838858425423753481085962114120185123089078513531045426316918036549403698066078445947881055316312848598741184161901260446303171175343050250045452903485086185722998336149005743485268486377824763449026501058416292877646187105446333888525480394665310217044483841168928926515929150167890936706159800372381200383816724043496032886366767166850459338411710056171379538841845247931898550165532492578625954615979453881721709564750235\",\"drivingclass\":\"83649701835078373520097916558245060224505938113940626586910000950978790663411517512280043632278010831292224659523658613504637416710001103641231226266903556936380105758523760424939825687213460920436570466066231912959327201876189240504388424799892400351592593406285436824571943165913587899115814843543998396726679289422080229750418336051741708013580146373647528674381958028243228435161765957312248113519708734663989428761879029086059388435772829434952754093999424834120341657211221855300108096057633128467059590470639772605075954658131680801785637700237403873940041665483384938586320674338994185073499523485570537331062\",\"emailaddress\":\"96995643129591814391344614133120459563648002327749700279517548454036811217735867585059116635583558148259032071807493674533230465312311981127622542797279917256478867847832932893748528200469349058284133058865149153179959849308383505167342565738382180666525211256221655129861213392455759272915565057394420728271409215556596974900718332893753172173500744392522771654048192448229319313386967045678744665093451560743782910263014930200762027209565313884859542996067229707388839912195826334964819133016500346618083969320902775088800287566711941842968839787149808739739233388585677095545116231323172342995837636586249573194609\",\"drivinglicenseid\":\"102840929811153624977554462471309185033977661854754815794111114507549576719389525167082631547450413573293352276930065480432301200611396989595571202142654033217842162456070556560693402484110499573693863745648118310258284468114751958738878996458420605301017450868522680454545537837403398645500541915771765220093329728663621098538954397330411649083351383375839056527007892276284168437065687748085384178113959961057476582871100422859953560730152958588610850909069434658487744782540788968302663076149478487413357533660817020800754493642858564081116318655661240523146995256712471572605700346459123074377380656921337264554594\",\"familyname\":\"2428690037146701497427424649573806616639612325136606164619283916796880313617677563507218774958436668407050506838114136163250163675016510113975582318007560622124292458766639319715064358235569650961433812439763343736699708535945693241909905707497180931492818502593885932421170612418693515054756633264933222189766691632082890045477718331705366111669009551578289182848340651375008362238266590844461708981816856194045325523248527964502118319210042254240848590574645476930113881493472578612352948284862674703949781070309344526122291448990325949065193279599181502524961004046979227803224474342778516917124487012958845744311\",\"master_secret\":\"96236339155824229583363924057798366491998077727991424922911165403434522806469328114407334094535810942859512352089785125683335350062474092708044674085769524387654467267128528564551803293661877480971961092735622606052503557881856409855812611523475975566606131897917979412576797874632169829901968854843162299366867885636535326810998541141840561418097240137120398317445832694001031827068485975315937269024666370665530455146256019590700349556357390218401217383173228376078058967743472704019765210324846681867991543267171763037513180046865961560351035005185946817643006206395175857900512245900162751815626427008481585714891\"},\"rctxt\":\"54359809198312125478916383106913469635175253891208897419510030559787479974126666313900084654632259260010008369569778456071591398552341004538623276997178295939490854663263886825856426285604332554317424030793691008221895556474599466123873279022389276698551452690414982831059651505731449763128921782866843113361548859434294057249048041670761184683271568216202174527891374770703485794299697663353847310928998125365841476766767508733046891626759537001358973715760759776149482147060701775948253839125589216812475133616408444838011643485797584321993661048373877626880635937563283836661934456534313802815974883441215836680800\",\"z\":\"99592262675748359673042256590146366586480829950402370244401571195191609039150608482506917768910598228167758026656953725016982562881531475875469671976107506976812319765644401707559997823702387678953647104105378063905395973550729717937712350758544336716556268064226491839700352305793370980462034813589488455836259737325502578253339820590260554457468082536249525493340350556649403477875367398139579018197084796440810685458274393317299082017275568964540311198115802021902455672385575542594821996060452628805634468222196284384514736044680778624637228114693554834388824212714580770066729185685978935409859595244639193538156\"}},\"issuerId\":\"did:prism:604ba1764ab89993f9a74625cc4f3e04737919639293eb382cc7adc53767f550\"}"
}
