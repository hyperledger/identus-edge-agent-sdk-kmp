package org.hyperledger.identus.walletsdk.pluto

import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.hyperledger.identus.walletsdk.apollo.utils.Secp256k1KeyPair
import org.hyperledger.identus.walletsdk.domain.models.DID
import org.hyperledger.identus.walletsdk.domain.models.Message
import org.hyperledger.identus.walletsdk.domain.models.PlutoError
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.StorableKey
import org.hyperledger.identus.walletsdk.pollux.models.JWTCredential
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.MockitoAnnotations
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PlutoImplTest {

    private var pluto: PlutoImpl? = null

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        pluto = PlutoImpl(DbConnectionInMemory())
    }

    @After
    fun destroy() {
        pluto = null
    }

    @Test
    fun `verify Pluto is connected after start`() = runTest {
        pluto?.start()
        assertEquals(false, pluto?.isConnected)
    }

    @Test
    fun `verify Pluto is not connected if not started`() = runTest {
        assertEquals(false, pluto?.isConnected)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `test Pluto throws an expection if started more than once`() = runTest {
        val context = Any()
        pluto?.start(context)
        assertFailsWith<PlutoError.DatabaseServiceAlreadyRunning> {
            pluto?.start(context)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `test storePrismDIDAndPrivateKeys stores DID and private keys`() = runTest {
        val did = DID("did:prism:example")
        val secp256PrivateKey = Secp256k1KeyPair.generateKeyPair()
        val alias = "alias"

        pluto?.start()
        pluto?.storePrismDIDAndPrivateKeys(did, 1, alias, listOf(secp256PrivateKey.privateKey as StorableKey))

        val dids = pluto?.getAllDIDs()?.first()
        assertNotNull(dids)
        assertTrue(dids!!.isNotEmpty())
        assertEquals(1, dids.size)
        assertEquals(did.toString(), dids.first().toString())

        val privateKeys = pluto?.getAllPrivateKeys()?.first()
        assertNotNull(privateKeys)
        assertTrue(privateKeys!!.isNotEmpty())
        assertEquals(1, privateKeys.size)

        val didKeyLinks = pluto?.getAllDIDKeyLinkData()?.first()
        assertNotNull(didKeyLinks)
        assertTrue(didKeyLinks!!.isNotEmpty())
        assertEquals(1, didKeyLinks.size)
    }

    @Test
    fun `test storePeerDID stores DID`() = runTest {
        val peerDID = DID("did:peer:test")
        pluto?.start()
        pluto?.storePeerDID(peerDID)

        val dids = pluto?.getAllDIDs()?.first()
        assertNotNull(dids)
        assertTrue(dids!!.isNotEmpty())
        assertEquals(1, dids.size)
        assertEquals(peerDID.toString(), dids.first().toString())
    }

    @Test
    fun `test storeDIDPair stores a two DIDs and alias`() = runTest {
        val host = DID("did:peer:host")
        val receiver = DID("did:peer:receiver")
        val alias = "alias"
        pluto?.start()
        pluto?.storeDIDPair(host, receiver, alias)

        val didPairs = pluto?.getAllDidPairs()?.first()
        assertNotNull(didPairs)
        assertTrue(didPairs!!.isNotEmpty())
        assertEquals(1, didPairs.size)
        val didPair = didPairs.first()
        assertEquals(host.toString(), didPair.holder.toString())
        assertEquals(receiver.toString(), didPair.receiver.toString())
        assertEquals(alias, didPair.name.toString())
    }

    @Test
    fun `test storeMessage stores a message`() = runTest {
        val message = Message(
            piuri = "https://didcomm.atalaprism.io/present-proof/3.0/request-presentation",
            from = DID("did:peer:from"),
            to = DID("did:peer:to"),
            body = "{}"
        )

        pluto?.start()
        pluto?.storeMessage(message)

        val messages = pluto?.getAllMessages()?.first()
        assertNotNull(messages)
        assertTrue(messages!!.isNotEmpty())
        assertEquals(1, messages.size)
        val msg = messages.first()
        assertEquals(message, msg)
    }

    @Test
    fun `test storePrivateKeys stores a storableKey, DID, keyPathIndex and metaId`() = runTest {
        val secp256PrivateKey = Secp256k1KeyPair.generateKeyPair().privateKey
        val did = DID("did:peer:example")

        pluto?.start()
        pluto?.storePrivateKeys(
            storableKey = secp256PrivateKey as StorableKey,
            did = did,
            keyPathIndex = 0,
            metaId = ""
        )

        val privateKeys = pluto?.getAllPrivateKeys()?.first()
        assertNotNull(privateKeys)
        assertTrue(privateKeys!!.isNotEmpty())
        assertEquals(1, privateKeys.size)

        val didKeyLinks = pluto?.getAllDIDKeyLinkData()?.first()
        assertNotNull(didKeyLinks)
        assertTrue(didKeyLinks!!.isNotEmpty())
        assertEquals(1, didKeyLinks.size)
    }

    @Test
    fun `test storePrivate`() = runTest {
        val privateKey = Secp256k1KeyPair.generateKeyPair().privateKey

        pluto?.start()
        pluto?.storePrivate(privateKey as StorableKey, recoveryId = "recoveryId")

        val privateKeys = pluto?.getAllPrivateKeys()?.first()
        assertNotNull(privateKeys)
        assertTrue(privateKeys!!.isNotEmpty())
        assertEquals(1, privateKeys.size)
        assertEquals("recoveryId", privateKeys.first().restorationIdentifier)
    }

    @Test
    fun `test storeMessages`() = runTest {
        val message = Message(
            piuri = "https://didcomm.atalaprism.io/present-proof/3.0/request-presentation",
            from = DID("did:peer:from"),
            to = DID("did:peer:to"),
            body = "{}"
        )

        val message1 = Message(
            piuri = "https://didcomm.atalaprism.io/present-proof/3.0/request-presentation",
            from = DID("did:peer:from"),
            to = DID("did:peer:to"),
            body = "{}"
        )

        pluto?.start()
        pluto?.storeMessages(listOf(message, message1))

        val messages = pluto?.getAllMessages()?.first()
        assertNotNull(messages)
        assertTrue(messages!!.isNotEmpty())
        assertEquals(2, messages.size)
        val msg = messages.first()
        assertEquals(message, msg)
        val msg1 = messages[1]
        assertEquals(message1, msg1)
    }

    @Test
    fun `test storeMediator`() = runTest {
        val mediatorDID = DID("did:peer:mediator")
        val hostDID = DID("did:peer:host")
        val routingDID = DID("did:peer:routing")

        pluto?.start()
        pluto?.storePeerDID(hostDID)
        pluto?.storeMediator(mediatorDID, hostDID, routingDID)

        val dids = pluto?.getAllDIDs()?.first()
        assertNotNull(dids)
        assertTrue(dids!!.isNotEmpty())
        assertEquals(3, dids.size)
        val didsString = dids.map { it.toString() }
        assertTrue(didsString.contains(mediatorDID.toString()))
        assertTrue(didsString.contains(hostDID.toString()))
        assertTrue(didsString.contains(routingDID.toString()))

        val mediators = pluto?.getAllMediators()?.first()
        assertNotNull(mediators)
        assertTrue(mediators!!.isNotEmpty())
        assertEquals(1, mediators.size)
        val mediator = mediators.first()
        assertEquals(mediatorDID.toString(), mediator.mediatorDID.toString())
        assertEquals(hostDID.toString(), mediator.hostDID.toString())
        assertEquals(routingDID.toString(), mediator.routingDID.toString())
    }

    @Test
    fun `test storeCredential`() = runTest {
        val credential = JWTCredential.fromJwtString(
            "eyJhbGciOiJFUzI1NksifQ.eyJpc3MiOiJkaWQ6cHJpc206MjU3MTlhOTZiMTUxMjA3MTY5ODFhODQzMGFkMGNiOTY4ZGQ1MzQwNzM1OTNjOGNkM2YxZDI3YTY4MDRlYzUwZTpDcG9DQ3BjQ0Vsb0tCV3RsZVMweEVBSkNUd29KYzJWamNESTFObXN4RWlBRW9TQ241dHlEYTZZNnItSW1TcXBKOFkxbWo3SkMzX29VekUwTnl5RWlDQm9nc2dOYWVSZGNDUkdQbGU4MlZ2OXRKZk53bDZyZzZWY2hSM09xaGlWYlRhOFNXd29HWVhWMGFDMHhFQVJDVHdvSmMyVmpjREkxTm1zeEVpRE1rQmQ2RnRpb0prM1hPRnUtX2N5NVhtUi00dFVRMk5MR2lXOGFJU29ta1JvZzZTZGU5UHduRzBRMFNCVG1GU1REYlNLQnZJVjZDVExYcmpJSnR0ZUdJbUFTWEFvSGJXRnpkR1Z5TUJBQlFrOEtDWE5sWTNBeU5UWnJNUklnTzcxMG10MVdfaXhEeVFNM3hJczdUcGpMQ05PRFF4Z1ZoeDVzaGZLTlgxb2FJSFdQcnc3SVVLbGZpYlF0eDZKazRUU2pnY1dOT2ZjT3RVOUQ5UHVaN1Q5dCIsInN1YiI6ImRpZDpwcmlzbTpiZWVhNTIzNGFmNDY4MDQ3MTRkOGVhOGVjNzdiNjZjYzdmM2U4MTVjNjhhYmI0NzVmMjU0Y2Y5YzMwNjI2NzYzOkNzY0JDc1FCRW1RS0QyRjFkR2hsYm5ScFkyRjBhVzl1TUJBRVFrOEtDWE5sWTNBeU5UWnJNUklnZVNnLTJPTzFKZG5welVPQml0eklpY1hkZnplQWNUZldBTi1ZQ2V1Q2J5SWFJSlE0R1RJMzB0YVZpd2NoVDNlMG5MWEJTNDNCNGo5amxzbEtvMlpsZFh6akVsd0tCMjFoYzNSbGNqQVFBVUpQQ2dselpXTndNalUyYXpFU0lIa29QdGpqdFNYWjZjMURnWXJjeUluRjNYODNnSEUzMWdEZm1BbnJnbThpR2lDVU9Ca3lOOUxXbFlzSElVOTN0Snkxd1V1TndlSV9ZNWJKU3FObVpYVjg0dyIsIm5iZiI6MTY4NTYzMTk5NSwiZXhwIjoxNjg1NjM1NTk1LCJ2YyI6eyJjcmVkZW50aWFsU3ViamVjdCI6eyJhZGRpdGlvbmFsUHJvcDIiOiJUZXN0MyIsImlkIjoiZGlkOnByaXNtOmJlZWE1MjM0YWY0NjgwNDcxNGQ4ZWE4ZWM3N2I2NmNjN2YzZTgxNWM2OGFiYjQ3NWYyNTRjZjljMzA2MjY3NjM6Q3NjQkNzUUJFbVFLRDJGMWRHaGxiblJwWTJGMGFXOXVNQkFFUWs4S0NYTmxZM0F5TlRack1SSWdlU2ctMk9PMUpkbnB6VU9CaXR6SWljWGRmemVBY1RmV0FOLVlDZXVDYnlJYUlKUTRHVEkzMHRhVml3Y2hUM2UwbkxYQlM0M0I0ajlqbHNsS28yWmxkWHpqRWx3S0IyMWhjM1JsY2pBUUFVSlBDZ2x6WldOd01qVTJhekVTSUhrb1B0amp0U1haNmMxRGdZcmN5SW5GM1g4M2dIRTMxZ0RmbUFucmdtOGlHaUNVT0JreU45TFdsWXNISVU5M3RKeTF3VXVOd2VJX1k1YkpTcU5tWlhWODR3In0sInR5cGUiOlsiVmVyaWZpYWJsZUNyZWRlbnRpYWwiXSwiQGNvbnRleHQiOlsiaHR0cHM6XC9cL3d3dy53My5vcmdcLzIwMThcL2NyZWRlbnRpYWxzXC92MSJdfX0.x0SF17Y0VCDmt7HceOdTxfHlofsZmY18Rn6VQb0-r-k_Bm3hTi1-k2vkdjB25hdxyTCvxam-AkAP-Ag3Ahn5Ng"
        )

        pluto?.start()
        pluto?.storeCredential(credential.toStorableCredential())

        val credentials = pluto?.getAllCredentials()?.first()
        assertNotNull(credentials)
        assertTrue(credentials!!.isNotEmpty())
        assertEquals(1, credentials.size)
        assertEquals("jwt+credential", credentials.first().restorationId)
    }

    @Test
    fun `test storeLinkSecret`() = runTest {
        val linkSecret = "linkSecret"
        pluto?.start()
        pluto?.storeLinkSecret(linkSecret)

        val linkSecretString = pluto?.getLinkSecret()?.first()
        assertEquals(linkSecret, linkSecretString)
    }

    @Test
    fun `test storeCredentialMetadata`() = runTest {
        val name = "meta"
        val linkSecretName = "linkSecretName"
        val json = "{}"

        pluto?.start()
        pluto?.storeCredentialMetadata(name, linkSecretName, json)

        val credentialMetadata = pluto?.getCredentialMetadata(linkSecretName)?.first()
        assertNotNull(credentialMetadata)
        assertEquals(linkSecretName, credentialMetadata!!.linkSecretName)
        assertEquals(json, credentialMetadata.json)
    }

//    @Test
//    fun `test getAllPrismDIDs returns list of PrismDIDInfo`() = runTest {
//        val did = DID("did", "prism", "asdfasdf", "alias")
//        val fetchAllPrismDID = FetchAllPrismDID(
//            did = did.toString(),
//            method = did.method,
//            methodId = did.methodId,
//            schema = did.schema,
//            alias = "alias",
//            keyPathIndex = null
//        )
//        whenever(sdkPlutoDbMock.dIDQueries.fetchAllPrismDID().asFlow().mapToList(any()))
//            .thenReturn(flowOf(listOf(fetchAllPrismDID)))
//
//        val context = Any()
//        pluto.start(context)
//        val result = pluto.getAllPrismDIDs()
//        assertEquals(
//            listOf(PrismDIDInfo(did, null, "alias")),
//            result.first()
//        )
//    }

//    @Test
//    fun `test getMessage returns message`() = runTest {
//        val messageDb = MessageDB(
//            id = "messageId",
//            createdTime = 0L,
//            dataJson = "dataJson",
//            from = "from",
//            thid = "thid",
//            to = "to",
//            piuri = "piuri",
//            direction = 0
//        )
//        whenever(sdkPlutoDbMock.messageQueries.fetchMessageById(any()).executeAsOne()).thenReturn(messageDb)
//          val context = Any()
//          pluto.start(context)
//        val result = pluto.getMessage("messageId")
//        assertEquals(
//            Message(
//                id = "messageId",
//                piuri = "piuri",
//                from = DID("from"),
//                to = DID("to"),
//                fromPrior = null,
//                body = "body",
//                extraHeaders = emptyMap(),
//                createdTime = 0L,
//                expiresTimePlus = null,
//                attachments = emptyList(),
//                thid = "thid",
//                pthid = null,
//                ack = null,
//                direction = Message.Direction.INBOUND
//            ),
//            result.first()
//        )
//    }
}
