package org.hyperledger.identus.walletsdk.edgeagent

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.hyperledger.identus.apollo.base64.base64UrlEncoded
import org.hyperledger.identus.walletsdk.domain.buildingblocks.Castor
import org.hyperledger.identus.walletsdk.domain.buildingblocks.Mercury
import org.hyperledger.identus.walletsdk.domain.buildingblocks.Pluto
import org.hyperledger.identus.walletsdk.domain.buildingblocks.Pollux
import org.hyperledger.identus.walletsdk.domain.models.AttachmentData.AttachmentBase64
import org.hyperledger.identus.walletsdk.domain.models.AttachmentDescriptor
import org.hyperledger.identus.walletsdk.domain.models.CredentialType
import org.hyperledger.identus.walletsdk.domain.models.Curve
import org.hyperledger.identus.walletsdk.domain.models.DID
import org.hyperledger.identus.walletsdk.domain.models.DIDDocument
import org.hyperledger.identus.walletsdk.domain.models.DIDUrl
import org.hyperledger.identus.walletsdk.domain.models.Message
import org.hyperledger.identus.walletsdk.edgeagent.mediation.MediationHandler
import org.hyperledger.identus.walletsdk.edgeagent.protocols.ProtocolType
import org.mockito.Mock
import org.mockito.Mockito.anyList
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.anyArray
import org.mockito.kotlin.argumentCaptor
import java.util.UUID
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ConnectionManagerTest {

    @Mock
    lateinit var mercuryMock: Mercury

    @Mock
    lateinit var castorMock: Castor

    @Mock
    lateinit var plutoMock: Pluto

    @Mock
    lateinit var polluxMock: Pollux

    @Mock
    lateinit var basicMediatorHandlerMock: MediationHandler

    lateinit var connectionManager: ConnectionManagerImpl

    @OptIn(ExperimentalCoroutinesApi::class)
    val testDispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setup() {
        MockitoAnnotations.openMocks(this)
        connectionManager = ConnectionManagerImpl(
            mercury = mercuryMock,
            castor = castorMock,
            pluto = plutoMock,
            mediationHandler = basicMediatorHandlerMock,
            pairings = mutableListOf(),
            pollux = polluxMock,
            experimentLiveModeOptIn = true,
            scope = CoroutineScope(testDispatcher)
        )
    }

    @Test
    fun testStartFetchingMessages_whenServiceEndpointContainsWSS_thenUseWebsockets() = runTest {
        `when`(basicMediatorHandlerMock.mediatorDID)
            .thenReturn(DID("did:prism:b6c0c33d701ac1b9a262a14454d1bbde3d127d697a76950963c5fd930605:Cj8KPRI7CgdtYXN0ZXIwEAFKLgoJc2VmsxEiECSTjyV7sUfCr_ArpN9rvCwR9fRMAhcsr_S7ZRiJk4p5k"))

        val vmAuthentication = DIDDocument.VerificationMethod(
            id = DIDUrl(DID("2", "1", "0")),
            controller = DID("2", "2", "0"),
            type = Curve.ED25519.value,
            publicKeyJwk = mapOf("crv" to Curve.ED25519.value, "x" to "")
        )

        val vmKeyAgreement = DIDDocument.VerificationMethod(
            id = DIDUrl(DID("3", "1", "0")),
            controller = DID("3", "2", "0"),
            type = Curve.X25519.value,
            publicKeyJwk = mapOf("crv" to Curve.X25519.value, "x" to "")
        )

        val vmService = DIDDocument.Service(
            id = UUID.randomUUID().toString(),
            type = emptyArray(),
            serviceEndpoint = DIDDocument.ServiceEndpoint(
                uri = "wss://serviceEndpoint"
            )
        )

        val didDoc = DIDDocument(
            id = DID("did:prism:asdfasdf"),
            coreProperties = arrayOf(
                DIDDocument.Authentication(
                    urls = emptyArray(),
                    verificationMethods = arrayOf(vmAuthentication)
                ),
                DIDDocument.KeyAgreement(
                    urls = emptyArray(),
                    verificationMethods = arrayOf(vmKeyAgreement)
                ),
                DIDDocument.Services(
                    values = arrayOf(vmService)
                )
            )
        )

        `when`(castorMock.resolveDID(any())).thenReturn(didDoc)

        connectionManager.startFetchingMessages()
        assertNotNull(connectionManager.fetchingMessagesJob)
        verify(basicMediatorHandlerMock).listenUnreadMessages(any(), any())
    }

    @Test
    fun testStartFetchingMessages_whenServiceEndpointContainsWSSButOptInLiveModeFalse_thenRegunarlApi() = runTest {
        connectionManager = ConnectionManagerImpl(
            mercury = mercuryMock,
            castor = castorMock,
            pluto = plutoMock,
            mediationHandler = basicMediatorHandlerMock,
            pairings = mutableListOf(),
            pollux = polluxMock,
            experimentLiveModeOptIn = false,
            scope = CoroutineScope(testDispatcher)
        )

        `when`(basicMediatorHandlerMock.mediatorDID)
            .thenReturn(DID("did:prism:b6c0c33d701ac1b9a262a14454d1bbde3d127d697a76950963c5fd930605:Cj8KPRI7CgdtYXN0ZXIwEAFKLgoJc2VmsxEiECSTjyV7sUfCr_ArpN9rvCwR9fRMAhcsr_S7ZRiJk4p5k"))

        val vmAuthentication = DIDDocument.VerificationMethod(
            id = DIDUrl(DID("2", "1", "0")),
            controller = DID("2", "2", "0"),
            type = Curve.ED25519.value,
            publicKeyJwk = mapOf("crv" to Curve.ED25519.value, "x" to "")
        )

        val vmKeyAgreement = DIDDocument.VerificationMethod(
            id = DIDUrl(DID("3", "1", "0")),
            controller = DID("3", "2", "0"),
            type = Curve.X25519.value,
            publicKeyJwk = mapOf("crv" to Curve.X25519.value, "x" to "")
        )

        val vmService = DIDDocument.Service(
            id = UUID.randomUUID().toString(),
            type = emptyArray(),
            serviceEndpoint = DIDDocument.ServiceEndpoint(
                uri = "wss://serviceEndpoint"
            )
        )

        val didDoc = DIDDocument(
            id = DID("did:prism:asdfasdf"),
            coreProperties = arrayOf(
                DIDDocument.Authentication(
                    urls = emptyArray(),
                    verificationMethods = arrayOf(vmAuthentication)
                ),
                DIDDocument.KeyAgreement(
                    urls = emptyArray(),
                    verificationMethods = arrayOf(vmKeyAgreement)
                ),
                DIDDocument.Services(
                    values = arrayOf(vmService)
                )
            )
        )

        `when`(castorMock.resolveDID(any())).thenReturn(didDoc)
        val messages = arrayOf(Pair("1234", Message(piuri = "", body = "")))
        `when`(basicMediatorHandlerMock.pickupUnreadMessages(any())).thenReturn(
            flow {
                emit(
                    messages
                )
            }
        )
        val attachments: Array<AttachmentDescriptor> =
            arrayOf(
                AttachmentDescriptor(
                    mediaType = "application/json",
                    format = CredentialType.JWT.type,
                    data = AttachmentBase64(base64 = "asdfasdfasdfasdfasdfasdfasdfasdfasdf".base64UrlEncoded)
                )
            )
        val listMessages = listOf(
            Message(
                piuri = ProtocolType.DidcommconnectionRequest.value,
                body = ""
            ),
            Message(
                piuri = ProtocolType.DidcommIssueCredential.value,
                thid = UUID.randomUUID().toString(),
                from = DID("did:peer:asdf897a6sdf"),
                to = DID("did:peer:f706sg678ha"),
                attachments = attachments,
                body = """{}"""
            )
        )
        val messageList: Flow<List<Message>> = flow {
            emit(listMessages)
        }
        `when`(plutoMock.getAllMessages()).thenReturn(messageList)

        connectionManager.startFetchingMessages()
        assertNotNull(connectionManager.fetchingMessagesJob)
        verify(basicMediatorHandlerMock).pickupUnreadMessages(10)
        verify(basicMediatorHandlerMock).registerMessagesAsRead(arrayOf("1234"))
    }

    @Test
    fun testStartFetchingMessages_whenServiceEndpointNotContainsWSS_thenUseAPIRequest() = runTest {
        `when`(basicMediatorHandlerMock.mediatorDID)
            .thenReturn(DID("did:prism:b6c0c33d701ac1b9a262a14454d1bbde3d127d697a76950963c5fd930605:Cj8KPRI7CgdtYXN0ZXIwEAFKLgoJc2VmsxEiECSTjyV7sUfCr_ArpN9rvCwR9fRMAhcsr_S7ZRiJk4p5k"))

        val vmAuthentication = DIDDocument.VerificationMethod(
            id = DIDUrl(DID("2", "1", "0")),
            controller = DID("2", "2", "0"),
            type = Curve.ED25519.value,
            publicKeyJwk = mapOf("crv" to Curve.ED25519.value, "x" to "")
        )

        val vmKeyAgreement = DIDDocument.VerificationMethod(
            id = DIDUrl(DID("3", "1", "0")),
            controller = DID("3", "2", "0"),
            type = Curve.X25519.value,
            publicKeyJwk = mapOf("crv" to Curve.X25519.value, "x" to "")
        )

        val vmService = DIDDocument.Service(
            id = UUID.randomUUID().toString(),
            type = emptyArray(),
            serviceEndpoint = DIDDocument.ServiceEndpoint(
                uri = "https://serviceEndpoint"
            )
        )

        val didDoc = DIDDocument(
            id = DID("did:prism:asdfasdf"),
            coreProperties = arrayOf(
                DIDDocument.Authentication(
                    urls = emptyArray(),
                    verificationMethods = arrayOf(vmAuthentication)
                ),
                DIDDocument.KeyAgreement(
                    urls = emptyArray(),
                    verificationMethods = arrayOf(vmKeyAgreement)
                ),
                DIDDocument.Services(
                    values = arrayOf(vmService)
                )
            )
        )

        `when`(castorMock.resolveDID(any())).thenReturn(didDoc)
        val messages = arrayOf(Pair("1234", Message(piuri = "", body = "")))
        `when`(basicMediatorHandlerMock.pickupUnreadMessages(any())).thenReturn(
            flow {
                emit(
                    messages
                )
            }
        )
        val attachments: Array<AttachmentDescriptor> =
            arrayOf(
                AttachmentDescriptor(
                    mediaType = "application/json",
                    format = CredentialType.JWT.type,
                    data = AttachmentBase64(base64 = "asdfasdfasdfasdfasdfasdfasdfasdfasdf".base64UrlEncoded)
                )
            )
        val listMessages = listOf(
            Message(
                piuri = ProtocolType.DidcommconnectionRequest.value,
                body = ""
            ),
            Message(
                piuri = ProtocolType.DidcommIssueCredential.value,
                thid = UUID.randomUUID().toString(),
                from = DID("did:peer:asdf897a6sdf"),
                to = DID("did:peer:f706sg678ha"),
                attachments = attachments,
                body = """{}"""
            )
        )
        val messageList: Flow<List<Message>> = flow {
            emit(listMessages)
        }
        `when`(plutoMock.getAllMessages()).thenReturn(messageList)

        connectionManager.startFetchingMessages()
        assertNotNull(connectionManager.fetchingMessagesJob)
        assert(connectionManager.fetchingMessagesJob?.isActive == true)
        verify(basicMediatorHandlerMock).pickupUnreadMessages(10)
        verify(basicMediatorHandlerMock).registerMessagesAsRead(arrayOf("1234"))
    }

    @Test
    fun testConnectionManager_whenProcessMessageRevoke_thenAllCorrect() = runTest {
        val threadId = UUID.randomUUID().toString()
        val attachments: Array<AttachmentDescriptor> =
            arrayOf(
                AttachmentDescriptor(
                    mediaType = "application/json",
                    format = CredentialType.JWT.type,
                    data = AttachmentBase64(base64 = "asdfasdfasdfasdfasdfasdfasdfasdfasdf".base64UrlEncoded)
                )
            )
        val listMessages = listOf(
            Message(
                piuri = ProtocolType.DidcommconnectionRequest.value,
                body = ""
            ),
            Message(
                piuri = ProtocolType.DidcommIssueCredential.value,
                thid = threadId,
                from = DID("did:peer:asdf897a6sdf"),
                to = DID("did:peer:f706sg678ha"),
                attachments = attachments,
                body = """{}"""
            )
        )
        val messageList: Flow<List<Message>> = flow {
            emit(listMessages)
        }
        `when`(plutoMock.getAllMessages()).thenReturn(messageList)
        `when`(polluxMock.extractCredentialFormatFromMessage(any())).thenReturn(CredentialType.JWT)

        val messages = arrayOf(
            Pair(
                threadId,
                Message(
                    piuri = ProtocolType.PrismRevocation.value,
                    from = DID("did:peer:0978aszdf7890asg"),
                    to = DID("did:peer:asdf9068asdf"),
                    body = """{"issueCredentialProtocolThreadId":"$threadId","comment":null}"""
                )
            )
        )

        connectionManager.processMessages(messages)
        val argumentCaptor = argumentCaptor<String>()
        verify(plutoMock).revokeCredential(argumentCaptor.capture())
        assertEquals("asdfasdfasdfasdfasdfasdfasdfasdfasdf", argumentCaptor.firstValue)
        verify(basicMediatorHandlerMock).registerMessagesAsRead(anyArray())
        verify(plutoMock).storeMessages(anyList())
    }
}
