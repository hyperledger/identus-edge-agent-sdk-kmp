@file:Suppress("ktlint:standard:import-ordering")

package org.hyperledger.identus.walletsdk.pollux

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.ECDSASigner
import com.nimbusds.jose.crypto.bc.BouncyCastleProviderSingleton
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import java.text.SimpleDateFormat
import java.util.*
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.didcommx.didcomm.common.Typ
import org.hyperledger.identus.apollo.base64.base64UrlDecoded
import org.hyperledger.identus.apollo.derivation.MnemonicHelper
import org.hyperledger.identus.walletsdk.apollo.ApolloImpl
import org.hyperledger.identus.walletsdk.apollo.utils.Ed25519KeyPair
import org.hyperledger.identus.walletsdk.apollo.utils.Secp256k1KeyPair
import org.hyperledger.identus.walletsdk.castor.CastorImpl
import org.hyperledger.identus.walletsdk.domain.buildingblocks.Apollo
import org.hyperledger.identus.walletsdk.domain.buildingblocks.Castor
import org.hyperledger.identus.walletsdk.domain.models.AnoncredsInputFieldFilter
import org.hyperledger.identus.walletsdk.domain.models.AnoncredsPresentationClaims
import org.hyperledger.identus.walletsdk.domain.models.Api
import org.hyperledger.identus.walletsdk.domain.models.ApiImpl
import org.hyperledger.identus.walletsdk.domain.models.CredentialOperationsOptions
import org.hyperledger.identus.walletsdk.domain.models.CredentialType
import org.hyperledger.identus.walletsdk.domain.models.Curve
import org.hyperledger.identus.walletsdk.domain.models.DID
import org.hyperledger.identus.walletsdk.domain.models.HttpResponse
import org.hyperledger.identus.walletsdk.domain.models.InputFieldFilter
import org.hyperledger.identus.walletsdk.domain.models.JWTPresentationClaims
import org.hyperledger.identus.walletsdk.domain.models.JWTVerifiableCredential
import org.hyperledger.identus.walletsdk.domain.models.KeyCurve
import org.hyperledger.identus.walletsdk.domain.models.KeyValue
import org.hyperledger.identus.walletsdk.domain.models.Message
import org.hyperledger.identus.walletsdk.domain.models.PolluxError
import org.hyperledger.identus.walletsdk.domain.models.PresentationClaims
import org.hyperledger.identus.walletsdk.domain.models.RequestedAttributes
import org.hyperledger.identus.walletsdk.domain.models.SDJWTPresentationClaims
import org.hyperledger.identus.walletsdk.domain.models.Seed
import org.hyperledger.identus.walletsdk.domain.models.httpClient
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.PrivateKey
import org.hyperledger.identus.walletsdk.edgeagent.protocols.proofOfPresentation.AnoncredsPresentationOptions
import org.hyperledger.identus.walletsdk.edgeagent.protocols.proofOfPresentation.JWTPresentationOptions
import org.hyperledger.identus.walletsdk.edgeagent.protocols.proofOfPresentation.PresentationSubmissionOptionsJWT
import org.hyperledger.identus.walletsdk.edgeagent.protocols.proofOfPresentation.PresentationSubmissionOptionsSDJWT
import org.hyperledger.identus.walletsdk.edgeagent.protocols.proofOfPresentation.SDJWTPresentationOptions
import org.hyperledger.identus.walletsdk.logger.Logger
import org.hyperledger.identus.walletsdk.pollux.models.AnonCredential
import org.hyperledger.identus.walletsdk.pollux.models.AnoncredsPresentationDefinitionRequest
import org.hyperledger.identus.walletsdk.pollux.models.JWTCredential
import org.hyperledger.identus.walletsdk.pollux.models.JWTPresentationDefinitionRequest
import org.hyperledger.identus.walletsdk.pollux.models.PresentationSubmission
import org.hyperledger.identus.walletsdk.pollux.models.SDJWTPresentationDefinitionRequest
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class PolluxImplTest {

    lateinit var pollux: PolluxImpl
    lateinit var apollo: Apollo
    lateinit var castor: Castor
    lateinit var api: Api

    @Mock
    lateinit var castorMock: Castor

    @Mock
    lateinit var loggerMock: Logger

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        apollo = ApolloImpl()

        castor = CastorImpl(apollo, loggerMock)

        doNothing()
            .`when`(loggerMock).debug(anyString(), any())

        api = spy(
            ApiImpl(
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
            )
        )
    }

//    @Test
//    fun testGetSchema_whenAnoncred_thenSchemaCorrect() = runTest {
//        val schema = pollux.getSchema("")
//        val attrNames = listOf("name", "surname")
//        assertEquals("Schema name", schema.name)
//        assertEquals("1.1", schema.version)
//        assertEquals(attrNames, schema.attrNames)
//        assertEquals(
//            "did:prism:604ba1764ab89993f9a74625cc4f3e04737919639293eb382cc7adc53767f550",
//            schema.issuerId
//        )
//    }

