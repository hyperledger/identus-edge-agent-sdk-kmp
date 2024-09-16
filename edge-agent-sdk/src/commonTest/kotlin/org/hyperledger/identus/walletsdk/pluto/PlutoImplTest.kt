package org.hyperledger.identus.walletsdk.pluto

import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.hyperledger.identus.apollo.base64.base64UrlEncoded
import org.hyperledger.identus.walletsdk.apollo.utils.Ed25519KeyPair
import org.hyperledger.identus.walletsdk.apollo.utils.Secp256k1KeyPair
import org.hyperledger.identus.walletsdk.apollo.utils.X25519KeyPair
import org.hyperledger.identus.walletsdk.domain.models.DID
import org.hyperledger.identus.walletsdk.domain.models.Message
import org.hyperledger.identus.walletsdk.domain.models.PlutoError
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.StorableKey
import org.hyperledger.identus.walletsdk.pollux.models.JWTCredential
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.MockitoAnnotations
import java.util.UUID
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PlutoImplTest {

    private lateinit var pluto: PlutoImpl
    private lateinit var dbConnection: DbConnectionInMemory

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        dbConnection = DbConnectionInMemory()
        pluto = PlutoImpl(dbConnection)
    }

    @After
    fun destroy() {
        if (pluto.isConnected) {
            pluto.stop()
        }
    }

    @Test
    fun `verify Pluto is connected after start`() = runTest {
        pluto.start()
        assertEquals(true, pluto.isConnected)
    }

    @Test
    fun `verify Pluto is not connected if not started`() = runTest {
        assertEquals(false, pluto.isConnected)
    }

    @Test
    fun `test Pluto throws an expection if started more than once`() = runTest {
        val context = Any()
        pluto.start(context)
        assertFailsWith<PlutoError.DatabaseServiceAlreadyRunning> {
            pluto.start(context)
        }
    }

    @Test
    fun `test storePrismDIDAndPrivateKeys stores DID and private keys`() = runTest {
        val did = DID("did:prism:example")
        val secp256PrivateKey = Secp256k1KeyPair.generateKeyPair()
        val alias = "alias"

        pluto.start()
        pluto.storePrismDIDAndPrivateKeys(did, 1, alias, listOf(secp256PrivateKey.privateKey as StorableKey))

        val dids = pluto.getAllDIDs().first()
        assertNotNull(dids)
        assertTrue(dids.isNotEmpty())
        assertEquals(1, dids.size)
        assertEquals(did.toString(), dids.first().toString())

        val privateKeys = pluto.getAllPrivateKeys().first()
        assertNotNull(privateKeys)
        assertTrue(privateKeys.isNotEmpty())
        assertEquals(1, privateKeys.size)

        val didKeyLinks = pluto.getAllDIDKeyLinkData().first()
        assertNotNull(didKeyLinks)
        assertTrue(didKeyLinks.isNotEmpty())
        assertEquals(1, didKeyLinks.size)
    }

    @Test
    fun `test storePeerDID stores DID`() = runTest {
        val peerDID = DID("did:peer:test")
        pluto.start()
        pluto.storePeerDID(peerDID)

        val dids = pluto.getAllDIDs().first()
        assertNotNull(dids)
        assertTrue(dids.isNotEmpty())
        assertEquals(1, dids.size)
        assertEquals(peerDID.toString(), dids.first().toString())
    }

    @Test
    fun `test storeDIDPair stores a two DIDs and alias`() = runTest {
        val host = DID("did:peer:host")
        val receiver = DID("did:peer:receiver")
        val alias = "alias"
        pluto.start()
        pluto.storeDIDPair(host, receiver, alias)

        val didPairs = pluto.getAllDidPairs().first()
        assertNotNull(didPairs)
        assertTrue(didPairs.isNotEmpty())
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

        pluto.start()
        pluto.storeMessage(message)

        val messages = pluto.getAllMessages().first()
        assertNotNull(messages)
        assertTrue(messages.isNotEmpty())
        assertEquals(1, messages.size)
        val msg = messages.first()
        assertEquals(message, msg)
    }

    @Test
    fun `test storePrivateKeys stores a storableKey, DID, keyPathIndex and metaId`() = runTest {
        val secp256PrivateKey = Secp256k1KeyPair.generateKeyPair().privateKey
        val did = DID("did:peer:example")

        pluto.start()
        pluto.storePrivateKeys(
            storableKey = secp256PrivateKey as StorableKey,
            did = did,
            keyPathIndex = 0,
            metaId = ""
        )

        val privateKeys = pluto.getAllPrivateKeys().first()
        assertNotNull(privateKeys)
        assertTrue(privateKeys.isNotEmpty())
        assertEquals(1, privateKeys.size)

        val didKeyLinks = pluto.getAllDIDKeyLinkData().first()
        assertNotNull(didKeyLinks)
        assertTrue(didKeyLinks.isNotEmpty())
        assertEquals(1, didKeyLinks.size)
    }

    @Test
    fun `test storePrivate`() = runTest {
        val privateKey = Secp256k1KeyPair.generateKeyPair().privateKey

        pluto.start()
        pluto.storePrivate(privateKey as StorableKey, recoveryId = "recoveryId")

        val privateKeys = pluto.getAllPrivateKeys().first()
        assertNotNull(privateKeys)
        assertTrue(privateKeys.isNotEmpty())
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

        pluto.start()
        pluto.storeMessages(listOf(message, message1))

        val messages = pluto.getAllMessages().first()
        assertNotNull(messages)
        assertTrue(messages.isNotEmpty())
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

        pluto.start()
        pluto.storePeerDID(hostDID)
        pluto.storeMediator(mediatorDID, hostDID, routingDID)

        val dids = pluto.getAllDIDs().first()
        assertNotNull(dids)
        assertTrue(dids.isNotEmpty())
        assertEquals(3, dids.size)
        val didsString = dids.map { it.toString() }
        assertTrue(didsString.contains(mediatorDID.toString()))
        assertTrue(didsString.contains(hostDID.toString()))
        assertTrue(didsString.contains(routingDID.toString()))

        val mediators = pluto.getAllMediators().first()
        assertNotNull(mediators)
        assertTrue(mediators.isNotEmpty())
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

        pluto.start()
        pluto.storeCredential(credential.toStorableCredential())

        val credentials = pluto.getAllCredentials().first()
        assertNotNull(credentials)
        assertTrue(credentials.isNotEmpty())
        assertEquals(1, credentials.size)
        assertEquals("jwt+credential", credentials.first().restorationId)
    }

    @Test
    fun `test storeLinkSecret`() = runTest {
        val linkSecret = "linkSecret"
        pluto.start()
        pluto.storeLinkSecret(linkSecret)

        val linkSecretString = pluto.getLinkSecret().first()
        assertEquals(linkSecret, linkSecretString)
    }

    @Test
    fun `test storeCredentialMetadata`() = runTest {
        val name = "meta"
        val linkSecretName = "linkSecretName"
        val json = "{}"

        pluto.start()
        pluto.storeCredentialMetadata(name, linkSecretName, json)

        val credentialMetadata = pluto.getCredentialMetadata(linkSecretName).first()
        assertNotNull(credentialMetadata)
        assertEquals(linkSecretName, credentialMetadata?.linkSecretName)
        assertEquals(json, credentialMetadata?.json)
    }

    @Test
    fun `test getAllPrismDIDs`() = runTest {
        val prismDID = DID("did:prism:test")
        val privateKey = Secp256k1KeyPair.generateKeyPair().privateKey as StorableKey
        pluto.start()
        pluto.storePrismDIDAndPrivateKeys(prismDID, null, null, listOf(privateKey))

        val allPrismDids = pluto.getAllPrismDIDs().first()
        assertNotNull(allPrismDids)
        assertEquals(1, allPrismDids.size)
        val prismDidInfo = allPrismDids.first()
        assertEquals(prismDID.toString(), prismDidInfo.did.toString())
        assertNull(prismDidInfo.keyPathIndex)
        assertNull(prismDidInfo.alias)
    }

    @Test
    fun `test getDIDInfoByDID`() = runTest {
        val prismDID = DID("did:prism:test")
        val privateKey = Secp256k1KeyPair.generateKeyPair().privateKey as StorableKey
        pluto.start()
        pluto.storePrismDIDAndPrivateKeys(prismDID, null, null, listOf(privateKey))

        val prismDidInfo = pluto.getDIDInfoByDID(prismDID).first()
        assertNotNull(prismDidInfo)
        assertEquals(prismDID.toString(), prismDidInfo?.did.toString())
    }

    @Test
    fun `test getDIDInfoByAlias`() = runTest {
        val prismDID = DID("did:prism:test")
        val privateKey = Secp256k1KeyPair.generateKeyPair().privateKey as StorableKey
        pluto.start()
        pluto.storePrismDIDAndPrivateKeys(prismDID, null, "alias", listOf(privateKey))

        val prismDidInfo = pluto.getDIDInfoByAlias("alias").first()
        assertNotNull(prismDidInfo)
        assertEquals(1, prismDidInfo.size)
        assertEquals(prismDID.toString(), prismDidInfo.first().did.toString())
    }

    @Test
    fun `test getDIDPrivateKeysByDID`() = runTest {
        val prismDID = DID("did:prism:test")
        val privateKey = Secp256k1KeyPair.generateKeyPair().privateKey as StorableKey
        pluto.start()
        pluto.storePrismDIDAndPrivateKeys(prismDID, null, "alias", listOf(privateKey))

        val storablePrivateKey = pluto.getDIDPrivateKeysByDID(prismDID).first()
        assertNotNull(storablePrivateKey)
        assertEquals(1, storablePrivateKey.size)
        val storableKey = storablePrivateKey.first()
        assertEquals("secp256k1+priv", storableKey.restorationIdentifier)
        assertEquals(privateKey.storableData.base64UrlEncoded, storableKey.data)
    }

    @Test
    fun `test getDIDPrivateKeyByID`() = runTest {
        val prismDID = DID("did:prism:test")
        val privateKey = Secp256k1KeyPair.generateKeyPair().privateKey as StorableKey
        pluto.start()
        pluto.storePrismDIDAndPrivateKeys(prismDID, null, "alias", listOf(privateKey))

        val storablePrivateKey = pluto.getDIDPrivateKeyByID(prismDID.toString()).first()
        assertNotNull(storablePrivateKey)
        assertEquals("secp256k1+priv", storablePrivateKey?.restorationIdentifier)
        assertEquals(privateKey.storableData.base64UrlEncoded, storablePrivateKey?.data)
    }

    @Test
    fun `test getPrismDIDKeyPathIndex`() = runTest {
        val prismDID = DID("did:prism:test")
        val privateKey = Secp256k1KeyPair.generateKeyPair().privateKey as StorableKey
        pluto.start()
        pluto.storePrismDIDAndPrivateKeys(prismDID, 99, "alias", listOf(privateKey))

        val keyPathIndex = pluto.getPrismDIDKeyPathIndex(prismDID).first()
        assertNotNull(keyPathIndex)
        assertEquals(99, keyPathIndex)
    }

    @Test
    fun `test getPrismLastKeyPathIndex`() = runTest {
        val prismDID = DID("did:prism:test")
        val privateKey = Secp256k1KeyPair.generateKeyPair().privateKey as StorableKey
        pluto.start()
        pluto.storePrismDIDAndPrivateKeys(prismDID, 10, "alias", listOf(privateKey))
        pluto.storePrismDIDAndPrivateKeys(prismDID, 9, "alias", listOf(privateKey))

        val keyPathIndex = pluto.getPrismLastKeyPathIndex().first()
        assertNotNull(keyPathIndex)
        assertEquals(10, keyPathIndex)
    }

    @Test
    fun `test getAllPeerDIDs`() = runTest {
        val peerDID1 = DID("did:peer:test1")
        val privateKey1 = Secp256k1KeyPair.generateKeyPair().privateKey as StorableKey
        val peerDID2 = DID("did:peer:test2")
        val privateKey2 = Ed25519KeyPair.generateKeyPair().privateKey as StorableKey
        val peerDID3 = DID("did:peer:test3")
        val privateKey3 = X25519KeyPair.generateKeyPair().privateKey as StorableKey

        pluto.start()
        pluto.storePeerDID(peerDID1)
        pluto.storePrivateKeys(privateKey1, peerDID1, null, "$peerDID1#key-1")
        pluto.storePeerDID(peerDID2)
        pluto.storePrivateKeys(privateKey2, peerDID2, null, "$peerDID2#key-1")
        pluto.storePeerDID(peerDID3)
        pluto.storePrivateKeys(privateKey3, peerDID3, null, "$peerDID3#key-1")

        val peerDIDs = pluto.getAllPeerDIDs().first()
        assertNotNull(peerDIDs)
        assertEquals(3, peerDIDs.size)

        assertEquals(peerDID1.toString(), peerDIDs[0].did.toString())
        assertEquals(1, peerDIDs[0].privateKeys.size)
        assertContentEquals(privateKey1.storableData, peerDIDs[0].privateKeys.first().raw)

        assertEquals(peerDID2.toString(), peerDIDs[1].did.toString())
        assertEquals(1, peerDIDs[1].privateKeys.size)
        assertContentEquals(privateKey2.storableData, peerDIDs[1].privateKeys.first().raw)

        assertEquals(peerDID3.toString(), peerDIDs[2].did.toString())
        assertEquals(1, peerDIDs[2].privateKeys.size)
        assertContentEquals(privateKey3.storableData, peerDIDs[2].privateKeys.first().raw)
    }

    @Test
    fun `test getAllDIDs`() = runTest {
        val peerDID1 = DID("did:peer:test1")
        val peerDID2 = DID("did:peer:test2")
        val peerDID3 = DID("did:peer:test3")

        pluto.start()
        pluto.storePeerDID(peerDID1)
        pluto.storePeerDID(peerDID2)
        pluto.storePeerDID(peerDID3)

        val dids = pluto.getAllDIDs().first()
        assertNotNull(dids)
        assertEquals(3, dids.size)

        assertEquals(peerDID1.toString(), dids[0].toString())
        assertEquals(peerDID2.toString(), dids[1].toString())
        assertEquals(peerDID3.toString(), dids[2].toString())
    }

    @Test
    fun `test getAllDidPairs`() = runTest {
        val hostPeerDID1 = DID("did:peer:test1")
        val receiverPeerDID1 = DID("did:peer:test1")
        val name1 = "name1"
        val hostPeerDID2 = DID("did:peer:test2")
        val receiverPeerDID2 = DID("did:peer:test3")
        val name2 = "name2"

        pluto.start()
        pluto.storeDIDPair(hostPeerDID1, receiverPeerDID1, name1)
        pluto.storeDIDPair(hostPeerDID2, receiverPeerDID2, name2)

        val didPairs = pluto.getAllDidPairs().first()
        assertNotNull(didPairs)
        assertEquals(2, didPairs.size)

        assertEquals(hostPeerDID1.toString(), didPairs[0].holder.toString())
        assertEquals(receiverPeerDID1.toString(), didPairs[0].receiver.toString())
        assertEquals(name1, didPairs[0].name)

        assertEquals(hostPeerDID2.toString(), didPairs[1].holder.toString())
        assertEquals(receiverPeerDID2.toString(), didPairs[1].receiver.toString())
        assertEquals(name2, didPairs[1].name)
    }

    @Test
    fun `test getPairByDID`() = runTest {
        val hostPeerDID1 = DID("did:peer:test1")
        val receiverPeerDID1 = DID("did:peer:test1")
        val name1 = "name1"
        val hostPeerDID2 = DID("did:peer:test2")
        val receiverPeerDID2 = DID("did:peer:test3")
        val name2 = "name2"

        pluto.start()
        pluto.storeDIDPair(hostPeerDID1, receiverPeerDID1, name1)
        pluto.storeDIDPair(hostPeerDID2, receiverPeerDID2, name2)

        val didPair = pluto.getPairByDID(hostPeerDID2).first()
        assertNotNull(didPair)

        assertEquals(hostPeerDID2.toString(), didPair?.holder.toString())
        assertEquals(receiverPeerDID2.toString(), didPair?.receiver.toString())
        assertEquals(name2, didPair?.name)
    }

    @Test
    fun `test getPairByName`() = runTest {
        val hostPeerDID1 = DID("did:peer:test1")
        val receiverPeerDID1 = DID("did:peer:test1")
        val name1 = "name1"
        val hostPeerDID2 = DID("did:peer:test2")
        val receiverPeerDID2 = DID("did:peer:test3")
        val name2 = "name2"

        pluto.start()
        pluto.storeDIDPair(hostPeerDID1, receiverPeerDID1, name1)
        pluto.storeDIDPair(hostPeerDID2, receiverPeerDID2, name2)

        val didPair = pluto.getPairByName(name2).first()
        assertNotNull(didPair)

        assertEquals(hostPeerDID2.toString(), didPair?.holder.toString())
        assertEquals(receiverPeerDID2.toString(), didPair?.receiver.toString())
        assertEquals(name2, didPair?.name)
    }

    @Test
    fun `test getAllMessages`() = runTest {
        val message = Message(
            piuri = "https://didcomm.atalaprism.io/present-proof/3.0/request-presentation",
            from = DID("did:peer:from1"),
            to = DID("did:peer:to1"),
            body = "{}"
        )

        val message1 = Message(
            piuri = "https://didcomm.atalaprism.io/present-proof/3.0/request-presentation",
            from = DID("did:peer:from2"),
            to = DID("did:peer:to2"),
            body = "{}"
        )

        pluto.start()
        pluto.storeMessages(listOf(message, message1))

        val messages = pluto.getAllMessages(message.from!!, message.to!!).first()
        assertNotNull(messages)
        assertEquals(1, messages.size)

        assertEquals(message.piuri, messages[0].piuri)
        assertEquals(message.from, messages[0].from)
        assertEquals(message.to, messages[0].to)
    }

    @Test
    fun `test getAllMessagesSent`() = runTest {
        val message = Message(
            piuri = "https://didcomm.atalaprism.io/present-proof/3.0/request-presentation",
            from = DID("did:peer:from1"),
            to = DID("did:peer:to1"),
            body = "{}"
        )

        val message1 = Message(
            piuri = "https://didcomm.atalaprism.io/present-proof/3.0/request-presentation",
            from = DID("did:peer:from2"),
            to = DID("did:peer:to2"),
            body = "{}",
            direction = Message.Direction.SENT
        )

        pluto.start()
        pluto.storeMessages(listOf(message, message1))

        val messages = pluto.getAllMessagesSent().first()
        assertNotNull(messages)
        assertEquals(1, messages.size)

        assertEquals(message1.piuri, messages[0].piuri)
        assertEquals(message1.from, messages[0].from)
        assertEquals(message1.to, messages[0].to)

        val messages1 = pluto.getAllMessagesReceived().first()
        assertNotNull(messages1)
        assertEquals(1, messages1.size)

        assertEquals(message.piuri, messages1[0].piuri)
        assertEquals(message.from, messages1[0].from)
        assertEquals(message.to, messages1[0].to)
    }

    @Test
    fun `test getAllMessagesSentTo`() = runTest {
        val message = Message(
            piuri = "https://didcomm.atalaprism.io/present-proof/3.0/request-presentation",
            from = DID("did:peer:from1"),
            to = DID("did:peer:to1"),
            body = "{}"
        )

        val message1 = Message(
            piuri = "https://didcomm.atalaprism.io/present-proof/3.0/request-presentation",
            from = DID("did:peer:from2"),
            to = DID("did:peer:to2"),
            body = "{}",
            direction = Message.Direction.SENT
        )

        pluto.start()
        pluto.storeMessages(listOf(message, message1))

        val messages = pluto.getAllMessagesSentTo(message1.to!!).first()
        assertNotNull(messages)
        assertEquals(1, messages.size)

        assertEquals(message1.piuri, messages[0].piuri)
        assertEquals(message1.from, messages[0].from)
        assertEquals(message1.to, messages[0].to!!)
    }

    @Test
    fun `test getAllMessagesReceivedFrom`() = runTest {
        val message = Message(
            piuri = "https://didcomm.atalaprism.io/present-proof/3.0/request-presentation",
            from = DID("did:peer:from1"),
            to = DID("did:peer:to1"),
            body = "{}"
        )

        val message1 = Message(
            piuri = "https://didcomm.atalaprism.io/present-proof/3.0/request-presentation",
            from = DID("did:peer:from2"),
            to = DID("did:peer:to2"),
            body = "{}",
            direction = Message.Direction.SENT
        )

        pluto.start()
        pluto.storeMessages(listOf(message, message1))

        val messages = pluto.getAllMessagesSentTo(message.to!!).first()
        assertNotNull(messages)
        assertEquals(1, messages.size)

        assertEquals(message.piuri, messages[0].piuri)
        assertEquals(message.from, messages[0].from!!)
        assertEquals(message.to, messages[0].to!!)
    }

    @Test
    fun `test getMessageByThidAndPiuri`() = runTest {
        val message = Message(
            piuri = "https://didcomm.atalaprism.io/present-proof/3.0/request-presentation",
            from = DID("did:peer:from1"),
            to = DID("did:peer:to1"),
            body = "{}"
        )

        val message1 = Message(
            piuri = "https://didcomm.atalaprism.io/present-proof/3.0/request-presentation",
            from = DID("did:peer:from2"),
            to = DID("did:peer:to2"),
            body = "{}",
            direction = Message.Direction.SENT,
            thid = UUID.randomUUID().toString()
        )

        pluto.start()
        pluto.storeMessages(listOf(message, message1))

        val message2 = pluto.getMessageByThidAndPiuri(message1.thid!!, piuri = message1.piuri).first()
        assertNotNull(message2)

        assertEquals(message1.piuri, message2?.piuri)
        assertEquals(message1.from, message2?.from)
        assertEquals(message1.to, message2?.to)
    }

    @Test
    fun `test revokeCredential`() = runTest {
        val credential = JWTCredential.fromJwtString(
            "eyJhbGciOiJFUzI1NksifQ.eyJpc3MiOiJkaWQ6cHJpc206MjU3MTlhOTZiMTUxMjA3MTY5ODFhODQzMGFkMGNiOTY4ZGQ1MzQwNzM1OTNjOGNkM2YxZDI3YTY4MDRlYzUwZTpDcG9DQ3BjQ0Vsb0tCV3RsZVMweEVBSkNUd29KYzJWamNESTFObXN4RWlBRW9TQ241dHlEYTZZNnItSW1TcXBKOFkxbWo3SkMzX29VekUwTnl5RWlDQm9nc2dOYWVSZGNDUkdQbGU4MlZ2OXRKZk53bDZyZzZWY2hSM09xaGlWYlRhOFNXd29HWVhWMGFDMHhFQVJDVHdvSmMyVmpjREkxTm1zeEVpRE1rQmQ2RnRpb0prM1hPRnUtX2N5NVhtUi00dFVRMk5MR2lXOGFJU29ta1JvZzZTZGU5UHduRzBRMFNCVG1GU1REYlNLQnZJVjZDVExYcmpJSnR0ZUdJbUFTWEFvSGJXRnpkR1Z5TUJBQlFrOEtDWE5sWTNBeU5UWnJNUklnTzcxMG10MVdfaXhEeVFNM3hJczdUcGpMQ05PRFF4Z1ZoeDVzaGZLTlgxb2FJSFdQcnc3SVVLbGZpYlF0eDZKazRUU2pnY1dOT2ZjT3RVOUQ5UHVaN1Q5dCIsInN1YiI6ImRpZDpwcmlzbTpiZWVhNTIzNGFmNDY4MDQ3MTRkOGVhOGVjNzdiNjZjYzdmM2U4MTVjNjhhYmI0NzVmMjU0Y2Y5YzMwNjI2NzYzOkNzY0JDc1FCRW1RS0QyRjFkR2hsYm5ScFkyRjBhVzl1TUJBRVFrOEtDWE5sWTNBeU5UWnJNUklnZVNnLTJPTzFKZG5welVPQml0eklpY1hkZnplQWNUZldBTi1ZQ2V1Q2J5SWFJSlE0R1RJMzB0YVZpd2NoVDNlMG5MWEJTNDNCNGo5amxzbEtvMlpsZFh6akVsd0tCMjFoYzNSbGNqQVFBVUpQQ2dselpXTndNalUyYXpFU0lIa29QdGpqdFNYWjZjMURnWXJjeUluRjNYODNnSEUzMWdEZm1BbnJnbThpR2lDVU9Ca3lOOUxXbFlzSElVOTN0Snkxd1V1TndlSV9ZNWJKU3FObVpYVjg0dyIsIm5iZiI6MTY4NTYzMTk5NSwiZXhwIjoxNjg1NjM1NTk1LCJ2YyI6eyJjcmVkZW50aWFsU3ViamVjdCI6eyJhZGRpdGlvbmFsUHJvcDIiOiJUZXN0MyIsImlkIjoiZGlkOnByaXNtOmJlZWE1MjM0YWY0NjgwNDcxNGQ4ZWE4ZWM3N2I2NmNjN2YzZTgxNWM2OGFiYjQ3NWYyNTRjZjljMzA2MjY3NjM6Q3NjQkNzUUJFbVFLRDJGMWRHaGxiblJwWTJGMGFXOXVNQkFFUWs4S0NYTmxZM0F5TlRack1SSWdlU2ctMk9PMUpkbnB6VU9CaXR6SWljWGRmemVBY1RmV0FOLVlDZXVDYnlJYUlKUTRHVEkzMHRhVml3Y2hUM2UwbkxYQlM0M0I0ajlqbHNsS28yWmxkWHpqRWx3S0IyMWhjM1JsY2pBUUFVSlBDZ2x6WldOd01qVTJhekVTSUhrb1B0amp0U1haNmMxRGdZcmN5SW5GM1g4M2dIRTMxZ0RmbUFucmdtOGlHaUNVT0JreU45TFdsWXNISVU5M3RKeTF3VXVOd2VJX1k1YkpTcU5tWlhWODR3In0sInR5cGUiOlsiVmVyaWZpYWJsZUNyZWRlbnRpYWwiXSwiQGNvbnRleHQiOlsiaHR0cHM6XC9cL3d3dy53My5vcmdcLzIwMThcL2NyZWRlbnRpYWxzXC92MSJdfX0.x0SF17Y0VCDmt7HceOdTxfHlofsZmY18Rn6VQb0-r-k_Bm3hTi1-k2vkdjB25hdxyTCvxam-AkAP-Ag3Ahn5Ng"
        )

        pluto.start()
        pluto.storeCredential(credential.toStorableCredential())

        val observeRevoked = pluto.observeRevokedCredentials()

        pluto.revokeCredential(credential.id)
        val credentials = pluto.getAllCredentials().first()
        assertNotNull(credentials)
        val cred = credentials.first()
        assertTrue(cred.revoked)
        assertEquals(1, observeRevoked.first().size)
    }
}
