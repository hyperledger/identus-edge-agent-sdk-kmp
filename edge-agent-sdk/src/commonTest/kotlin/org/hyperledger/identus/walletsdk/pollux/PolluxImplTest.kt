package org.hyperledger.identus.walletsdk.pollux

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.ECDSASigner
import com.nimbusds.jose.crypto.bc.BouncyCastleProviderSingleton
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import io.ktor.http.HttpStatusCode
import java.text.SimpleDateFormat
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.serialization.kotlinx.json.json
import java.util.*
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import org.hyperledger.identus.apollo.derivation.MnemonicHelper
import org.hyperledger.identus.walletsdk.apollo.ApolloImpl
import org.hyperledger.identus.walletsdk.apollo.utils.Ed25519KeyPair
import org.hyperledger.identus.walletsdk.apollo.utils.Secp256k1KeyPair
import org.hyperledger.identus.walletsdk.castor.CastorImpl
import org.didcommx.didcomm.common.Typ
import org.hyperledger.identus.walletsdk.domain.buildingblocks.Apollo
import org.hyperledger.identus.walletsdk.domain.buildingblocks.Castor
import org.hyperledger.identus.walletsdk.domain.models.CredentialType
import org.hyperledger.identus.walletsdk.domain.models.Curve
import org.hyperledger.identus.walletsdk.domain.models.DID
import org.hyperledger.identus.walletsdk.domain.models.InputFieldFilter
import org.hyperledger.identus.walletsdk.domain.models.JWTVerifiableCredential
import org.hyperledger.identus.walletsdk.domain.models.KeyCurve
import org.hyperledger.identus.walletsdk.domain.models.PolluxError
import org.hyperledger.identus.walletsdk.domain.models.PresentationClaims
import org.hyperledger.identus.walletsdk.domain.models.Seed
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.PrivateKey
import org.hyperledger.identus.walletsdk.edgeagent.protocols.proofOfPresentation.PresentationDefinitionRequest
import org.hyperledger.identus.walletsdk.edgeagent.protocols.proofOfPresentation.PresentationOptions
import org.hyperledger.identus.walletsdk.edgeagent.protocols.proofOfPresentation.PresentationSubmission
import org.hyperledger.identus.walletsdk.edgeagent.protocols.proofOfPresentation.PresentationSubmissionOptionsJWT
import org.hyperledger.identus.walletsdk.logger.PrismLogger
import org.hyperledger.identus.walletsdk.pollux.models.AnonCredential
import org.hyperledger.identus.walletsdk.pollux.models.JWTCredential
import org.mockito.kotlin.mock
import java.text.SimpleDateFormat
import java.util.*
import org.hyperledger.identus.walletsdk.domain.models.Api
import org.hyperledger.identus.walletsdk.domain.models.ApiImpl
import org.hyperledger.identus.walletsdk.domain.models.HttpResponse
import org.hyperledger.identus.walletsdk.domain.models.httpClient
import org.hyperledger.identus.walletsdk.edgeagent.shared.KeyValue
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.spy
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class PolluxImplTest {

    lateinit var pollux: PolluxImpl
    lateinit var apollo: Apollo

    @Mock
    lateinit var castorMock: Castor

    @Mock
    lateinit var apiMock: Api

    @BeforeTest
    fun setup() {
        MockitoAnnotations.openMocks(this)
        apollo = ApolloImpl()
        pollux = PolluxImpl(apollo, castorMock, apiMock)
    }

    @Test
    fun testGetSchema_whenAnoncred_thenSchemaCorrect() = runTest {
        val response =
            "{\"name\":\"Schema name\",\"version\":\"1.1\",\"attrNames\":[\"name\",\"surname\"],\"issuerId\":\"did:prism:604ba1764ab89993f9a74625cc4f3e04737919639293eb382cc7adc53767f550\"}"
        val httpResponse = HttpResponse(status = HttpStatusCode.OK.value, response)

        `when`(
            apiMock.request(
                httpMethod = HttpMethod.Get.value,
                url = "",
                urlParameters = emptyArray(),
                httpHeaders = arrayOf(KeyValue(HttpHeaders.ContentType, Typ.Encrypted.typ)),
                body = null
            )
        ).thenReturn(httpResponse)

        val schema = pollux.getSchema("")
        val attrNames = listOf("name", "surname")
        assertEquals("Schema name", schema.name)
        assertEquals("1.1", schema.version)
        assertEquals(attrNames, schema.attrNames)
        assertEquals(
            "did:prism:604ba1764ab89993f9a74625cc4f3e04737919639293eb382cc7adc53767f550",
            schema.issuerId
        )
    }

    @Test
    fun testCreatePresentationDefinitionRequest_whenOptionsNoJWT_thenExceptionThrown() = runTest {
        assertFailsWith(PolluxError.InvalidJWTPresentationDefinitionError::class) {
            pollux.createPresentationDefinitionRequest(
                type = CredentialType.JWT,
                presentationClaims = PresentationClaims(
                    claims = mapOf()
                ),
                options = PresentationOptions(jwt = emptyArray(), domain = "", challenge = "")
            )
        }
    }

    @Test
    fun testCreatePresentationDefinitionRequest_whenAllCorrect_thenPresentationDefinitionRequestCorrect() =
        runTest {
            val definitionRequest = pollux.createPresentationDefinitionRequest(
                type = CredentialType.JWT,
                presentationClaims = PresentationClaims(
                    claims = mapOf(
                        "$.vc.credentialSubject.email" to InputFieldFilter(
                            type = "string",
                            value = "value"
                        )
                    )
                ),
                options = PresentationOptions(
                    name = "Testing",
                    purpose = "Test presentation definition",
                    jwt = arrayOf("EcdsaSecp256k1Signature2019"),
                    domain = "domain",
                    challenge = "challenge"
                )
            )

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
    fun testCreatePresentationSubmission_whenCredentialNotJWT_thenExceptionThrown() = runTest {
        val definitionJson = """
            {
                "presentation_definition": {
                    "id": "32f54163-7166-48f1-93d8-ff217bdb0653",
                    "input_descriptors": [
                        {
                            "id": "wa_driver_license",
                            "name": "Washington State Business License",
                            "purpose": "We can only allow licensed Washington State business representatives into the WA Business Conference",
                            "constraints": {
                                "fields": [
                                    {
                                        "path": [
                                            "$.credentialSubject.dateOfBirth",
                                            "$.credentialSubject.dob",
                                            "$.vc.credentialSubject.dateOfBirth",
                                            "$.vc.credentialSubject.dob"
                                        ]
                                    }
                                ]
                            }
                        }
                    ],
                    "format": {
                        "jwt": {
                            "alg": ["ES256K"]
                        }
                    }
                },
                "options": {
                    "domain": "domain",
                    "challenge": "challenge"
                }
            }
        """

        val presentationDefinitionRequest: PresentationDefinitionRequest =
            Json.decodeFromString(definitionJson)
        val credential = AnonCredential(
            schemaID = "",
            credentialDefinitionID = "",
            values = mapOf(),
            signatureJson = "",
            signatureCorrectnessProofJson = "",
            revocationRegistryId = null,
            revocationRegistryJson = null,
            witnessJson = "",
            json = ""
        )
        val secpKeyPair = generateSecp256k1KeyPair()

        assertFailsWith(PolluxError.CredentialTypeNotSupportedError::class) {
            pollux.createPresentationSubmission(
                presentationDefinitionRequest = presentationDefinitionRequest,
                credential = credential,
                privateKey = secpKeyPair.privateKey
            )
        }
    }

    @Test
    fun testCreatePresentationSubmission_whenPrivateKeyNotSecp256k1_thenExceptionThrown() =
        runTest {
            val definitionJson = """
                {
                    "presentation_definition": {
                        "id": "32f54163-7166-48f1-93d8-ff217bdb0653",
                        "input_descriptors": [
                            {
                                "id": "wa_driver_license",
                                "name": "Washington State Business License",
                                "purpose": "We can only allow licensed Washington State business representatives into the WA Business Conference",
                                "constraints": {
                                    "fields": [
                                        {
                                            "path": [
                                                "$.credentialSubject.dateOfBirth",
                                                "$.credentialSubject.dob",
                                                "$.vc.credentialSubject.dateOfBirth",
                                                "$.vc.credentialSubject.dob"
                                            ]
                                        }
                                    ]
                                }
                            }
                        ],
                        "format": {
                            "jwt": {
                                "alg": ["ES256K"]
                            }
                        }
                    },
                    "options": {
                        "domain": "domain",
                        "challenge": "challenge"
                    }
                }
            """

            val presentationDefinitionRequest: PresentationDefinitionRequest =
                Json.decodeFromString(definitionJson)
            val credential = JWTCredential.fromJwtString(
                "eyJhbGciOiJFUzI1NksifQ.eyJpc3MiOiJkaWQ6cHJpc206MjU3MTlhOTZiMTUxMjA3MTY5ODFhODQzMGFkMGNiOTY4ZGQ1MzQwNzM1OTNjOGNkM2YxZDI3YTY4MDRlYzUwZTpDcG9DQ3BjQ0Vsb0tCV3RsZVMweEVBSkNUd29KYzJWamNESTFObXN4RWlBRW9TQ241dHlEYTZZNnItSW1TcXBKOFkxbWo3SkMzX29VekUwTnl5RWlDQm9nc2dOYWVSZGNDUkdQbGU4MlZ2OXRKZk53bDZyZzZWY2hSM09xaGlWYlRhOFNXd29HWVhWMGFDMHhFQVJDVHdvSmMyVmpjREkxTm1zeEVpRE1rQmQ2RnRpb0prM1hPRnUtX2N5NVhtUi00dFVRMk5MR2lXOGFJU29ta1JvZzZTZGU5UHduRzBRMFNCVG1GU1REYlNLQnZJVjZDVExYcmpJSnR0ZUdJbUFTWEFvSGJXRnpkR1Z5TUJBQlFrOEtDWE5sWTNBeU5UWnJNUklnTzcxMG10MVdfaXhEeVFNM3hJczdUcGpMQ05PRFF4Z1ZoeDVzaGZLTlgxb2FJSFdQcnc3SVVLbGZpYlF0eDZKazRUU2pnY1dOT2ZjT3RVOUQ5UHVaN1Q5dCIsInN1YiI6ImRpZDpwcmlzbTpiZWVhNTIzNGFmNDY4MDQ3MTRkOGVhOGVjNzdiNjZjYzdmM2U4MTVjNjhhYmI0NzVmMjU0Y2Y5YzMwNjI2NzYzOkNzY0JDc1FCRW1RS0QyRjFkR2hsYm5ScFkyRjBhVzl1TUJBRVFrOEtDWE5sWTNBeU5UWnJNUklnZVNnLTJPTzFKZG5welVPQml0eklpY1hkZnplQWNUZldBTi1ZQ2V1Q2J5SWFJSlE0R1RJMzB0YVZpd2NoVDNlMG5MWEJTNDNCNGo5amxzbEtvMlpsZFh6akVsd0tCMjFoYzNSbGNqQVFBVUpQQ2dselpXTndNalUyYXpFU0lIa29QdGpqdFNYWjZjMURnWXJjeUluRjNYODNnSEUzMWdEZm1BbnJnbThpR2lDVU9Ca3lOOUxXbFlzSElVOTN0Snkxd1V1TndlSV9ZNWJKU3FObVpYVjg0dyIsIm5iZiI6MTY4NTYzMTk5NSwiZXhwIjoxNjg1NjM1NTk1LCJ2YyI6eyJjcmVkZW50aWFsU3ViamVjdCI6eyJhZGRpdGlvbmFsUHJvcDIiOiJUZXN0MyIsImlkIjoiZGlkOnByaXNtOmJlZWE1MjM0YWY0NjgwNDcxNGQ4ZWE4ZWM3N2I2NmNjN2YzZTgxNWM2OGFiYjQ3NWYyNTRjZjljMzA2MjY3NjM6Q3NjQkNzUUJFbVFLRDJGMWRHaGxiblJwWTJGMGFXOXVNQkFFUWs4S0NYTmxZM0F5TlRack1SSWdlU2ctMk9PMUpkbnB6VU9CaXR6SWljWGRmemVBY1RmV0FOLVlDZXVDYnlJYUlKUTRHVEkzMHRhVml3Y2hUM2UwbkxYQlM0M0I0ajlqbHNsS28yWmxkWHpqRWx3S0IyMWhjM1JsY2pBUUFVSlBDZ2x6WldOd01qVTJhekVTSUhrb1B0amp0U1haNmMxRGdZcmN5SW5GM1g4M2dIRTMxZ0RmbUFucmdtOGlHaUNVT0JreU45TFdsWXNISVU5M3RKeTF3VXVOd2VJX1k1YkpTcU5tWlhWODR3In0sInR5cGUiOlsiVmVyaWZpYWJsZUNyZWRlbnRpYWwiXSwiQGNvbnRleHQiOlsiaHR0cHM6XC9cL3d3dy53My5vcmdcLzIwMThcL2NyZWRlbnRpYWxzXC92MSJdfX0.x0SF17Y0VCDmt7HceOdTxfHlofsZmY18Rn6VQb0-r-k_Bm3hTi1-k2vkdjB25hdxyTCvxam-AkAP-Ag3Ahn5Ng"
            )
            val nonSecpKeyPair = Ed25519KeyPair.generateKeyPair()

            assertFailsWith(PolluxError.PrivateKeyTypeNotSupportedError::class) {
                pollux.createPresentationSubmission(
                    presentationDefinitionRequest = presentationDefinitionRequest,
                    credential = credential,
                    privateKey = nonSecpKeyPair.privateKey
                )
            }
        }

    @Test
    fun testCreatePresentationSubmission_whenAllCorrect_thenPresentationSubmissionProofWellFormed() =
        runTest {
            val loggerMock = mock<PrismLogger>()
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

            val vtc = createVerificationTestCase(
                VerificationTestCase(
                    issuer = issuerDID,
                    holder = holderDID,
                    issuerPrv = issuerKeyPair.privateKey,
                    holderPrv = holderKeyPair.privateKey,
                    subject = """{"course": "Identus Training course Certification 2024"} """,
                    claims = PresentationClaims(
                        claims = mapOf(
                            "course" to InputFieldFilter(
                                type = "string",
                                pattern = "Identus Training course Certification 2024"
                            )
                        )
                    ),
                )
            )
            val presentationDefinitionRequest = vtc.first
            val presentationSubmissionProof = vtc.second

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

        val vtc = createVerificationTestCase(
            VerificationTestCase(
                issuer = issuerDID,
                holder = holderDID,
                issuerPrv = issuerKeyPair.privateKey,
                holderPrv = holderKeyPair.privateKey,
                subject = """{"course": "Identus Training course Certification 2024"} """,
                claims = PresentationClaims(
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
                presentationSubmission = vtc.second,
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

            val vtc = createVerificationTestCase(
                VerificationTestCase(
                    issuer = issuerDID,
                    holder = holderDID,
                    issuerPrv = issuerKeyPair.privateKey,
                    holderPrv = holderKeyPair.privateKey,
                    subject = """{"course": "Identus Training course Certification 2023"} """,
                    claims = PresentationClaims(
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
                    presentationSubmission = vtc.second,
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

        val vtc = createVerificationTestCase(
            VerificationTestCase(
                issuer = issuerDID,
                holder = holderDID,
                issuerPrv = issuerKeyPair.privateKey,
                holderPrv = holderKeyPair.privateKey,
                subject = """{"course": "Identus Training course Certification 2024"} """,
                claims = PresentationClaims(
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
            presentationSubmission = vtc.second,
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

        val vtc = createVerificationTestCase(
            VerificationTestCase(
                issuer = issuerDID,
                holder = holderDID,
                issuerPrv = issuerKeyPair.privateKey,
                holderPrv = holderKeyPair.privateKey,
                subject = """{"course": "Identus Training course Certification 2024"} """,
                claims = PresentationClaims(
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

        val descriptorPath = DescriptorPath(Json.encodeToJsonElement(presentationSubmission))
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

        val vtc = createVerificationTestCase(
            VerificationTestCase(
                issuer = issuerDID,
                holder = holderDID,
                issuerPrv = issuerKeyPair.privateKey,
                holderPrv = holderKeyPair.privateKey,
                subject = """{"course": "Identus Training course Certification 2024"} """,
                claims = PresentationClaims(
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
            presentationSubmission = vtc.second,
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

        val vtc = createVerificationTestCase(
            VerificationTestCase(
                issuer = issuerDID,
                holder = holderDID,
                issuerPrv = issuerKeyPair.privateKey,
                holderPrv = holderKeyPair.privateKey,
                subject = """{"course": "Identus Training course Certification 2024"} """,
                claims = PresentationClaims(
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
            presentationSubmission = vtc.second,
            options = PresentationSubmissionOptionsJWT(vtc.first)
        )
        assertTrue(isVerified)
    }

    @Test
    fun testDescriptorPath_whenClaimsAreValue_thenValidatedOk() = runTest {
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

        val vtc = createVerificationTestCase(
            VerificationTestCase(
                issuer = issuerDID,
                holder = holderDID,
                issuerPrv = issuerKeyPair.privateKey,
                holderPrv = holderKeyPair.privateKey,
                subject = """{"course": "Identus Training course Certification 2024"} """,
                claims = PresentationClaims(
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
            presentationSubmission = vtc.second,
            options = PresentationSubmissionOptionsJWT(vtc.first)
        )
        assertTrue(isVerified)
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

    @OptIn(ExperimentalSerializationApi::class)
    private suspend fun createVerificationTestCase(testCaseOptions: VerificationTestCase): Triple<PresentationDefinitionRequest, PresentationSubmission, String> {
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
                  "credentialSubject": ${testCaseOptions.subject}}"""
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
            presentationClaims = PresentationClaims(
                issuer = testCaseOptions.issuer.toString(),
                claims = testCaseOptions.claims.claims
            ),
            options = PresentationOptions(domain = "domain", challenge = testCaseOptions.challenge)
        )

        val presentationSubmission = pollux.createPresentationSubmission(
            presentationDefinitionRequest = presentationDefinition,
            credential = jwtCredential,
            privateKey = testCaseOptions.holderPrv
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

    fun testIsCredentialRevoked_when_then() = runTest {
        val response = """{
                "proof": {
                    "type": "EcdsaSecp256k1Signature2019",
                    "proofPurpose": "assertionMethod",
                    "verificationMethod": "data:application/json;base64,eyJAY29udGV4dCI6WyJodHRwczovL3czaWQub3JnL3NlY3VyaXR5L3YxIl0sInR5cGUiOiJFY2RzYVNlY3AyNTZrMVZlcmlmaWNhdGlvbktleTIwMTkiLCJwdWJsaWNLZXlKd2siOnsiY3J2Ijoic2VjcDI1NmsxIiwia2V5X29wcyI6WyJ2ZXJpZnkiXSwia3R5IjoiRUMiLCJ4IjoiVFlCZ21sM1RpUWRSX1lRRDFoSXVOTzhiUnluU0otcmxQcWFVd3JXa3EtRT0iLCJ5IjoiVjBnVFlBM0xhbFd3Q3hPZHlqb2ZoR2JkYVFEd3EwQXdCblNodFJLXzNYZz0ifX0=",
                    "created": "2024-06-14T10:56:59.948091Z",
                    "jws": "eyJiNjQiOmZhbHNlLCJjcml0IjpbImI2NCJdLCJhbGciOiJFUzI1NksifQ..Q1mj3jMf5DWK83E55r6vNUPpsYYgclgwYoNFBSYBzA5x6fI_2cPHJsXECnQlG1XMj2ifldngpJXegTpwe3Fgwg"
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

        val credential = JWTCredential(
            "eyJhbGciOiJFUzI1NksifQ.eyJpc3MiOiJkaWQ6cHJpc206ZTAyZTgwOTlkNTAzNTEzNDVjNWRkODMxYTllOTExMmIzOTRhODVkMDA2NGEyZWI1OTQyOTA4MDczNGExNTliNjpDcmtCQ3JZQkVqb0tCbUYxZEdndE1SQUVTaTRLQ1hObFkzQXlOVFpyTVJJaEF1Vlljb3JmV25MMGZZdEE1dmdKSzRfLW9iM2JVRGMtdzJVT0hkTzNRRXZxRWpzS0IybHpjM1ZsTFRFUUFrb3VDZ2x6WldOd01qVTJhekVTSVFMQ3U5Tm50cXVwQmotME5DZE1BNzV6UmVCZXlhQ0pPMWFHWWVQNEJNUUhWQkk3Q2dkdFlYTjBaWEl3RUFGS0xnb0pjMlZqY0RJMU5tc3hFaUVET1dndlF4NnZSdTZ3VWI0RlljSnVhRUNqOUJqUE1KdlJwOEx3TTYxaEVUNCIsInN1YiI6ImRpZDpwcmlzbTpiZDgxZmY1NDQzNDJjMTAwNDZkZmE0YmEyOTVkNWIzNmU0Y2ZlNWE3ZWIxMjBlMTBlZTVjMjQ4NzAwNjUxMDA5OkNvVUJDb0lCRWpzS0IyMWhjM1JsY2pBUUFVb3VDZ2x6WldOd01qVTJhekVTSVFQdjVQNXl5Z3Jad2FKbFl6bDU5bTJIQURLVFhVTFBzUmUwa2dlRUh2dExnQkpEQ2c5aGRYUm9aVzUwYVdOaGRHbHZiakFRQkVvdUNnbHpaV053TWpVMmF6RVNJUVB2NVA1eXlnclp3YUpsWXpsNTltMkhBREtUWFVMUHNSZTBrZ2VFSHZ0TGdBIiwibmJmIjoxNzE1MDAwNjc0LCJleHAiOjE3MTg2MDA2NzQsInZjIjp7ImNyZWRlbnRpYWxTdWJqZWN0Ijp7ImVtYWlsQWRkcmVzcyI6ImNyaXN0aWFuLmNhc3Ryb0Bpb2hrLmlvIiwiaWQiOiJkaWQ6cHJpc206YmQ4MWZmNTQ0MzQyYzEwMDQ2ZGZhNGJhMjk1ZDViMzZlNGNmZTVhN2ViMTIwZTEwZWU1YzI0ODcwMDY1MTAwOTpDb1VCQ29JQkVqc0tCMjFoYzNSbGNqQVFBVW91Q2dselpXTndNalUyYXpFU0lRUHY1UDV5eWdyWndhSmxZemw1OW0ySEFES1RYVUxQc1JlMGtnZUVIdnRMZ0JKRENnOWhkWFJvWlc1MGFXTmhkR2x2YmpBUUJFb3VDZ2x6WldOd01qVTJhekVTSVFQdjVQNXl5Z3Jad2FKbFl6bDU5bTJIQURLVFhVTFBzUmUwa2dlRUh2dExnQSJ9LCJ0eXBlIjpbIlZlcmlmaWFibGVDcmVkZW50aWFsIl0sIkBjb250ZXh0IjpbImh0dHBzOlwvXC93d3cudzMub3JnXC8yMDE4XC9jcmVkZW50aWFsc1wvdjEiXSwiY3JlZGVudGlhbFN0YXR1cyI6eyJzdGF0dXNQdXJwb3NlIjoiUmV2b2NhdGlvbiIsInN0YXR1c0xpc3RJbmRleCI6MjUsImlkIjoiaHR0cDpcL1wvMTAuOTEuMTAwLjEyNjo4MDAwXC9wcmlzbS1hZ2VudFwvY3JlZGVudGlhbC1zdGF0dXNcLzUxNGU4NTI4LTRiMzgtNDc3YS1iMGU0LTMyNGJiZTIyMDQ2NCMyNSIsInR5cGUiOiJTdGF0dXNMaXN0MjAyMUVudHJ5Iiwic3RhdHVzTGlzdENyZWRlbnRpYWwiOiJodHRwOlwvXC8xMC45MS4xMDAuMTI2OjgwMDBcL3ByaXNtLWFnZW50XC9jcmVkZW50aWFsLXN0YXR1c1wvNTE0ZTg1MjgtNGIzOC00NzdhLWIwZTQtMzI0YmJlMjIwNDY0In19fQ.5OmmL5tdcRKugiHVt01PJUhp9r22zgMPPALUOB41g_1_BPHE3ezqJ2QhSmScU_EOGYcKksHftdrvfoND65nSjw"
        )
        // TODO:  Add usage of this to presentation request
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

        val credential = JWTCredential(
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

        val credential = JWTCredential(
            "eyJhbGciOiJFUzI1NksifQ.eyJpc3MiOiJkaWQ6cHJpc206ZTAyZTgwOTlkNTAzNTEzNDVjNWRkODMxYTllOTExMmIzOTRhODVkMDA2NGEyZWI1OTQyOTA4MDczNGExNTliNjpDcmtCQ3JZQkVqb0tCbUYxZEdndE1SQUVTaTRLQ1hObFkzQXlOVFpyTVJJaEF1Vlljb3JmV25MMGZZdEE1dmdKSzRfLW9iM2JVRGMtdzJVT0hkTzNRRXZxRWpzS0IybHpjM1ZsTFRFUUFrb3VDZ2x6WldOd01qVTJhekVTSVFMQ3U5Tm50cXVwQmotME5DZE1BNzV6UmVCZXlhQ0pPMWFHWWVQNEJNUUhWQkk3Q2dkdFlYTjBaWEl3RUFGS0xnb0pjMlZqY0RJMU5tc3hFaUVET1dndlF4NnZSdTZ3VWI0RlljSnVhRUNqOUJqUE1KdlJwOEx3TTYxaEVUNCIsInN1YiI6ImRpZDpwcmlzbTpiZDgxZmY1NDQzNDJjMTAwNDZkZmE0YmEyOTVkNWIzNmU0Y2ZlNWE3ZWIxMjBlMTBlZTVjMjQ4NzAwNjUxMDA5OkNvVUJDb0lCRWpzS0IyMWhjM1JsY2pBUUFVb3VDZ2x6WldOd01qVTJhekVTSVFQdjVQNXl5Z3Jad2FKbFl6bDU5bTJIQURLVFhVTFBzUmUwa2dlRUh2dExnQkpEQ2c5aGRYUm9aVzUwYVdOaGRHbHZiakFRQkVvdUNnbHpaV053TWpVMmF6RVNJUVB2NVA1eXlnclp3YUpsWXpsNTltMkhBREtUWFVMUHNSZTBrZ2VFSHZ0TGdBIiwibmJmIjoxNzE1MDAwNjc0LCJleHAiOjE3MTg2MDA2NzQsInZjIjp7ImNyZWRlbnRpYWxTdWJqZWN0Ijp7ImVtYWlsQWRkcmVzcyI6ImNyaXN0aWFuLmNhc3Ryb0Bpb2hrLmlvIiwiaWQiOiJkaWQ6cHJpc206YmQ4MWZmNTQ0MzQyYzEwMDQ2ZGZhNGJhMjk1ZDViMzZlNGNmZTVhN2ViMTIwZTEwZWU1YzI0ODcwMDY1MTAwOTpDb1VCQ29JQkVqc0tCMjFoYzNSbGNqQVFBVW91Q2dselpXTndNalUyYXpFU0lRUHY1UDV5eWdyWndhSmxZemw1OW0ySEFES1RYVUxQc1JlMGtnZUVIdnRMZ0JKRENnOWhkWFJvWlc1MGFXTmhkR2x2YmpBUUJFb3VDZ2x6WldOd01qVTJhekVTSVFQdjVQNXl5Z3Jad2FKbFl6bDU5bTJIQURLVFhVTFBzUmUwa2dlRUh2dExnQSJ9LCJ0eXBlIjpbIlZlcmlmaWFibGVDcmVkZW50aWFsIl0sIkBjb250ZXh0IjpbImh0dHBzOlwvXC93d3cudzMub3JnXC8yMDE4XC9jcmVkZW50aWFsc1wvdjEiXSwiY3JlZGVudGlhbFN0YXR1cyI6eyJzdGF0dXNQdXJwb3NlIjoiUmV2b2NhdGlvbiIsInN0YXR1c0xpc3RJbmRleCI6MjUsImlkIjoiaHR0cDpcL1wvMTAuOTEuMTAwLjEyNjo4MDAwXC9wcmlzbS1hZ2VudFwvY3JlZGVudGlhbC1zdGF0dXNcLzUxNGU4NTI4LTRiMzgtNDc3YS1iMGU0LTMyNGJiZTIyMDQ2NCMyNSIsInR5cGUiOiJTdGF0dXNMaXN0MjAyMUVudHJ5Iiwic3RhdHVzTGlzdENyZWRlbnRpYWwiOiJodHRwOlwvXC8xMC45MS4xMDAuMTI2OjgwMDBcL3ByaXNtLWFnZW50XC9jcmVkZW50aWFsLXN0YXR1c1wvNTE0ZTg1MjgtNGIzOC00NzdhLWIwZTQtMzI0YmJlMjIwNDY0In19fQ.5OmmL5tdcRKugiHVt01PJUhp9r22zgMPPALUOB41g_1_BPHE3ezqJ2QhSmScU_EOGYcKksHftdrvfoND65nSjw"
        )
        assertFailsWith<PolluxError.RevocationRegistryJsonMissingFieldError> {
            pollux.isCredentialRevoked(credential)
        }
    }

    @Test
    fun test() {
        val credential = JWTCredential(
            "eyJhbGciOiJFUzI1NksifQ.eyJpc3MiOiJkaWQ6cHJpc206ZTAyZTgwOTlkNTAzNTEzNDVjNWRkODMxYTllOTExMmIzOTRhODVkMDA2NGEyZWI1OTQyOTA4MDczNGExNTliNjpDcmtCQ3JZQkVqb0tCbUYxZEdndE1SQUVTaTRLQ1hObFkzQXlOVFpyTVJJaEF1Vlljb3JmV25MMGZZdEE1dmdKSzRfLW9iM2JVRGMtdzJVT0hkTzNRRXZxRWpzS0IybHpjM1ZsTFRFUUFrb3VDZ2x6WldOd01qVTJhekVTSVFMQ3U5Tm50cXVwQmotME5DZE1BNzV6UmVCZXlhQ0pPMWFHWWVQNEJNUUhWQkk3Q2dkdFlYTjBaWEl3RUFGS0xnb0pjMlZqY0RJMU5tc3hFaUVET1dndlF4NnZSdTZ3VWI0RlljSnVhRUNqOUJqUE1KdlJwOEx3TTYxaEVUNCIsInN1YiI6ImRpZDpwcmlzbTpiZDgxZmY1NDQzNDJjMTAwNDZkZmE0YmEyOTVkNWIzNmU0Y2ZlNWE3ZWIxMjBlMTBlZTVjMjQ4NzAwNjUxMDA5OkNvVUJDb0lCRWpzS0IyMWhjM1JsY2pBUUFVb3VDZ2x6WldOd01qVTJhekVTSVFQdjVQNXl5Z3Jad2FKbFl6bDU5bTJIQURLVFhVTFBzUmUwa2dlRUh2dExnQkpEQ2c5aGRYUm9aVzUwYVdOaGRHbHZiakFRQkVvdUNnbHpaV053TWpVMmF6RVNJUVB2NVA1eXlnclp3YUpsWXpsNTltMkhBREtUWFVMUHNSZTBrZ2VFSHZ0TGdBIiwibmJmIjoxNzE1MDAwNjc0LCJleHAiOjE3MTg2MDA2NzQsInZjIjp7ImNyZWRlbnRpYWxTdWJqZWN0Ijp7ImVtYWlsQWRkcmVzcyI6ImNyaXN0aWFuLmNhc3Ryb0Bpb2hrLmlvIiwiaWQiOiJkaWQ6cHJpc206YmQ4MWZmNTQ0MzQyYzEwMDQ2ZGZhNGJhMjk1ZDViMzZlNGNmZTVhN2ViMTIwZTEwZWU1YzI0ODcwMDY1MTAwOTpDb1VCQ29JQkVqc0tCMjFoYzNSbGNqQVFBVW91Q2dselpXTndNalUyYXpFU0lRUHY1UDV5eWdyWndhSmxZemw1OW0ySEFES1RYVUxQc1JlMGtnZUVIdnRMZ0JKRENnOWhkWFJvWlc1MGFXTmhkR2x2YmpBUUJFb3VDZ2x6WldOd01qVTJhekVTSVFQdjVQNXl5Z3Jad2FKbFl6bDU5bTJIQURLVFhVTFBzUmUwa2dlRUh2dExnQSJ9LCJ0eXBlIjpbIlZlcmlmaWFibGVDcmVkZW50aWFsIl0sIkBjb250ZXh0IjpbImh0dHBzOlwvXC93d3cudzMub3JnXC8yMDE4XC9jcmVkZW50aWFsc1wvdjEiXSwiY3JlZGVudGlhbFN0YXR1cyI6eyJzdGF0dXNQdXJwb3NlIjoiUmV2b2NhdGlvbiIsInN0YXR1c0xpc3RJbmRleCI6MjUsImlkIjoiaHR0cDpcL1wvMTAuOTEuMTAwLjEyNjo4MDAwXC9wcmlzbS1hZ2VudFwvY3JlZGVudGlhbC1zdGF0dXNcLzUxNGU4NTI4LTRiMzgtNDc3YS1iMGU0LTMyNGJiZTIyMDQ2NCMyNSIsInR5cGUiOiJTdGF0dXNMaXN0MjAyMUVudHJ5Iiwic3RhdHVzTGlzdENyZWRlbnRpYWwiOiJodHRwOlwvXC8xMC45MS4xMDAuMTI2OjgwMDBcL3ByaXNtLWFnZW50XC9jcmVkZW50aWFsLXN0YXR1c1wvNTE0ZTg1MjgtNGIzOC00NzdhLWIwZTQtMzI0YmJlMjIwNDY0In19fQ.5OmmL5tdcRKugiHVt01PJUhp9r22zgMPPALUOB41g_1_BPHE3ezqJ2QhSmScU_EOGYcKksHftdrvfoND65nSjw"
        )
        val storableCredential = credential.toStorableCredential()

        val fromStorable = storableCredential.fromStorableCredential()
    }
}