//    @Test
//    fun testGetSchema_whenAnoncred_thenSchemaCorrect() = runTest {
//        val response =
//            "{\"name\":\"Schema name\",\"version\":\"1.1\",\"attrNames\":[\"name\",\"surname\"],\"issuerId\":\"did:prism:604ba1764ab89993f9a74625cc4f3e04737919639293eb382cc7adc53767f550\"}"
//        val httpResponse = HttpResponse(status = HttpStatusCode.OK.value, response)
//
//        doReturn(httpResponse)
//            .`when`(api).request(
//                httpMethod = HttpMethod.Get.value,
//                url = "",
//                urlParameters = emptyArray(),
//                httpHeaders = arrayOf(KeyValue(HttpHeaders.ContentType, Typ.Encrypted.typ)),
//                body = null
//            )
//        pollux = PolluxImpl(apollo, castor, api)
//
//        val schema = pollux.getSchema("")
//        val attrNames = listOf("name", "surname")
//        assertEquals("Schema name", schema.name)
//        assertEquals("1.1", schema.version)
//        assertEquals(attrNames, schema.attrNames)
//        assertEquals(
//            "did:prism:604ba1764ab89993f9a74625cc4f3e04737919639293eb382cc7adc53767f550",
//            schema.issuerId
//        )
//    }

    @Test
    fun testCreatePresentationDefinitionRequest_whenOptionsNoJWT_thenExceptionThrown() = runTest {
        pollux = PolluxImpl(apollo, castor, api)
        assertFailsWith(PolluxError.InvalidJWTPresentationDefinitionError::class) {
            pollux.createPresentationDefinitionRequest(
                type = CredentialType.JWT,
                presentationClaims = JWTPresentationClaims(
                    claims = mapOf()
                ),
                options = JWTPresentationOptions(jwt = emptyArray(), domain = "", challenge = "")
            )
        }
    }

    @Test
    fun testCreatePresentationDefinitionRequest_whenJWT_thenPresentationDefinitionRequestCorrect() =
        runTest {
            pollux = PolluxImpl(apollo, castor, api)
            val definitionRequestString = pollux.createPresentationDefinitionRequest(
                type = CredentialType.JWT,
                presentationClaims = JWTPresentationClaims(
                    claims = mapOf(
                        "$.vc.credentialSubject.email" to InputFieldFilter(
                            type = "string",
                            value = "value"
                        )
                    )
                ),
                options = JWTPresentationOptions(
                    name = "Testing",
                    purpose = "Test presentation definition",
                    jwt = arrayOf("EcdsaSecp256k1Signature2019"),
                    domain = "domain",
                    challenge = "challenge"
                )
            )

            val definitionRequest = Json.decodeFromString<JWTPresentationDefinitionRequest>(definitionRequestString)
            assertEquals(1, definitionRequest.presentationDefinition.inputDescriptors.size)
            assertEquals(
                1,
                definitionRequest.presentationDefinition.inputDescriptors.first().constraints.fields?.size
            )
            assertEquals(
                2,
                definitionRequest.presentationDefinition.inputDescriptors.first().constraints.fields?.first()?.path?.size
            )
            assertEquals(
                "Testing",
                definitionRequest.presentationDefinition.inputDescriptors.first().name
            )
            assertEquals(
                "Test presentation definition",
                definitionRequest.presentationDefinition.inputDescriptors.first().purpose
            )
        }

    @Test
    fun testCreatePresentationSubmission_whenAllCorrect_thenPresentationSubmissionProofWellFormed() =
        runTest {
            val loggerMock = mock<Logger>()
            val castor: Castor = CastorImpl(apollo = ApolloImpl(), loggerMock)

            val issuerKeyPair =
                Secp256k1KeyPair.generateKeyPair(
                    Seed(MnemonicHelper.createRandomSeed()),
                    KeyCurve(Curve.SECP256K1)
                )
            val holderKeyPair =
                Secp256k1KeyPair.generateKeyPair(
                    Seed(MnemonicHelper.createRandomSeed()),
                    KeyCurve(Curve.SECP256K1)
                )
            val issuerDID = castor.createPrismDID(issuerKeyPair.publicKey, emptyArray())
            val holderDID = castor.createPrismDID(holderKeyPair.publicKey, emptyArray())

            pollux = spy(PolluxImpl(apollo, castor, api))

            val vtc = createVerificationTestCase(
                VerificationTestCase(
                    issuer = issuerDID,
                    holder = holderDID,
                    issuerPrv = issuerKeyPair.privateKey,
                    holderPrv = holderKeyPair.privateKey,
                    subject = """{"course": "Identus Training course Certification 2024"} """,
                    claims = JWTPresentationClaims(
                        claims = mapOf(
                            "course" to InputFieldFilter(
                                type = "string",
                                pattern = "Identus Training course Certification 2024"
                            )
                        )
                    ),
                )
            )
            val presentationDefinitionRequest = Json.decodeFromString<JWTPresentationDefinitionRequest>(vtc.first)
            val presentationSubmissionProof = Json.decodeFromString<PresentationSubmission>(vtc.second)

            assertEquals(
                presentationDefinitionRequest.presentationDefinition.id,
                presentationSubmissionProof.presentationSubmission.definitionId
            )
            assertEquals(1, presentationSubmissionProof.presentationSubmission.descriptorMap.size)
            val inputDescriptor =
                presentationDefinitionRequest.presentationDefinition.inputDescriptors.first()
            val descriptorMap =
                presentationSubmissionProof.presentationSubmission.descriptorMap.first()
            assertEquals(inputDescriptor.id, descriptorMap.id)
            assertEquals("$.verifiablePresentation[0]", descriptorMap.path)
            assertEquals(1, presentationSubmissionProof.verifiablePresentation.size)
        }

    @Test
    fun testVerifyPresentationSubmission_whenWrongJwtIssuer_thenVerifiedFalse() = runTest {
        val issuerKeyPair =
            Secp256k1KeyPair.generateKeyPair(
                Seed(MnemonicHelper.createRandomSeed()),
                KeyCurve(Curve.SECP256K1)
            )
        val wrongIssuerKeyPair =
            Secp256k1KeyPair.generateKeyPair(
                Seed(MnemonicHelper.createRandomSeed()),
                KeyCurve(Curve.SECP256K1)
            )
        val holderKeyPair =
            Secp256k1KeyPair.generateKeyPair(
                Seed(MnemonicHelper.createRandomSeed()),
                KeyCurve(Curve.SECP256K1)
            )
        val issuerDID = castor.createPrismDID(wrongIssuerKeyPair.publicKey, emptyArray())
        val holderDID = castor.createPrismDID(holderKeyPair.publicKey, emptyArray())

        val httpResponse = correctHttpResponseFetchRevocationRegistry()

        doReturn(httpResponse)
            .`when`(api).request(
                HttpMethod.Get.value,
                "http://10.91.100.126:8000/prism-agent/credential-status/514e8528-4b38-477a-b0e4-324bbe220464",
                emptyArray(),
                arrayOf(KeyValue(HttpHeaders.ContentType, Typ.Encrypted.typ)),
                null
            )

        pollux = spy(PolluxImpl(apollo, castor, api))

        val vtc = createVerificationTestCase(
            VerificationTestCase(
                issuer = issuerDID,
                holder = holderDID,
                issuerPrv = issuerKeyPair.privateKey,
                holderPrv = holderKeyPair.privateKey,
                subject = """{"course": "Identus Training course Certification 2024"} """,
                claims = JWTPresentationClaims(
                    claims = mapOf(
                        "course" to InputFieldFilter(
                            type = "string",
                            pattern = "Identus Training course Certification 2024"
                        )
                    )
                ),
            )
        )
        assertFailsWith(PolluxError.VerificationUnsuccessful::class, "Issuer signature not valid") {
            pollux.verifyPresentationSubmission(
                presentationSubmissionString = vtc.second,
                options = PresentationSubmissionOptionsJWT(vtc.first)
            )
        }
    }

    @Test
    fun testVerifyPresentationSubmission_whenJwtSignaturesOkAndFieldsNot_thenVerifiedFalse() =
        runTest {
            val issuerKeyPair =
                Secp256k1KeyPair.generateKeyPair(
                    Seed(MnemonicHelper.createRandomSeed()),
                    KeyCurve(Curve.SECP256K1)
                )
            val holderKeyPair =
                Secp256k1KeyPair.generateKeyPair(
                    Seed(MnemonicHelper.createRandomSeed()),
                    KeyCurve(Curve.SECP256K1)
                )
            val issuerDID = castor.createPrismDID(issuerKeyPair.publicKey, emptyArray())
            val holderDID = castor.createPrismDID(holderKeyPair.publicKey, emptyArray())

            val httpResponse = correctHttpResponseFetchRevocationRegistry()

            doReturn(httpResponse)
                .`when`(api).request(
                    HttpMethod.Get.value,
                    "http://10.91.100.126:8000/prism-agent/credential-status/514e8528-4b38-477a-b0e4-324bbe220464",
                    emptyArray(),
                    arrayOf(KeyValue(HttpHeaders.ContentType, Typ.Encrypted.typ)),
                    null
                )

            pollux = spy(PolluxImpl(apollo, castor, api))

            val vtc = createVerificationTestCase(
                VerificationTestCase(
                    issuer = issuerDID,
                    holder = holderDID,
                    issuerPrv = issuerKeyPair.privateKey,
                    holderPrv = holderKeyPair.privateKey,
                    subject = """{"course": "Identus Training course Certification 2023"} """,
                    claims = JWTPresentationClaims(
                        claims = mapOf(
                            "course" to InputFieldFilter(
                                type = "string",
                                pattern = "Identus Training course Certification 2024"
                            )
                        )
                    ),
                )
            )
            assertFailsWith(
                PolluxError.VerificationUnsuccessful::class,
                "Identus Training course Certification 2023"
            ) {
                pollux.verifyPresentationSubmission(
                    presentationSubmissionString = vtc.second,
                    options = PresentationSubmissionOptionsJWT(vtc.first)
                )
            }
        }

    @Test
    fun testVerifyPresentationSubmission_whenJwtSignaturesAndFieldsOk_thenVerifiedOk() = runTest {
        val issuerKeyPair =
            Secp256k1KeyPair.generateKeyPair(
                Seed(MnemonicHelper.createRandomSeed()),
                KeyCurve(Curve.SECP256K1)
            )
        val holderKeyPair =
            Secp256k1KeyPair.generateKeyPair(
                Seed(MnemonicHelper.createRandomSeed()),
                KeyCurve(Curve.SECP256K1)
            )
        val issuerDID = castor.createPrismDID(issuerKeyPair.publicKey, emptyArray())
        val holderDID = castor.createPrismDID(holderKeyPair.publicKey, emptyArray())

        val httpResponse = correctHttpResponseFetchRevocationRegistry()

        doReturn(httpResponse)
            .`when`(api).request(
                HttpMethod.Get.value,
                "http://10.91.100.126:8000/prism-agent/credential-status/514e8528-4b38-477a-b0e4-324bbe220464",
                emptyArray(),
                arrayOf(KeyValue(HttpHeaders.ContentType, Typ.Encrypted.typ)),
                null
            )

        pollux = spy(PolluxImpl(apollo, castor, api))

        val vtc = createVerificationTestCase(
            VerificationTestCase(
                issuer = issuerDID,
                holder = holderDID,
                issuerPrv = issuerKeyPair.privateKey,
                holderPrv = holderKeyPair.privateKey,
                subject = """{"course": "Identus Training course Certification 2024"} """,
                claims = JWTPresentationClaims(
                    claims = mapOf(
                        "course" to InputFieldFilter(
                            type = "string",
                            pattern = "Identus Training course Certification 2024"
                        )
                    )
                ),
            )
        )

        val isVerified = pollux.verifyPresentationSubmission(
            presentationSubmissionString = vtc.second,
            options = PresentationSubmissionOptionsJWT(vtc.first)
        )
        assertTrue(isVerified)
    }

    @Test
    fun testDescriptorPath_whenGetValue_thenArrayIndexValueAsString() = runTest {
        val issuerKeyPair =
            Secp256k1KeyPair.generateKeyPair(
                Seed(MnemonicHelper.createRandomSeed()),
                KeyCurve(Curve.SECP256K1)
            )
        val holderKeyPair =
            Secp256k1KeyPair.generateKeyPair(
                Seed(MnemonicHelper.createRandomSeed()),
                KeyCurve(Curve.SECP256K1)
            )
        val issuerDID = castor.createPrismDID(issuerKeyPair.publicKey, emptyArray())
        val holderDID = castor.createPrismDID(holderKeyPair.publicKey, emptyArray())

        pollux = spy(PolluxImpl(apollo, castor, api))

        val vtc = createVerificationTestCase(
            VerificationTestCase(
                issuer = issuerDID,
                holder = holderDID,
                issuerPrv = issuerKeyPair.privateKey,
                holderPrv = holderKeyPair.privateKey,
                subject = """{"course": "Identus Training course Certification 2024"} """,
                claims = JWTPresentationClaims(
                    claims = mapOf(
                        "course" to InputFieldFilter(
                            type = "string",
                            pattern = "Identus Training course Certification 2024"
                        )
                    )
                ),
            )
        )
        val presentationSubmission = vtc.second

        val descriptorPath =
            DescriptorPath(Json.encodeToJsonElement(Json.decodeFromString<PresentationSubmission>(presentationSubmission)))
        val path = "\$.verifiablePresentation[0]"
        val holderJws = descriptorPath.getValue(path)
        assertNotNull(holderJws)
        assertTrue(holderJws is String)
        val path1 = "\$.verifiablePresentation"
        val holderJws1 = descriptorPath.getValue(path1)
        assertNotNull(holderJws1)
    }

    @Test
    fun testDescriptorPath_whenClaimsAreEnum_thenValidatedOk() = runTest {
        val issuerKeyPair =
            Secp256k1KeyPair.generateKeyPair(
                Seed(MnemonicHelper.createRandomSeed()),
                KeyCurve(Curve.SECP256K1)
            )
        val holderKeyPair =
            Secp256k1KeyPair.generateKeyPair(
                Seed(MnemonicHelper.createRandomSeed()),
                KeyCurve(Curve.SECP256K1)
            )
        val issuerDID = castor.createPrismDID(issuerKeyPair.publicKey, emptyArray())
        val holderDID = castor.createPrismDID(holderKeyPair.publicKey, emptyArray())

        val httpResponse = correctHttpResponseFetchRevocationRegistry()

        doReturn(httpResponse)
            .`when`(api).request(
                HttpMethod.Get.value,
                "http://10.91.100.126:8000/prism-agent/credential-status/514e8528-4b38-477a-b0e4-324bbe220464",
                emptyArray(),
                arrayOf(KeyValue(HttpHeaders.ContentType, Typ.Encrypted.typ)),
                null
            )

        pollux = spy(PolluxImpl(apollo, castor, api))

        val vtc = createVerificationTestCase(
            VerificationTestCase(
                issuer = issuerDID,
                holder = holderDID,
                issuerPrv = issuerKeyPair.privateKey,
                holderPrv = holderKeyPair.privateKey,
                subject = """{"course": "Identus Training course Certification 2024"} """,
                claims = JWTPresentationClaims(
                    claims = mapOf(
                        "course" to InputFieldFilter(
                            type = "string",
                            enum = listOf("test", "Identus Training course Certification 2024")
                        )
                    )
                ),
            )
        )

        val isVerified = pollux.verifyPresentationSubmission(
            presentationSubmissionString = vtc.second,
            options = PresentationSubmissionOptionsJWT(vtc.first)
        )
        assertTrue(isVerified)
    }

    @Test
    fun testDescriptorPath_whenClaimsAreConst_thenValidatedOk() = runTest {
        val issuerKeyPair =
            Secp256k1KeyPair.generateKeyPair(
                Seed(MnemonicHelper.createRandomSeed()),
                KeyCurve(Curve.SECP256K1)
            )
        val holderKeyPair =
            Secp256k1KeyPair.generateKeyPair(
                Seed(MnemonicHelper.createRandomSeed()),
                KeyCurve(Curve.SECP256K1)
            )
        val issuerDID = castor.createPrismDID(issuerKeyPair.publicKey, emptyArray())
        val holderDID = castor.createPrismDID(holderKeyPair.publicKey, emptyArray())

        val httpResponse = correctHttpResponseFetchRevocationRegistry()

        doReturn(httpResponse)
            .`when`(api).request(
                HttpMethod.Get.value,
                "http://10.91.100.126:8000/prism-agent/credential-status/514e8528-4b38-477a-b0e4-324bbe220464",
                emptyArray(),
                arrayOf(KeyValue(HttpHeaders.ContentType, Typ.Encrypted.typ)),
                null
            )

        pollux = spy(PolluxImpl(apollo, castor, api))

        val vtc = createVerificationTestCase(
            VerificationTestCase(
                issuer = issuerDID,
                holder = holderDID,
                issuerPrv = issuerKeyPair.privateKey,
                holderPrv = holderKeyPair.privateKey,
                subject = """{"course": "Identus Training course Certification 2024"} """,
                claims = JWTPresentationClaims(
                    claims = mapOf(
                        "course" to InputFieldFilter(
                            type = "string",
                            const = listOf("test", "Identus Training course Certification 2024")
                        )
                    )
                ),
            )
        )

        val isVerified = pollux.verifyPresentationSubmission(
            presentationSubmissionString = vtc.second,
            options = PresentationSubmissionOptionsJWT(vtc.first)
        )
        assertTrue(isVerified)
    }

    @Test
    fun testDescriptorPath_whenClaimsAreValue_thenValidatedOk() = runTest {
        val httpResponse = correctHttpResponseFetchRevocationRegistry()

        doReturn(httpResponse)
            .`when`(api).request(
                HttpMethod.Get.value,
                "http://10.91.100.126:8000/prism-agent/credential-status/514e8528-4b38-477a-b0e4-324bbe220464",
                emptyArray(),
                arrayOf(KeyValue(HttpHeaders.ContentType, Typ.Encrypted.typ)),
                null
            )

        val issuerKeyPair =
            Secp256k1KeyPair.generateKeyPair(
                Seed(MnemonicHelper.createRandomSeed()),
                KeyCurve(Curve.SECP256K1)
            )
        val holderKeyPair =
            Secp256k1KeyPair.generateKeyPair(
                Seed(MnemonicHelper.createRandomSeed()),
                KeyCurve(Curve.SECP256K1)
            )
        val issuerDID = castor.createPrismDID(issuerKeyPair.publicKey, emptyArray())
        val holderDID = castor.createPrismDID(holderKeyPair.publicKey, emptyArray())

        pollux = spy(PolluxImpl(apollo, castor, api))

        val vtc = createVerificationTestCase(
            VerificationTestCase(
                issuer = issuerDID,
                holder = holderDID,
                issuerPrv = issuerKeyPair.privateKey,
                holderPrv = holderKeyPair.privateKey,
                subject = """{"course": "Identus Training course Certification 2024"} """,
                claims = JWTPresentationClaims(
                    claims = mapOf(
                        "course" to InputFieldFilter(
                            type = "string",
                            value = "Identus Training course Certification 2024"
                        )
                    )
                ),
            )
        )

        val isVerified = pollux.verifyPresentationSubmission(
            presentationSubmissionString = vtc.second,
            options = PresentationSubmissionOptionsJWT(vtc.first)
        )
        assertTrue(isVerified)
    }

    @Test
    fun testCreatePresentationDefinitionRequest_whenAnoncreds_thenPresentationDefinitionRequestCorrect() = runTest {
        val nonce = "asdf1234"

        pollux = PolluxImpl(apollo, castorMock, api)
        val definitionRequestString = pollux.createPresentationDefinitionRequest(
            type = CredentialType.ANONCREDS_PROOF_REQUEST,
            presentationClaims = AnoncredsPresentationClaims(
                predicates = mapOf(
                    "age" to AnoncredsInputFieldFilter(
                        type = "string",
                        name = "age",
                        gte = 18
                    ),
                    "income" to AnoncredsInputFieldFilter(
                        type = "string",
                        name = "income",
                        lt = 99000
                    )
                ),
                attributes = mapOf(
                    "name" to RequestedAttributes(
                        name = "name",
                        names = setOf(),
                        emptyMap(),
                        null
                    )
                )
            ),
            options = AnoncredsPresentationOptions(
                nonce = nonce
            )
        )

        val definitionRequest = Json.decodeFromString<AnoncredsPresentationDefinitionRequest>(definitionRequestString)

        assertEquals("anoncreds_presentation_request", definitionRequest.name)
        assertEquals(nonce, definitionRequest.nonce)
        assertEquals(1, definitionRequest.requestedAttributes.size)
        assertEquals(2, definitionRequest.requestedPredicates.size)
        val predicates = definitionRequest.requestedPredicates
        assertEquals(">=", predicates["age"]!!.pType)
        assertEquals(18, predicates["age"]!!.pValue)
        assertEquals("<", predicates["income"]!!.pType)
        assertEquals(99000, predicates["income"]!!.pValue)
    }

    @Test
    fun testCreatePresentationDefinitionRequest_whenAnoncredsNoOptions_thenThrowException() = runTest {
        pollux = PolluxImpl(apollo, castorMock, api)
        assertFailsWith(PolluxError.PresentationDefinitionRequestError::class) {
            pollux.createPresentationDefinitionRequest(
                type = CredentialType.ANONCREDS_PROOF_REQUEST,
                presentationClaims = AnoncredsPresentationClaims(
                    predicates = mapOf(
                        "age" to AnoncredsInputFieldFilter(
                            type = "string",
                            name = "age",
                            gte = "18"
                        ),
                        "income" to AnoncredsInputFieldFilter(
                            type = "string",
                            name = "income",
                            lt = "99000"
                        )
                    ),
                    attributes = mapOf(
                        "name" to RequestedAttributes(
                            name = "name",
                            names = setOf(),
                            emptyMap(),
                            null
                        )
                    )
                ),
                options = JWTPresentationOptions(domain = "domain", challenge = "challenge")
            )
        }
        val nonce = "asdf1234"
        assertFailsWith(PolluxError.PresentationDefinitionRequestError::class) {
            pollux.createPresentationDefinitionRequest(
                type = CredentialType.ANONCREDS_PROOF_REQUEST,
                presentationClaims = JWTPresentationClaims(
                    claims = mapOf(
                        "$.vc.credentialSubject.email" to InputFieldFilter(
                            type = "string",
                            value = "value"
                        )
                    )
                ),
                options = AnoncredsPresentationOptions(
                    nonce = nonce
                )
            )
        }
    }

    private fun generateSecp256k1KeyPair(): Secp256k1KeyPair {
        val mnemonics = listOf(
            "blade",
            "multiply",
            "coil",
            "rare",
            "fox",
            "doll",
            "tongue",
            "please",
            "icon",
            "mind",
            "gesture",
            "moral",
            "old",
            "laugh",
            "symptom",
            "assume",
            "burden",
            "appear",
            "always",
            "oil",
            "ticket",
            "vault",
            "return",
            "height"
        )
        val seed = Seed(MnemonicHelper.createSeed(mnemonics = mnemonics, passphrase = "mnemonic"))
        return Secp256k1KeyPair.generateKeyPair(seed, KeyCurve(Curve.SECP256K1))
    }

    @Test
    fun testIsCredentialRevoked_whenNotRevoked_thenCorrect() = runTest {
        val httpResponse = correctHttpResponseFetchRevocationRegistry()

        apollo = ApolloImpl()
        val api = spy(
            ApiImpl(
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
            )
        )
        doReturn(httpResponse)
            .`when`(api).request(
                HttpMethod.Get.value,
                "http://192.168.68.113:8000/cloud-agent/credential-status/6c67b6c0-1bd9-47a1-85ee-f88edaa5a894",
                emptyArray(),
                arrayOf(KeyValue(HttpHeaders.ContentType, Typ.Encrypted.typ)),
                null
            )

        pollux = PolluxImpl(apollo, castorMock, api)

        val credential = JWTCredential.fromJwtString(
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NksifQ.eyJpc3MiOiJkaWQ6cHJpc206ZjNhNzYyYjFmOTc0MWY4Mjc5MWZiZGFmZDBlYTJiYmM3Yjg3MjBmY2M0ZmJhNjZmOWZhYzhmODk2ZmFjMzVkOTpDcmtCQ3JZQkVqb0tCbUYxZEdndE1SQUVTaTRLQ1hObFkzQXlOVFpyTVJJaEF0WTAxTHY2TFpnN1VoVmpuVDZuM09CUU5qZzQwSmRhdi0xRkJjaE0xSERqRWpzS0IybHpjM1ZsTFRFUUFrb3VDZ2x6WldOd01qVTJhekVTSVFMYXVNQVh2ajcxZkVLdC01c1M4clJicWkwWHBaN3Y1N2lFdHRCNFh1cFc1Qkk3Q2dkdFlYTjBaWEl3RUFGS0xnb0pjMlZqY0RJMU5tc3hFaUVDVVYyZm9HTHlKOTRUeUc2VE1lTTZ6V0VCdEdWZ2YwOThKczNmYmtTV3RLSSIsInN1YiI6ImRpZDpwcmlzbTo4ZmY3MDBiOGZmZGNmNDhjMTRlYWJhNDc1MGMyN2NlZTVjMDc0ZWViMTdmMDIzZDU0NmYyOGRiMjk4OTdkMjA2OkNvVUJDb0lCRWpzS0IyMWhjM1JsY2pBUUFVb3VDZ2x6WldOd01qVTJhekVTSVFLODhJcklGMThrc29OMmlXMVpqTVM1ZG5fMndhcVJCLWNIRHZzSGxUUEFGQkpEQ2c5aGRYUm9aVzUwYVdOaGRHbHZiakFRQkVvdUNnbHpaV053TWpVMmF6RVNJUUs4OElySUYxOGtzb04yaVcxWmpNUzVkbl8yd2FxUkItY0hEdnNIbFRQQUZBIiwibmJmIjoxNzE4ODQ1MTg4LCJ2YyI6eyJjcmVkZW50aWFsU3ViamVjdCI6eyJlbWFpbEFkZHJlc3MiOiJkZW1vQGVtYWlsLmNvbSIsImRyaXZpbmdDbGFzcyI6IjEiLCJmYW1pbHlOYW1lIjoiZGVtbyIsImRyaXZpbmdMaWNlbnNlSUQiOiJBMTIyMTMzMiIsImlkIjoiZGlkOnByaXNtOjhmZjcwMGI4ZmZkY2Y0OGMxNGVhYmE0NzUwYzI3Y2VlNWMwNzRlZWIxN2YwMjNkNTQ2ZjI4ZGIyOTg5N2QyMDY6Q29VQkNvSUJFanNLQjIxaGMzUmxjakFRQVVvdUNnbHpaV053TWpVMmF6RVNJUUs4OElySUYxOGtzb04yaVcxWmpNUzVkbl8yd2FxUkItY0hEdnNIbFRQQUZCSkRDZzloZFhSb1pXNTBhV05oZEdsdmJqQVFCRW91Q2dselpXTndNalUyYXpFU0lRSzg4SXJJRjE4a3NvTjJpVzFaak1TNWRuXzJ3YXFSQi1jSER2c0hsVFBBRkEiLCJkYXRlT2ZJc3N1YW5jZSI6IjAxXC8wMVwvMjAyNCJ9LCJ0eXBlIjpbIlZlcmlmaWFibGVDcmVkZW50aWFsIl0sIkBjb250ZXh0IjpbImh0dHBzOlwvXC93d3cudzMub3JnXC8yMDE4XC9jcmVkZW50aWFsc1wvdjEiXSwiY3JlZGVudGlhbFN0YXR1cyI6eyJzdGF0dXNQdXJwb3NlIjoiUmV2b2NhdGlvbiIsInN0YXR1c0xpc3RJbmRleCI6MywiaWQiOiJodHRwOlwvXC8xOTIuMTY4LjY4LjExMzo4MDAwXC9jbG91ZC1hZ2VudFwvY3JlZGVudGlhbC1zdGF0dXNcLzZjNjdiNmMwLTFiZDktNDdhMS04NWVlLWY4OGVkYWE1YTg5NCMzIiwidHlwZSI6IlN0YXR1c0xpc3QyMDIxRW50cnkiLCJzdGF0dXNMaXN0Q3JlZGVudGlhbCI6Imh0dHA6XC9cLzE5Mi4xNjguNjguMTEzOjgwMDBcL2Nsb3VkLWFnZW50XC9jcmVkZW50aWFsLXN0YXR1c1wvNmM2N2I2YzAtMWJkOS00N2ExLTg1ZWUtZjg4ZWRhYTVhODk0In19fQ.phJQoTGsqZEftPw_ZvQe8l3t1vIJd4ehN9Fv37N88kA4mCMov-ncwH-jqlAdZOGuRSdAP_cQbcqxdLN42HzWFg"
        )
        assertFalse(pollux.isCredentialRevoked(credential))
    }

    @Test
    fun testIsCredentialRevoked_whenWrongJWSProof_thenThrowException() = runTest {
        val response = """{
                "proof": {
                    "type": "EcdsaSecp256k1Signature2019",
                    "proofPurpose": "assertionMethod",
                    "verificationMethod": "data:application/json;base64,eyJAY29udGV4dCI6WyJodHRwczovL3czaWQub3JnL3NlY3VyaXR5L3YxIl0sInR5cGUiOiJFY2RzYVNlY3AyNTZrMVZlcmlmaWNhdGlvbktleTIwMTkiLCJwdWJsaWNLZXlKd2siOnsiY3J2Ijoic2VjcDI1NmsxIiwia2V5X29wcyI6WyJ2ZXJpZnkiXSwia3R5IjoiRUMiLCJ4IjoiVFlCZ21sM1RpUWRSX1lRRDFoSXVOTzhiUnluU0otcmxQcWFVd3JXa3EtRT0iLCJ5IjoiVjBnVFlBM0xhbFd3Q3hPZHlqb2ZoR2JkYVFEd3EwQXdCblNodFJLXzNYZz0ifX0=",
                    "created": "2024-06-14T10:56:59.948091Z",
                    "jws": "eyJiNjQiOmZhbHNlLCJjcml0IjpbImI2NCJdLCJhbGciOiJFUzI1NksifQ..Q1mj3aMf5DWK83E55r6vNUPpsYYgclgwYoNFBSYBzA5x6fI_2cPHJsXECnQlG1XMj2ifldngpJXegTpwe3Fgwg"
                },
                "@context": [
                    "https://www.w3.org/2018/credentials/v1",
                    "https://w3id.org/vc/status-list/2021/v1"
                ],
                "type": [
                    "VerifiableCredential",
                    "StatusList2021Credential"
                ],
                "id": "http://localhost:8085/credential-status/575092c2-7eb0-40ae-8f41-3b499f45f3dc",
                "issuer": "did:prism:462c4811bf61d7de25b3baf86c5d2f0609b4debe53792d297bf612269bf8593a",
                "issuanceDate": 1717714047,
                "credentialSubject": {
                    "type": "StatusList2021",
                    "statusPurpose": "Revocation",
                    "encodedList": "H4sIAAAAAAAA_-3BMQ0AAAACIGf_0MbwARoAAAAAAAAAAAAAAAAAAADgbbmHB0sAQAAA"
                }
            }"""
        val httpResponse = HttpResponse(status = HttpStatusCode.OK.value, response)

        apollo = ApolloImpl()
        val api = spy(
            ApiImpl(
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
            )
        )

        pollux = PolluxImpl(apollo, castorMock, api)

        doReturn(httpResponse)
            .`when`(api).request(
                HttpMethod.Get.value,
                "http://10.91.100.126:8000/prism-agent/credential-status/514e8528-4b38-477a-b0e4-324bbe220464",
                emptyArray(),
                arrayOf(KeyValue(HttpHeaders.ContentType, Typ.Encrypted.typ)),
                null
            )

        val credential = JWTCredential.fromJwtString(
            "eyJhbGciOiJFUzI1NksifQ.eyJpc3MiOiJkaWQ6cHJpc206ZTAyZTgwOTlkNTAzNTEzNDVjNWRkODMxYTllOTExMmIzOTRhODVkMDA2NGEyZWI1OTQyOTA4MDczNGExNTliNjpDcmtCQ3JZQkVqb0tCbUYxZEdndE1SQUVTaTRLQ1hObFkzQXlOVFpyTVJJaEF1Vlljb3JmV25MMGZZdEE1dmdKSzRfLW9iM2JVRGMtdzJVT0hkTzNRRXZxRWpzS0IybHpjM1ZsTFRFUUFrb3VDZ2x6WldOd01qVTJhekVTSVFMQ3U5Tm50cXVwQmotME5DZE1BNzV6UmVCZXlhQ0pPMWFHWWVQNEJNUUhWQkk3Q2dkdFlYTjBaWEl3RUFGS0xnb0pjMlZqY0RJMU5tc3hFaUVET1dndlF4NnZSdTZ3VWI0RlljSnVhRUNqOUJqUE1KdlJwOEx3TTYxaEVUNCIsInN1YiI6ImRpZDpwcmlzbTpiZDgxZmY1NDQzNDJjMTAwNDZkZmE0YmEyOTVkNWIzNmU0Y2ZlNWE3ZWIxMjBlMTBlZTVjMjQ4NzAwNjUxMDA5OkNvVUJDb0lCRWpzS0IyMWhjM1JsY2pBUUFVb3VDZ2x6WldOd01qVTJhekVTSVFQdjVQNXl5Z3Jad2FKbFl6bDU5bTJIQURLVFhVTFBzUmUwa2dlRUh2dExnQkpEQ2c5aGRYUm9aVzUwYVdOaGRHbHZiakFRQkVvdUNnbHpaV053TWpVMmF6RVNJUVB2NVA1eXlnclp3YUpsWXpsNTltMkhBREtUWFVMUHNSZTBrZ2VFSHZ0TGdBIiwibmJmIjoxNzE1MDAwNjc0LCJleHAiOjE3MTg2MDA2NzQsInZjIjp7ImNyZWRlbnRpYWxTdWJqZWN0Ijp7ImVtYWlsQWRkcmVzcyI6ImNyaXN0aWFuLmNhc3Ryb0Bpb2hrLmlvIiwiaWQiOiJkaWQ6cHJpc206YmQ4MWZmNTQ0MzQyYzEwMDQ2ZGZhNGJhMjk1ZDViMzZlNGNmZTVhN2ViMTIwZTEwZWU1YzI0ODcwMDY1MTAwOTpDb1VCQ29JQkVqc0tCMjFoYzNSbGNqQVFBVW91Q2dselpXTndNalUyYXpFU0lRUHY1UDV5eWdyWndhSmxZemw1OW0ySEFES1RYVUxQc1JlMGtnZUVIdnRMZ0JKRENnOWhkWFJvWlc1MGFXTmhkR2x2YmpBUUJFb3VDZ2x6WldOd01qVTJhekVTSVFQdjVQNXl5Z3Jad2FKbFl6bDU5bTJIQURLVFhVTFBzUmUwa2dlRUh2dExnQSJ9LCJ0eXBlIjpbIlZlcmlmaWFibGVDcmVkZW50aWFsIl0sIkBjb250ZXh0IjpbImh0dHBzOlwvXC93d3cudzMub3JnXC8yMDE4XC9jcmVkZW50aWFsc1wvdjEiXSwiY3JlZGVudGlhbFN0YXR1cyI6eyJzdGF0dXNQdXJwb3NlIjoiUmV2b2NhdGlvbiIsInN0YXR1c0xpc3RJbmRleCI6MjUsImlkIjoiaHR0cDpcL1wvMTAuOTEuMTAwLjEyNjo4MDAwXC9wcmlzbS1hZ2VudFwvY3JlZGVudGlhbC1zdGF0dXNcLzUxNGU4NTI4LTRiMzgtNDc3YS1iMGU0LTMyNGJiZTIyMDQ2NCMyNSIsInR5cGUiOiJTdGF0dXNMaXN0MjAyMUVudHJ5Iiwic3RhdHVzTGlzdENyZWRlbnRpYWwiOiJodHRwOlwvXC8xMC45MS4xMDAuMTI2OjgwMDBcL3ByaXNtLWFnZW50XC9jcmVkZW50aWFsLXN0YXR1c1wvNTE0ZTg1MjgtNGIzOC00NzdhLWIwZTQtMzI0YmJlMjIwNDY0In19fQ.5OmmL5tdcRKugiHVt01PJUhp9r22zgMPPALUOB41g_1_BPHE3ezqJ2QhSmScU_EOGYcKksHftdrvfoND65nSjw"
        )
        assertFailsWith<PolluxError.VerifyProofError> {
            pollux.isCredentialRevoked(credential)
        }
    }

    @Test
    fun testIsCredentialRevoked_whenMissingProof_thenThrowException() = runTest {
        val response = """{
                "@context": [
                    "https://www.w3.org/2018/credentials/v1",
                    "https://w3id.org/vc/status-list/2021/v1"
                ],
                "type": [
                    "VerifiableCredential",
                    "StatusList2021Credential"
                ],
                "id": "http://localhost:8085/credential-status/575092c2-7eb0-40ae-8f41-3b499f45f3dc",
                "issuer": "did:prism:462c4811bf61d7de25b3baf86c5d2f0609b4debe53792d297bf612269bf8593a",
                "issuanceDate": 1717714047,
                "credentialSubject": {
                    "type": "StatusList2021",
                    "statusPurpose": "Revocation",
                    "encodedList": "H4sIAAAAAAAA_-3BMQ0AAAACIGf_0MbwARoAAAAAAAAAAAAAAAAAAADgbbmHB0sAQAAA"
                }
            }"""
        val httpResponse = HttpResponse(status = HttpStatusCode.OK.value, response)

        apollo = ApolloImpl()
        val api = spy(
            ApiImpl(
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
            )
        )

        pollux = PolluxImpl(apollo, castorMock, api)

        doReturn(httpResponse)
            .`when`(api).request(
                HttpMethod.Get.value,
                "http://10.91.100.126:8000/prism-agent/credential-status/514e8528-4b38-477a-b0e4-324bbe220464",
                emptyArray(),
                arrayOf(KeyValue(HttpHeaders.ContentType, Typ.Encrypted.typ)),
                null
            )

        val credential = JWTCredential.fromJwtString(
            "eyJhbGciOiJFUzI1NksifQ.eyJpc3MiOiJkaWQ6cHJpc206ZTAyZTgwOTlkNTAzNTEzNDVjNWRkODMxYTllOTExMmIzOTRhODVkMDA2NGEyZWI1OTQyOTA4MDczNGExNTliNjpDcmtCQ3JZQkVqb0tCbUYxZEdndE1SQUVTaTRLQ1hObFkzQXlOVFpyTVJJaEF1Vlljb3JmV25MMGZZdEE1dmdKSzRfLW9iM2JVRGMtdzJVT0hkTzNRRXZxRWpzS0IybHpjM1ZsTFRFUUFrb3VDZ2x6WldOd01qVTJhekVTSVFMQ3U5Tm50cXVwQmotME5DZE1BNzV6UmVCZXlhQ0pPMWFHWWVQNEJNUUhWQkk3Q2dkdFlYTjBaWEl3RUFGS0xnb0pjMlZqY0RJMU5tc3hFaUVET1dndlF4NnZSdTZ3VWI0RlljSnVhRUNqOUJqUE1KdlJwOEx3TTYxaEVUNCIsInN1YiI6ImRpZDpwcmlzbTpiZDgxZmY1NDQzNDJjMTAwNDZkZmE0YmEyOTVkNWIzNmU0Y2ZlNWE3ZWIxMjBlMTBlZTVjMjQ4NzAwNjUxMDA5OkNvVUJDb0lCRWpzS0IyMWhjM1JsY2pBUUFVb3VDZ2x6WldOd01qVTJhekVTSVFQdjVQNXl5Z3Jad2FKbFl6bDU5bTJIQURLVFhVTFBzUmUwa2dlRUh2dExnQkpEQ2c5aGRYUm9aVzUwYVdOaGRHbHZiakFRQkVvdUNnbHpaV053TWpVMmF6RVNJUVB2NVA1eXlnclp3YUpsWXpsNTltMkhBREtUWFVMUHNSZTBrZ2VFSHZ0TGdBIiwibmJmIjoxNzE1MDAwNjc0LCJleHAiOjE3MTg2MDA2NzQsInZjIjp7ImNyZWRlbnRpYWxTdWJqZWN0Ijp7ImVtYWlsQWRkcmVzcyI6ImNyaXN0aWFuLmNhc3Ryb0Bpb2hrLmlvIiwiaWQiOiJkaWQ6cHJpc206YmQ4MWZmNTQ0MzQyYzEwMDQ2ZGZhNGJhMjk1ZDViMzZlNGNmZTVhN2ViMTIwZTEwZWU1YzI0ODcwMDY1MTAwOTpDb1VCQ29JQkVqc0tCMjFoYzNSbGNqQVFBVW91Q2dselpXTndNalUyYXpFU0lRUHY1UDV5eWdyWndhSmxZemw1OW0ySEFES1RYVUxQc1JlMGtnZUVIdnRMZ0JKRENnOWhkWFJvWlc1MGFXTmhkR2x2YmpBUUJFb3VDZ2x6WldOd01qVTJhekVTSVFQdjVQNXl5Z3Jad2FKbFl6bDU5bTJIQURLVFhVTFBzUmUwa2dlRUh2dExnQSJ9LCJ0eXBlIjpbIlZlcmlmaWFibGVDcmVkZW50aWFsIl0sIkBjb250ZXh0IjpbImh0dHBzOlwvXC93d3cudzMub3JnXC8yMDE4XC9jcmVkZW50aWFsc1wvdjEiXSwiY3JlZGVudGlhbFN0YXR1cyI6eyJzdGF0dXNQdXJwb3NlIjoiUmV2b2NhdGlvbiIsInN0YXR1c0xpc3RJbmRleCI6MjUsImlkIjoiaHR0cDpcL1wvMTAuOTEuMTAwLjEyNjo4MDAwXC9wcmlzbS1hZ2VudFwvY3JlZGVudGlhbC1zdGF0dXNcLzUxNGU4NTI4LTRiMzgtNDc3YS1iMGU0LTMyNGJiZTIyMDQ2NCMyNSIsInR5cGUiOiJTdGF0dXNMaXN0MjAyMUVudHJ5Iiwic3RhdHVzTGlzdENyZWRlbnRpYWwiOiJodHRwOlwvXC8xMC45MS4xMDAuMTI2OjgwMDBcL3ByaXNtLWFnZW50XC9jcmVkZW50aWFsLXN0YXR1c1wvNTE0ZTg1MjgtNGIzOC00NzdhLWIwZTQtMzI0YmJlMjIwNDY0In19fQ.5OmmL5tdcRKugiHVt01PJUhp9r22zgMPPALUOB41g_1_BPHE3ezqJ2QhSmScU_EOGYcKksHftdrvfoND65nSjw"
        )
        assertFailsWith<PolluxError.RevocationRegistryJsonMissingFieldError> {
            pollux.isCredentialRevoked(credential)
        }
    }

    @Test
    fun testIsCredentialRevoked_whenCorrectJson_thenValidationCorrect() = runTest {
        val response = """{
                "proof": {
                    "type": "EcdsaSecp256k1Signature2019",
                    "proofPurpose": "assertionMethod",
                    "verificationMethod": "data:application/json;base64,eyJAY29udGV4dCI6WyJodHRwczovL3czaWQub3JnL3NlY3VyaXR5L3YxIl0sInR5cGUiOiJFY2RzYVNlY3AyNTZrMVZlcmlmaWNhdGlvbktleTIwMTkiLCJwdWJsaWNLZXlKd2siOnsiY3J2Ijoic2VjcDI1NmsxIiwia2V5X29wcyI6WyJ2ZXJpZnkiXSwia3R5IjoiRUMiLCJ4IjoiQ1hJRmwyUjE4YW1lTEQteWtTT0dLUW9DQlZiRk01b3Vsa2MydklySnRTND0iLCJ5IjoiRDJRWU5pNi1BOXoxbHhwUmpLYm9jS1NUdk5BSXNOVnNsQmpsemVnWXlVQT0ifX0=",
                    "created": "2024-07-25T22:49:59.091957Z",
                    "jws": "eyJiNjQiOmZhbHNlLCJjcml0IjpbImI2NCJdLCJhbGciOiJFUzI1NksifQ..FJLUBsZhGB1o_G1UwsVaoL-8agvcpoelJtAr2GlNOOqCSOd-WNEj5-FOgv0m0QcdKMokl2TxibJMg3Y-MJq4-A"
                },
                "@context": [
                    "https://www.w3.org/2018/credentials/v1",
                    "https://w3id.org/vc/status-list/2021/v1"
                ],
                "type": [
                    "VerifiableCredential",
                    "StatusList2021Credential"
                ],
                "id": "http://localhost:8085/credential-status/01def9a2-2bcb-4bb3-8a36-6834066431d0",
                "issuer": "did:prism:462c4811bf61d7de25b3baf86c5d2f0609b4debe53792d297bf612269bf8593a",
                "issuanceDate": 1721947798,
                "credentialSubject": {
                    "type": "StatusList2021",
                    "statusPurpose": "Revocation",
                    "encodedList": "H4sIAAAAAAAA_-3BIQEAAAACIKf6f4UzLEADAAAAAAAAAAAAAAAAAAAAvA3PduITAEAAAA=="
                }
            }"""
        val httpResponse = HttpResponse(status = HttpStatusCode.OK.value, response)

        apollo = ApolloImpl()
        val api = spy(
            ApiImpl(
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
            )
        )

        pollux = PolluxImpl(apollo, castorMock, api)

        doReturn(httpResponse)
            .`when`(api).request(
                HttpMethod.Get.value,
                "http://10.91.100.126:8000/prism-agent/credential-status/514e8528-4b38-477a-b0e4-324bbe220464",
                emptyArray(),
                arrayOf(KeyValue(HttpHeaders.ContentType, Typ.Encrypted.typ)),
                null
            )

        val credential = JWTCredential.fromJwtString(
            "eyJhbGciOiJFUzI1NksifQ.eyJpc3MiOiJkaWQ6cHJpc206ZTAyZTgwOTlkNTAzNTEzNDVjNWRkODMxYTllOTExMmIzOTRhODVkMDA2NGEyZWI1OTQyOTA4MDczNGExNTliNjpDcmtCQ3JZQkVqb0tCbUYxZEdndE1SQUVTaTRLQ1hObFkzQXlOVFpyTVJJaEF1Vlljb3JmV25MMGZZdEE1dmdKSzRfLW9iM2JVRGMtdzJVT0hkTzNRRXZxRWpzS0IybHpjM1ZsTFRFUUFrb3VDZ2x6WldOd01qVTJhekVTSVFMQ3U5Tm50cXVwQmotME5DZE1BNzV6UmVCZXlhQ0pPMWFHWWVQNEJNUUhWQkk3Q2dkdFlYTjBaWEl3RUFGS0xnb0pjMlZqY0RJMU5tc3hFaUVET1dndlF4NnZSdTZ3VWI0RlljSnVhRUNqOUJqUE1KdlJwOEx3TTYxaEVUNCIsInN1YiI6ImRpZDpwcmlzbTpiZDgxZmY1NDQzNDJjMTAwNDZkZmE0YmEyOTVkNWIzNmU0Y2ZlNWE3ZWIxMjBlMTBlZTVjMjQ4NzAwNjUxMDA5OkNvVUJDb0lCRWpzS0IyMWhjM1JsY2pBUUFVb3VDZ2x6WldOd01qVTJhekVTSVFQdjVQNXl5Z3Jad2FKbFl6bDU5bTJIQURLVFhVTFBzUmUwa2dlRUh2dExnQkpEQ2c5aGRYUm9aVzUwYVdOaGRHbHZiakFRQkVvdUNnbHpaV053TWpVMmF6RVNJUVB2NVA1eXlnclp3YUpsWXpsNTltMkhBREtUWFVMUHNSZTBrZ2VFSHZ0TGdBIiwibmJmIjoxNzE1MDAwNjc0LCJleHAiOjE3MTg2MDA2NzQsInZjIjp7ImNyZWRlbnRpYWxTdWJqZWN0Ijp7ImVtYWlsQWRkcmVzcyI6ImNyaXN0aWFuLmNhc3Ryb0Bpb2hrLmlvIiwiaWQiOiJkaWQ6cHJpc206YmQ4MWZmNTQ0MzQyYzEwMDQ2ZGZhNGJhMjk1ZDViMzZlNGNmZTVhN2ViMTIwZTEwZWU1YzI0ODcwMDY1MTAwOTpDb1VCQ29JQkVqc0tCMjFoYzNSbGNqQVFBVW91Q2dselpXTndNalUyYXpFU0lRUHY1UDV5eWdyWndhSmxZemw1OW0ySEFES1RYVUxQc1JlMGtnZUVIdnRMZ0JKRENnOWhkWFJvWlc1MGFXTmhkR2x2YmpBUUJFb3VDZ2x6WldOd01qVTJhekVTSVFQdjVQNXl5Z3Jad2FKbFl6bDU5bTJIQURLVFhVTFBzUmUwa2dlRUh2dExnQSJ9LCJ0eXBlIjpbIlZlcmlmaWFibGVDcmVkZW50aWFsIl0sIkBjb250ZXh0IjpbImh0dHBzOlwvXC93d3cudzMub3JnXC8yMDE4XC9jcmVkZW50aWFsc1wvdjEiXSwiY3JlZGVudGlhbFN0YXR1cyI6eyJzdGF0dXNQdXJwb3NlIjoiUmV2b2NhdGlvbiIsInN0YXR1c0xpc3RJbmRleCI6MjUsImlkIjoiaHR0cDpcL1wvMTAuOTEuMTAwLjEyNjo4MDAwXC9wcmlzbS1hZ2VudFwvY3JlZGVudGlhbC1zdGF0dXNcLzUxNGU4NTI4LTRiMzgtNDc3YS1iMGU0LTMyNGJiZTIyMDQ2NCMyNSIsInR5cGUiOiJTdGF0dXNMaXN0MjAyMUVudHJ5Iiwic3RhdHVzTGlzdENyZWRlbnRpYWwiOiJodHRwOlwvXC8xMC45MS4xMDAuMTI2OjgwMDBcL3ByaXNtLWFnZW50XC9jcmVkZW50aWFsLXN0YXR1c1wvNTE0ZTg1MjgtNGIzOC00NzdhLWIwZTQtMzI0YmJlMjIwNDY0In19fQ.5OmmL5tdcRKugiHVt01PJUhp9r22zgMPPALUOB41g_1_BPHE3ezqJ2QhSmScU_EOGYcKksHftdrvfoND65nSjw"
        )
        val vc = JWTVerifiableCredential(
            context = credential.verifiableCredential!!.context,
            type = credential.verifiableCredential!!.type,
            credentialSchema = credential.verifiableCredential!!.credentialSchema,
            credentialSubject = credential.verifiableCredential!!.credentialSubject,
            credentialStatus = Json.decodeFromString("""{"statusPurpose":"Revocation","statusListIndex":2,"id":"http://10.91.100.126:8000/prism-agent/credential-status/514e8528-4b38-477a-b0e4-324bbe220464#25","type":"StatusList2021Entry","statusListCredential":"http://10.91.100.126:8000/prism-agent/credential-status/514e8528-4b38-477a-b0e4-324bbe220464"}"""),
            refreshService = credential.verifiableCredential!!.refreshService,
            evidence = credential.verifiableCredential!!.evidence,
            termsOfUse = credential.verifiableCredential!!.termsOfUse
        )

        credential.verifiableCredential = vc
        val isRevoked = pollux.isCredentialRevoked(credential)
        assertTrue(isRevoked)

        val vc1 = JWTVerifiableCredential(
            context = credential.verifiableCredential!!.context,
            type = credential.verifiableCredential!!.type,
            credentialSchema = credential.verifiableCredential!!.credentialSchema,
            credentialSubject = credential.verifiableCredential!!.credentialSubject,
            credentialStatus = Json.decodeFromString("""{"statusPurpose":"Revocation","statusListIndex":3,"id":"http://10.91.100.126:8000/prism-agent/credential-status/514e8528-4b38-477a-b0e4-324bbe220464#25","type":"StatusList2021Entry","statusListCredential":"http://10.91.100.126:8000/prism-agent/credential-status/514e8528-4b38-477a-b0e4-324bbe220464"}"""),
            refreshService = credential.verifiableCredential!!.refreshService,
            evidence = credential.verifiableCredential!!.evidence,
            termsOfUse = credential.verifiableCredential!!.termsOfUse
        )

        credential.verifiableCredential = vc1
        assertFalse(pollux.isCredentialRevoked(credential))
    }

    @Test
    fun testEncodedListUnGzip_whenNotRevoked_thenReturnFalse() = runTest {
        val httpResponse = correctHttpResponseFetchRevocationRegistry()

        pollux = PolluxImpl(apollo, castorMock, api)

        assertFalse(pollux.checkEncodedListRevoked(httpResponse.jsonString, 3))
    }

    @Test
    fun testEncodedListUnGzip_whenFirstThreeRevoked_thenProvedOk() = runTest {
        val encodedList = "H4sIAAAAAAAA_-3BIQEAAAACIKf6f4UzLEADAAAAAAAAAAAAAAAAAAAAvA3PduITAEAAAA=="

        pollux = PolluxImpl(apollo, castorMock, api)

        assertTrue(pollux.verifyStatusListIndexForEncodedList(encodedList, 1))
        assertTrue(pollux.verifyStatusListIndexForEncodedList(encodedList, 2))
        assertFalse(pollux.verifyStatusListIndexForEncodedList(encodedList, 3))
    }

    @Test
    fun `Test signClaims for JWT including kid`() = runTest {
        pollux = PolluxImpl(apollo, castor, api)
        val keyPair = Secp256k1KeyPair.generateKeyPair()

        val did =
            DID("did:prism:cd6cf9f94a43c53e286b0f2015c0083701350a694f52a22ee02e3bd29d93eba9:CrQBCrEBEjsKB21hc3RlcjAQAUouCglzZWNwMjU2azESIQKJIokEe_iKRGsr0f2EEa1JHGm59g0qP7QMtw6FcVxW9xJDCg9hdXRoZW50aWNhdGlvbjAQBEouCglzZWNwMjU2azESIQKJIokEe_iKRGsr0f2EEa1JHGm59g0qP7QMtw6FcVxW9xotCgojZGlkY29tbS0xEhBESURDb21tTWVzc2FnaW5nGg1kaWQ6cGVlcjp0ZXN0")
        val domain = "domain"
        val challenge = "challenge"
        val credential = JWTCredential.fromJwtString(
            "eyJhbGciOiJFUzI1NksifQ.eyJpc3MiOiJkaWQ6cHJpc206ZTAyZTgwOTlkNTAzNTEzNDVjNWRkODMxYTllOTExMmIzOTRhODVkMDA2NGEyZWI1OTQyOTA4MDczNGExNTliNjpDcmtCQ3JZQkVqb0tCbUYxZEdndE1SQUVTaTRLQ1hObFkzQXlOVFpyTVJJaEF1Vlljb3JmV25MMGZZdEE1dmdKSzRfLW9iM2JVRGMtdzJVT0hkTzNRRXZxRWpzS0IybHpjM1ZsTFRFUUFrb3VDZ2x6WldOd01qVTJhekVTSVFMQ3U5Tm50cXVwQmotME5DZE1BNzV6UmVCZXlhQ0pPMWFHWWVQNEJNUUhWQkk3Q2dkdFlYTjBaWEl3RUFGS0xnb0pjMlZqY0RJMU5tc3hFaUVET1dndlF4NnZSdTZ3VWI0RlljSnVhRUNqOUJqUE1KdlJwOEx3TTYxaEVUNCIsInN1YiI6ImRpZDpwcmlzbTpiZDgxZmY1NDQzNDJjMTAwNDZkZmE0YmEyOTVkNWIzNmU0Y2ZlNWE3ZWIxMjBlMTBlZTVjMjQ4NzAwNjUxMDA5OkNvVUJDb0lCRWpzS0IyMWhjM1JsY2pBUUFVb3VDZ2x6WldOd01qVTJhekVTSVFQdjVQNXl5Z3Jad2FKbFl6bDU5bTJIQURLVFhVTFBzUmUwa2dlRUh2dExnQkpEQ2c5aGRYUm9aVzUwYVdOaGRHbHZiakFRQkVvdUNnbHpaV053TWpVMmF6RVNJUVB2NVA1eXlnclp3YUpsWXpsNTltMkhBREtUWFVMUHNSZTBrZ2VFSHZ0TGdBIiwibmJmIjoxNzE1MDAwNjc0LCJleHAiOjE3MTg2MDA2NzQsInZjIjp7ImNyZWRlbnRpYWxTdWJqZWN0Ijp7ImVtYWlsQWRkcmVzcyI6ImNyaXN0aWFuLmNhc3Ryb0Bpb2hrLmlvIiwiaWQiOiJkaWQ6cHJpc206YmQ4MWZmNTQ0MzQyYzEwMDQ2ZGZhNGJhMjk1ZDViMzZlNGNmZTVhN2ViMTIwZTEwZWU1YzI0ODcwMDY1MTAwOTpDb1VCQ29JQkVqc0tCMjFoYzNSbGNqQVFBVW91Q2dselpXTndNalUyYXpFU0lRUHY1UDV5eWdyWndhSmxZemw1OW0ySEFES1RYVUxQc1JlMGtnZUVIdnRMZ0JKRENnOWhkWFJvWlc1MGFXTmhkR2x2YmpBUUJFb3VDZ2x6WldOd01qVTJhekVTSVFQdjVQNXl5Z3Jad2FKbFl6bDU5bTJIQURLVFhVTFBzUmUwa2dlRUh2dExnQSJ9LCJ0eXBlIjpbIlZlcmlmaWFibGVDcmVkZW50aWFsIl0sIkBjb250ZXh0IjpbImh0dHBzOlwvXC93d3cudzMub3JnXC8yMDE4XC9jcmVkZW50aWFsc1wvdjEiXSwiY3JlZGVudGlhbFN0YXR1cyI6eyJzdGF0dXNQdXJwb3NlIjoiUmV2b2NhdGlvbiIsInN0YXR1c0xpc3RJbmRleCI6MjUsImlkIjoiaHR0cDpcL1wvMTAuOTEuMTAwLjEyNjo4MDAwXC9wcmlzbS1hZ2VudFwvY3JlZGVudGlhbC1zdGF0dXNcLzUxNGU4NTI4LTRiMzgtNDc3YS1iMGU0LTMyNGJiZTIyMDQ2NCMyNSIsInR5cGUiOiJTdGF0dXNMaXN0MjAyMUVudHJ5Iiwic3RhdHVzTGlzdENyZWRlbnRpYWwiOiJodHRwOlwvXC8xMC45MS4xMDAuMTI2OjgwMDBcL3ByaXNtLWFnZW50XC9jcmVkZW50aWFsLXN0YXR1c1wvNTE0ZTg1MjgtNGIzOC00NzdhLWIwZTQtMzI0YmJlMjIwNDY0In19fQ.5OmmL5tdcRKugiHVt01PJUhp9r22zgMPPALUOB41g_1_BPHE3ezqJ2QhSmScU_EOGYcKksHftdrvfoND65nSjw"
        )
        val signedClaims = pollux.signClaims(
            subjectDID = did,
            privateKey = keyPair.privateKey,
            domain = domain,
            challenge = challenge,
            credential = credential
        )
        assertTrue(signedClaims.contains("."))
        val splits = signedClaims.split(".")
        val header = splits[0].base64UrlDecoded
        val json = Json.parseToJsonElement(header)
        assertTrue(json.jsonObject.containsKey("kid"))
        val kid = json.jsonObject["kid"]!!.jsonPrimitive.content
        assertEquals(
            "did:prism:cd6cf9f94a43c53e286b0f2015c0083701350a694f52a22ee02e3bd29d93eba9:CrQBCrEBEjsKB21hc3RlcjAQAUouCglzZWNwMjU2azESIQKJIokEe_iKRGsr0f2EEa1JHGm59g0qP7QMtw6FcVxW9xJDCg9hdXRoZW50aWNhdGlvbjAQBEouCglzZWNwMjU2azESIQKJIokEe_iKRGsr0f2EEa1JHGm59g0qP7QMtw6FcVxW9xotCgojZGlkY29tbS0xEhBESURDb21tTWVzc2FnaW5nGg1kaWQ6cGVlcjp0ZXN0#authentication0",
            kid
        )
    }

    @Test
    fun `Test SD-JWT createPresentationDefinitionRequest`() = runTest {
        pollux = PolluxImpl(apollo, castor, api)

        val sdJwtPresentationDefinitionRequest = pollux.createPresentationDefinitionRequest(
            type = CredentialType.SDJWT,
            presentationClaims = SDJWTPresentationClaims(
                claims = mapOf(
                    "first_name" to InputFieldFilter(
                        type = "string",
                        pattern = "Wonderland"
                    ),
                    "last_name" to InputFieldFilter(
                        type = "string",
                        pattern = "Alice"
                    ),
                    "emailAddress" to InputFieldFilter(
                        type = "string",
                        pattern = "alice@wonderland.com"
                    ),
                )
            ),
            options = SDJWTPresentationOptions()
        )
        val presentationDefinition =
            Json.decodeFromString<SDJWTPresentationDefinitionRequest>(sdJwtPresentationDefinitionRequest)
        val inputDescriptor = presentationDefinition.presentationDefinition.inputDescriptors.first()
        assertNotNull(inputDescriptor.constraints.fields)
        var firstNameAvailable = false
        var lastNameAvailable = false
        var emailAvailable = false
        inputDescriptor.constraints.fields!!.forEach {
            if (it.name == "first_name") {
                firstNameAvailable = true
            }
            if (it.name == "last_name") {
                lastNameAvailable = true
            }
            if (it.name == "emailAddress") {
                emailAvailable = true
            }
        }
        assertTrue(firstNameAvailable && lastNameAvailable && emailAvailable)
    }

    @Test
    fun `test verifyPresentationSubmission SD-JWT`() = runTest {
        pollux = PolluxImpl(apollo, castor, api)
        val msg = Json.decodeFromString<Message>(
            """{"id":"56992a63-9871-490a-b9f8-4b1238c23c5e","piuri":"https://didcomm.atalaprism.io/present-proof/3.0/request-presentation","from":{"method":"peer","methodId":"asdf"},"to":{"method":"peer","methodId":"fdsafdsa"},"fromPrior":null,"body":"{\"proof_types\":[]}","created_time":"1726767099","expires_time_plus":"1726853499","attachments":[{"id":"f135525e-26c7-44f5-8f23-b8fbc928bfb2","media_type":"application/json","data":{"base64":"eyJwcmVzZW50YXRpb25fZGVmaW5pdGlvbiI6eyJpZCI6IjVhMTljNjZmLWEwZTUtNGFhOC1iNmE5LTdjZGIzYzk0ZGI0NyIsImlucHV0X2Rlc2NyaXB0b3JzIjpbeyJpZCI6IjBjNGY1ZDg0LTBmYjItNGU0Yy1iMDViLTZjZTBmMGFmYzU4NiIsIm5hbWUiOiJQcmVzZW50YXRpb24iLCJwdXJwb3NlIjoiUHJlc2VudGF0aW9uIGRlZmluaXRpb24iLCJmb3JtYXQiOnsic2RKd3QiOnsiYWxnIjpbIkVTMjU2SyJdfX0sImNvbnN0cmFpbnRzIjp7ImZpZWxkcyI6W3sicGF0aCI6WyIkLnZjLmNyZWRlbnRpYWxTdWJqZWN0LmZpcnN0X25hbWUiLCIkLmNyZWRlbnRpYWxTdWJqZWN0LmZpcnN0X25hbWUiLCIkLmZpcnN0X25hbWUiXSwiaWQiOiI2YWFmYmI3NC01ZjE4LTQ4YTMtOGJlNC00MmI3NWIzZmNlZDgiLCJuYW1lIjoiZmlyc3RfbmFtZSIsImZpbHRlciI6eyJ0eXBlIjoic3RyaW5nIiwicGF0dGVybiI6IkNyaXN0aWFuIn19LHsicGF0aCI6WyIkLnZjLmNyZWRlbnRpYWxTdWJqZWN0Lmxhc3RfbmFtZSIsIiQuY3JlZGVudGlhbFN1YmplY3QubGFzdF9uYW1lIiwiJC5sYXN0X25hbWUiXSwiaWQiOiJiMmU1NTkxNi0yYjU1LTQ0MTEtYTQzMS0wOTlkOWMyMDQ0ZjAiLCJuYW1lIjoibGFzdF9uYW1lIiwiZmlsdGVyIjp7InR5cGUiOiJzdHJpbmciLCJwYXR0ZXJuIjoiR29uemFsZXoifX0seyJwYXRoIjpbIiQudmMuY3JlZGVudGlhbFN1YmplY3QuZW1haWxBZGRyZXNzIiwiJC5jcmVkZW50aWFsU3ViamVjdC5lbWFpbEFkZHJlc3MiLCIkLmVtYWlsQWRkcmVzcyJdLCJpZCI6IjU2ODRlMTY0LTA0MzAtNGYyYS1iMDI2LWRmNjcwYWZjNGVkNSIsIm5hbWUiOiJlbWFpbEFkZHJlc3MiLCJmaWx0ZXIiOnsidHlwZSI6InN0cmluZyIsInBhdHRlcm4iOiJ0ZXN0QGlvaGsuaW8ifX1dLCJsaW1pdF9kaXNjbG9zdXJlIjoicmVxdWlyZWQifX1dLCJmb3JtYXQiOnsic2RKd3QiOnsiYWxnIjpbIkVTMjU2SyJdfX19fQ=="},"format":"dif/presentation-exchange/definitions@v1.0"}],"thid":"11001ef6-f4c9-430c-84d7-ef74f0689e9f","ack":[],"direction":"SENT"}"""
        )
        val requestData = msg.attachments.first().data.getDataAsJsonString()

        val presentationSubmission =
            """{"presentation_submission":{"id":"780d0597-b2fd-4f22-a7dc-b29b4fa08cc1","definition_id":"5569abd7-2994-4068-be36-4025ebcaf20b","descriptor_map":[{"id":"0c4f5d84-0fb2-4e4c-b05b-6ce0f0afc586","format":"sdjwt","path":"${'$'}.verifiablePresentation[0]"}]},"verifiablePresentation":["eyJhbGciOiJFZERTQSJ9.eyJzdWIiOiJkaWQ6cHJpc206YXNkZmFzZGYiLCJfc2QiOlsiWWdQSV9kREQySVE4Z3d0MF9CbUs4Rk55MEpVVXoxbmJoTzQ2b0tuNUFsRSIsIi1nZ2wxR3g1akQzY2VoNHUxTHRXc0xQM3Z5MWtGNktlN2lCWXV5Ynd2eUUiLCJEQnM2Z0hhQTZZUldDSGVXZjlnRHA2eXVnaTVtX3dudkdPSno0aFdmdXlNIl0sIl9zZF9hbGciOiJzaGEtMjU2IiwiaXNzIjoiZGlkOnByaXNtOmNlMzQwM2I1YTczMzg4MzAzNWQ2ZWM0M2JhMDc1YTQxYzljYzBhMzI1Nzk3N2Q4MGM3NWQ2MzE5YWRlMGVkNzAiLCJleHAiOjE3MzU2ODk2NjEsImlhdCI6MTUxNjIzOTAyMn0.hVy9qpC9bVFpoGQ0G2s1GOkDgZxNGdhHIoVOaF8IMQU5pIvxZH1UQ1GzDpnhU0m_ZM8r1QpCeSVPuQKvMluDBQ~WyJtRzVscURxRFg2YWdraHpjX0NBR19RIiwiZmlyc3RfbmFtZSIsIkNyaXN0aWFuIl0~WyJSY2Z5X3ZCQm9wUjhLZFZqejRxbGJRIiwibGFzdF9uYW1lIiwiR29uemFsZXoiXQ~WyJpM0kzQWZJTERuSnVqRXkxS2ZNbFp3IiwiZW1haWxBZGRyZXNzIiwidGVzdEBpb2hrLmlvIl0~"]}"""

        assertTrue(
            pollux.verifyPresentationSubmission(
                presentationSubmission,
                PresentationSubmissionOptionsSDJWT(presentationDefinitionRequest = requestData)
            )
        )
    }

    private suspend fun createVerificationTestCase(testCaseOptions: VerificationTestCase): Triple<String, String, String> {
        val currentDate = Calendar.getInstance()
        val nextMonthDate = currentDate.clone() as Calendar
        nextMonthDate.add(Calendar.MONTH, 1)
        val issuanceDate = currentDate.timeInMillis
        val expirationDate = nextMonthDate.timeInMillis
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val jwsHeader = JWSHeader.Builder(JWSAlgorithm.ES256K).build()
        val json = Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        }

        val vc = json.decodeFromString<JWTVerifiableCredential>(
            """{"@context":[
                     "https://www.w3.org/2018/credentials/v1"
                  ],
                  "type":[
                     "VerifiableCredential"
                  ],
                  "issuer":"${testCaseOptions.issuer}",
                  "issuanceDate": "${sdf.format(Date(issuanceDate))}",
                  "credentialStatus": {
                      "statusPurpose": "Revocation",
                      "statusListIndex": 25,
                      "id": "http://10.91.100.126:8000/prism-agent/credential-status/514e8528-4b38-477a-b0e4-324bbe220464#25",
                      "type": "StatusList2021Entry",
                      "statusListCredential": "http://10.91.100.126:8000/prism-agent/credential-status/514e8528-4b38-477a-b0e4-324bbe220464"
                    },
                  "credentialSubject": ${testCaseOptions.subject}
                  }"""
        )

        val ecPrivateKey = pollux.parsePrivateKey(testCaseOptions.issuerPrv)
        val claims = JWTClaimsSet.Builder()
            .issuer(testCaseOptions.issuer.toString())
            .audience(testCaseOptions.domain)
            .notBeforeTime(Date(issuanceDate))
            .expirationTime(Date(expirationDate))
            .subject(testCaseOptions.holder.toString())
            .claim("vc", vc)
            .build()
        val signedJwt = SignedJWT(jwsHeader, claims)
        val signer = ECDSASigner(
            ecPrivateKey as java.security.PrivateKey,
            com.nimbusds.jose.jwk.Curve.SECP256K1
        )
        val provider = BouncyCastleProviderSingleton.getInstance()
        signer.jcaContext.provider = provider
        signedJwt.sign(signer)
        val jwtString = signedJwt.serialize()
        val jwtCredential = JWTCredential.fromJwtString(jwtString)
        val presentationDefinition = pollux.createPresentationDefinitionRequest(
            type = CredentialType.JWT,
            presentationClaims = JWTPresentationClaims(
                issuer = testCaseOptions.issuer.toString(),
                claims = (testCaseOptions.claims as JWTPresentationClaims).claims
            ),
            options = JWTPresentationOptions(domain = "domain", challenge = testCaseOptions.challenge)
        )

        doReturn(false)
            .`when`(pollux).isCredentialRevoked(any())

        val presentationSubmission = jwtCredential.presentation(
            attachmentFormat = CredentialType.PRESENTATION_EXCHANGE_DEFINITIONS.type,
            request = presentationDefinition.encodeToByteArray(),
            options = listOf(
                CredentialOperationsOptions.SubjectDID(testCaseOptions.holder),
                CredentialOperationsOptions.ExportableKey(testCaseOptions.holderPrv)
            )

        )
        return Triple(presentationDefinition, presentationSubmission, jwtString)
    }

    data class VerificationTestCase(
        val issuer: DID,
        val holder: DID,
        val holderPrv: PrivateKey,
        val issuerPrv: PrivateKey,
        val subject: String,
        val claims: PresentationClaims,
        val domain: String = UUID.randomUUID().toString(),
        val challenge: String = UUID.randomUUID().toString()
    )

    private fun correctHttpResponseFetchRevocationRegistry(): HttpResponse {
        val response = """
            {
                "@context": [
                    "https://www.w3.org/2018/credentials/v1",
                    "https://w3id.org/vc/status-list/2021/v1"
                ],
                "type": [
                    "VerifiableCredential",
                    "StatusList2021Credential"
                ],
                "issuer": "did:prism:d6407754370022455313c4d870de48d38d638b905f3efa665fe917ba9c2a73c6",
                "id": "http://192.168.68.113:8000/cloud-agent/credential-status/f054ca9e-34f1-4231-a52f-1a3221f023c3",
                "issuanceDate": 1718988530,
                "credentialSubject": {
                    "type": "StatusList2021",
                    "statusPurpose": "Revocation",
                    "encodedList": "H4sIAAAAAAAA_-3BMQEAAADCoPVPbQwfoAAAAAAAAAAAAAAAAAAAAIC3AYbSVKsAQAAA"
                },
                "proof": {
                    "type": "EcdsaSecp256k1Signature2019",
                    "proofPurpose": "assertionMethod",
                    "verificationMethod": "data:application/json;base64,eyJAY29udGV4dCI6WyJodHRwczovL3czaWQub3JnL3NlY3VyaXR5L3YxIl0sInR5cGUiOiJFY2RzYVNlY3AyNTZrMVZlcmlmaWNhdGlvbktleTIwMTkiLCJwdWJsaWNLZXlKd2siOnsiY3J2Ijoic2VjcDI1NmsxIiwia2V5X29wcyI6WyJ2ZXJpZnkiXSwia3R5IjoiRUMiLCJ4IjoiSmNQS2xyc0dwU3NLb0RmOExpNzMwUVZMcUltOThmN211Rnc3d25fZ0pnbz0iLCJ5IjoiR3ZtWjJ4eE1vT1Y0cU9VajZXV3Fael9Kd2M4NEk5Rzlpc2hxMHAyVE83TT0ifX0=",
                    "created": "2024-06-21T16:48:50.744054087Z",
                    "jws": "eyJiNjQiOmZhbHNlLCJjcml0IjpbImI2NCJdLCJhbGciOiJFUzI1NksifQ..5duK6WkHqGEFIlbu-CfFrkHR-ALu6LfpADFwQT7OXWhcFVjpCgfOHC5plJuQ7IBwCVJo0myfRHSS15O1-7c9Gw"
                }
            }
            """
        return HttpResponse(status = HttpStatusCode.OK.value, response)
    }
}
