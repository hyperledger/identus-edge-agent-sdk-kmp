package io.iohk.atala.prism.walletsdk.mercury

import io.iohk.atala.prism.walletsdk.domain.models.DID
import io.iohk.atala.prism.walletsdk.domain.models.MercuryError
import io.iohk.atala.prism.walletsdk.domain.models.Message
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue


@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class MercuryTests {

    lateinit var apiMock: ApiMock
    lateinit var castorMock: CastorMock
    lateinit var protocolMock: ProtocolMock
    lateinit var sut: MercuryImpl

    @BeforeTest
    fun setup() {
        apiMock = ApiMock()
        castorMock = CastorMock()
        protocolMock = ProtocolMock()
        sut = MercuryImpl(castorMock, protocolMock, apiMock)
    }

    @Test
    fun testPackMessage_shouldThrowError_whenToAbsent() = runTest {
        val msg = Message(piuri = "", body = "")
        assertFailsWith<MercuryError.NoDIDReceiverSetError> { sut.packMessage(msg) }
    }

    @Test
    fun testPackMessage_shouldCall_ProtocolPackEncrypted() = runTest {
        val to = DID("test", "method", "id")
        val msg = Message(piuri = "", body = "", to = to)
        sut.packMessage(msg)
        assertTrue { protocolMock.packEncryptedWasCalledWith === msg }
    }


    @Test
    fun testUnpackMessage_shouldCall_ProtocolUnpack() = runTest {
        val messageString = "testMessageString"
        sut.unpackMessage(messageString)
        assertTrue { protocolMock.unpackWasCalledWith === messageString }
    }


    @Test
    fun testSendMessage_shouldThrowError_whenToAbsent() = runTest {
        val msg = Message(piuri = "", body = "")
        assertFailsWith<MercuryError.NoDIDReceiverSetError> { sut.sendMessage(msg) }
    }

    @Test
    fun testSendMessage_shouldThrowError_whenServiceAbsent() = runTest {
        val msg = Message(piuri = "", body = "", to = DID("test", "method", "id"))
        assertFailsWith<MercuryError.NoValidServiceFoundError> { sut.sendMessage(msg) }
    }


}