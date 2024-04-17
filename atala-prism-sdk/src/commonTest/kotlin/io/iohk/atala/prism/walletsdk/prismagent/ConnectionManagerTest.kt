@file:Suppress("ktlint:standard:import-ordering")

package io.iohk.atala.prism.walletsdk.prismagent

import io.iohk.atala.prism.apollo.base64.base64UrlEncoded
import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Castor
import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Mercury
import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Pluto
import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Pollux
import io.iohk.atala.prism.walletsdk.domain.models.AttachmentBase64
import io.iohk.atala.prism.walletsdk.domain.models.AttachmentDescriptor
import io.iohk.atala.prism.walletsdk.domain.models.CredentialType
import io.iohk.atala.prism.walletsdk.domain.models.Curve
import io.iohk.atala.prism.walletsdk.domain.models.DID
import io.iohk.atala.prism.walletsdk.domain.models.DIDDocument
import io.iohk.atala.prism.walletsdk.domain.models.DIDUrl
import io.iohk.atala.prism.walletsdk.domain.models.Message
import io.iohk.atala.prism.walletsdk.prismagent.mediation.MediationHandler
import io.iohk.atala.prism.walletsdk.prismagent.protocols.ProtocolType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.runTest
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import org.mockito.Mockito.anyList
import org.mockito.kotlin.anyArray
import org.mockito.kotlin.argumentCaptor
import kotlin.test.assertNotNull
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

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

    lateinit var connectionManager: ConnectionManager

    val testDispatcher = TestCoroutineDispatcher()

    @BeforeTest
    fun setup() {
        MockitoAnnotations.openMocks(this)
        connectionManager = ConnectionManager(
            mercury = mercuryMock,
            castor = castorMock,
            pluto = plutoMock,
            mediationHandler = basicMediatorHandlerMock,
            pairings = mutableListOf(),
            pollux = polluxMock,
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
    fun testStartFetchingMessages_whenServiceEndpointNotContainsWSS_thenUseAPIRequest() =
        runBlockingTest {
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
