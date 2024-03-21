package org.hyperledger.identus.walletsdk.pollux

import io.iohk.atala.prism.apollo.derivation.MnemonicHelper
import org.hyperledger.identus.walletsdk.edgeagent.protocols.proofOfPresentation.PresentationDefinitionRequest
import io.iohk.atala.prism.walletsdk.prismagent.protocols.proofOfPresentation.PresentationOptions
import io.iohk.atala.prism.walletsdk.prismagent.protocols.proofOfPresentation.W3cCredentialSubmission
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.hyperledger.identus.walletsdk.apollo.utils.Ed25519KeyPair
import org.hyperledger.identus.walletsdk.apollo.utils.Secp256k1KeyPair
import org.hyperledger.identus.walletsdk.apollo.utils.Secp256k1PrivateKey
import org.hyperledger.identus.walletsdk.domain.models.CredentialType
import org.hyperledger.identus.walletsdk.domain.models.Curve
import org.hyperledger.identus.walletsdk.domain.models.DID
import org.hyperledger.identus.walletsdk.domain.models.KeyCurve
import org.hyperledger.identus.walletsdk.domain.models.PolluxError
import org.hyperledger.identus.walletsdk.domain.models.Seed
import org.hyperledger.identus.walletsdk.edgeagent.CastorMock
import org.hyperledger.identus.walletsdk.edgeagent.protocols.proofOfPresentation.ProofTypes
import org.hyperledger.identus.walletsdk.mercury.ApiMock
import org.hyperledger.identus.walletsdk.pollux.models.AnonCredential
import org.hyperledger.identus.walletsdk.pollux.models.JWTCredential
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PolluxImplTest {

    lateinit var pollux: PolluxImpl
    lateinit var castorMock: CastorMock
    lateinit var apiMock: ApiMock

    @BeforeTest
    fun setup() {
        castorMock = CastorMock()
        val json =
            "{\"name\":\"Schema name\",\"version\":\"1.1\",\"attrNames\":[\"name\",\"surname\"],\"issuerId\":\"did:prism:604ba1764ab89993f9a74625cc4f3e04737919639293eb382cc7adc53767f550\"}"
        apiMock = ApiMock(HttpStatusCode.OK, json)
        pollux = PolluxImpl(castorMock, apiMock)
    }

    @Test
    fun testGetSchema_whenAnoncred_thenSchemaCorrect() = runTest {
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
                proofs = arrayOf(),
                options = PresentationOptions()
            )
        }
    }

    @Test
    fun testCreatePresentationDefinitionRequest_whenAllCorrect_thenPresentationDefinitionRequestCorrect() = runTest {
        val proofType = ProofTypes(
            schema = "schema",
            requiredFields = arrayOf("$.vc.credentialSubject.dateOfBirth"),
            trustIssuers = arrayOf("$.vc.issuer")
        )

        val definitionRequest = pollux.createPresentationDefinitionRequest(
            type = CredentialType.JWT,
            proofs = arrayOf(proofType),
            options = PresentationOptions(
                name = "Testing",
                purpose = "Test presentation definition",
                challenge = "1f44d55f-f161-4938-a659-f8026467f126",
                domain = "domain",
                jwtVpAlg = arrayOf("EcdsaSecp256k1Signature2019")
            )
        )

        assertEquals("domain", definitionRequest.domain)
        assertEquals("1f44d55f-f161-4938-a659-f8026467f126", definitionRequest.challenge)
        assertEquals(1, definitionRequest.presentationDefinitionBody.inputDescriptors.size)
        assertEquals(1, definitionRequest.presentationDefinitionBody.inputDescriptors.first().constraints.fields?.size)
        assertEquals(
            2,
            definitionRequest.presentationDefinitionBody.inputDescriptors.first().constraints.fields?.first()?.path?.size
        )
        assertEquals(
            "Testing",
            definitionRequest.presentationDefinitionBody.inputDescriptors.first().name
        )
        assertEquals(
            "Test presentation definition",
            definitionRequest.presentationDefinitionBody.inputDescriptors.first().purpose
        )
    }

    @Test
    fun testCreatePresentationSubmission_whenCredentialNotJWT_thenExceptionThrown() = runTest {
        val definitionJson =
            "{\"presentation_definition\": {\"id\": \"32f54163-7166-48f1-93d8-ff217bdb0653\", \"input_descriptors\": [{\"id\": \"wa_driver_license\", \"name\": \"Washington State Business License\", \"purpose\": \"We can only allow licensed Washington State business representatives into the WA Business Conference\", \"constraints\": {\"fields\": [{\"path\": [\"\$.credentialSubject.dateOfBirth\", \"\$.credentialSubject.dob\", \"\$.vc.credentialSubject.dateOfBirth\", \"\$.vc.credentialSubject.dob\"]}]}}],\"format\": { \"jwt_vp\": { \"alg\": [\"EdDSA\", \"ES256K\"]}}},\"challenge\": \"1f44d55f-f161-4938-a659-f8026467f126\"}"
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
                did = DID("did", "test", "123"),
                privateKey = secpKeyPair.privateKey
            )
        }
    }

    @Test
    fun testCreatePresentationSubmission_whenPrivateKeyNotSecp256k1_thenExceptionThrown() =
        runTest {
            val definitionJson =
                "{\"presentation_definition\": {\"id\": \"32f54163-7166-48f1-93d8-ff217bdb0653\", \"input_descriptors\": [{\"id\": \"wa_driver_license\", \"name\": \"Washington State Business License\", \"purpose\": \"We can only allow licensed Washington State business representatives into the WA Business Conference\", \"constraints\": {\"fields\": [{\"path\": [\"\$.credentialSubject.dateOfBirth\", \"\$.credentialSubject.dob\", \"\$.vc.credentialSubject.dateOfBirth\", \"\$.vc.credentialSubject.dob\"]}]}}],\"format\": { \"jwt_vp\": { \"alg\": [\"EdDSA\", \"ES256K\"]}}},\"challenge\": \"1f44d55f-f161-4938-a659-f8026467f126\"}"
            val presentationDefinitionRequest: PresentationDefinitionRequest =
                Json.decodeFromString(definitionJson)
            val credential = JWTCredential(
                "eyJhbGciOiJFUzI1NksifQ.eyJpc3MiOiJkaWQ6cHJpc206MjU3MTlhOTZiMTUxMjA3MTY5ODFhODQzMGFkMGNiOTY4ZGQ1MzQwNzM1OTNjOGNkM2YxZDI3YTY4MDRlYzUwZTpDcG9DQ3BjQ0Vsb0tCV3RsZVMweEVBSkNUd29KYzJWamNESTFObXN4RWlBRW9TQ241dHlEYTZZNnItSW1TcXBKOFkxbWo3SkMzX29VekUwTnl5RWlDQm9nc2dOYWVSZGNDUkdQbGU4MlZ2OXRKZk53bDZyZzZWY2hSM09xaGlWYlRhOFNXd29HWVhWMGFDMHhFQVJDVHdvSmMyVmpjREkxTm1zeEVpRE1rQmQ2RnRpb0prM1hPRnUtX2N5NVhtUi00dFVRMk5MR2lXOGFJU29ta1JvZzZTZGU5UHduRzBRMFNCVG1GU1REYlNLQnZJVjZDVExYcmpJSnR0ZUdJbUFTWEFvSGJXRnpkR1Z5TUJBQlFrOEtDWE5sWTNBeU5UWnJNUklnTzcxMG10MVdfaXhEeVFNM3hJczdUcGpMQ05PRFF4Z1ZoeDVzaGZLTlgxb2FJSFdQcnc3SVVLbGZpYlF0eDZKazRUU2pnY1dOT2ZjT3RVOUQ5UHVaN1Q5dCIsInN1YiI6ImRpZDpwcmlzbTpiZWVhNTIzNGFmNDY4MDQ3MTRkOGVhOGVjNzdiNjZjYzdmM2U4MTVjNjhhYmI0NzVmMjU0Y2Y5YzMwNjI2NzYzOkNzY0JDc1FCRW1RS0QyRjFkR2hsYm5ScFkyRjBhVzl1TUJBRVFrOEtDWE5sWTNBeU5UWnJNUklnZVNnLTJPTzFKZG5welVPQml0eklpY1hkZnplQWNUZldBTi1ZQ2V1Q2J5SWFJSlE0R1RJMzB0YVZpd2NoVDNlMG5MWEJTNDNCNGo5amxzbEtvMlpsZFh6akVsd0tCMjFoYzNSbGNqQVFBVUpQQ2dselpXTndNalUyYXpFU0lIa29QdGpqdFNYWjZjMURnWXJjeUluRjNYODNnSEUzMWdEZm1BbnJnbThpR2lDVU9Ca3lOOUxXbFlzSElVOTN0Snkxd1V1TndlSV9ZNWJKU3FObVpYVjg0dyIsIm5iZiI6MTY4NTYzMTk5NSwiZXhwIjoxNjg1NjM1NTk1LCJ2YyI6eyJjcmVkZW50aWFsU3ViamVjdCI6eyJhZGRpdGlvbmFsUHJvcDIiOiJUZXN0MyIsImlkIjoiZGlkOnByaXNtOmJlZWE1MjM0YWY0NjgwNDcxNGQ4ZWE4ZWM3N2I2NmNjN2YzZTgxNWM2OGFiYjQ3NWYyNTRjZjljMzA2MjY3NjM6Q3NjQkNzUUJFbVFLRDJGMWRHaGxiblJwWTJGMGFXOXVNQkFFUWs4S0NYTmxZM0F5TlRack1SSWdlU2ctMk9PMUpkbnB6VU9CaXR6SWljWGRmemVBY1RmV0FOLVlDZXVDYnlJYUlKUTRHVEkzMHRhVml3Y2hUM2UwbkxYQlM0M0I0ajlqbHNsS28yWmxkWHpqRWx3S0IyMWhjM1JsY2pBUUFVSlBDZ2x6WldOd01qVTJhekVTSUhrb1B0amp0U1haNmMxRGdZcmN5SW5GM1g4M2dIRTMxZ0RmbUFucmdtOGlHaUNVT0JreU45TFdsWXNISVU5M3RKeTF3VXVOd2VJX1k1YkpTcU5tWlhWODR3In0sInR5cGUiOlsiVmVyaWZpYWJsZUNyZWRlbnRpYWwiXSwiQGNvbnRleHQiOlsiaHR0cHM6XC9cL3d3dy53My5vcmdcLzIwMThcL2NyZWRlbnRpYWxzXC92MSJdfX0.x0SF17Y0VCDmt7HceOdTxfHlofsZmY18Rn6VQb0-r-k_Bm3hTi1-k2vkdjB25hdxyTCvxam-AkAP-Ag3Ahn5Ng"
            )
            val nonSecpKeyPair = Ed25519KeyPair.generateKeyPair()

            assertFailsWith(PolluxError.PrivateKeyTypeNotSupportedError::class) {
                pollux.createPresentationSubmission(
                    presentationDefinitionRequest = presentationDefinitionRequest,
                    credential = credential,
                    did = DID("did", "test", "123"),
                    privateKey = nonSecpKeyPair.privateKey
                )
            }
        }

    @Test
    fun testCreatePresentationSubmission_whenAllCorrect_thenPresentationSubmissionProofWellFormed() =
        runTest {
            val definitionJson =
                "{\"presentation_definition\": {\"id\": \"32f54163-7166-48f1-93d8-ff217bdb0653\", \"input_descriptors\": [{\"id\": \"wa_driver_license\", \"name\": \"Washington State Business License\", \"purpose\": \"We can only allow licensed Washington State business representatives into the WA Business Conference\", \"constraints\": {\"fields\": [{\"path\": [\"\$.credentialSubject.dateOfBirth\", \"\$.credentialSubject.dob\", \"\$.vc.credentialSubject.dateOfBirth\", \"\$.vc.credentialSubject.dob\"]}]}}],\"format\": { \"jwt_vp\": { \"alg\": [\"EdDSA\", \"ES256K\"]}}},\"challenge\": \"1f44d55f-f161-4938-a659-f8026467f126\"}"
            val presentationDefinitionRequest: PresentationDefinitionRequest =
                Json.decodeFromString(definitionJson)
            val credential = JWTCredential(
                "eyJhbGciOiJFUzI1NksifQ.eyJpc3MiOiJkaWQ6cHJpc206MjU3MTlhOTZiMTUxMjA3MTY5ODFhODQzMGFkMGNiOTY4ZGQ1MzQwNzM1OTNjOGNkM2YxZDI3YTY4MDRlYzUwZTpDcG9DQ3BjQ0Vsb0tCV3RsZVMweEVBSkNUd29KYzJWamNESTFObXN4RWlBRW9TQ241dHlEYTZZNnItSW1TcXBKOFkxbWo3SkMzX29VekUwTnl5RWlDQm9nc2dOYWVSZGNDUkdQbGU4MlZ2OXRKZk53bDZyZzZWY2hSM09xaGlWYlRhOFNXd29HWVhWMGFDMHhFQVJDVHdvSmMyVmpjREkxTm1zeEVpRE1rQmQ2RnRpb0prM1hPRnUtX2N5NVhtUi00dFVRMk5MR2lXOGFJU29ta1JvZzZTZGU5UHduRzBRMFNCVG1GU1REYlNLQnZJVjZDVExYcmpJSnR0ZUdJbUFTWEFvSGJXRnpkR1Z5TUJBQlFrOEtDWE5sWTNBeU5UWnJNUklnTzcxMG10MVdfaXhEeVFNM3hJczdUcGpMQ05PRFF4Z1ZoeDVzaGZLTlgxb2FJSFdQcnc3SVVLbGZpYlF0eDZKazRUU2pnY1dOT2ZjT3RVOUQ5UHVaN1Q5dCIsInN1YiI6ImRpZDpwcmlzbTpiZWVhNTIzNGFmNDY4MDQ3MTRkOGVhOGVjNzdiNjZjYzdmM2U4MTVjNjhhYmI0NzVmMjU0Y2Y5YzMwNjI2NzYzOkNzY0JDc1FCRW1RS0QyRjFkR2hsYm5ScFkyRjBhVzl1TUJBRVFrOEtDWE5sWTNBeU5UWnJNUklnZVNnLTJPTzFKZG5welVPQml0eklpY1hkZnplQWNUZldBTi1ZQ2V1Q2J5SWFJSlE0R1RJMzB0YVZpd2NoVDNlMG5MWEJTNDNCNGo5amxzbEtvMlpsZFh6akVsd0tCMjFoYzNSbGNqQVFBVUpQQ2dselpXTndNalUyYXpFU0lIa29QdGpqdFNYWjZjMURnWXJjeUluRjNYODNnSEUzMWdEZm1BbnJnbThpR2lDVU9Ca3lOOUxXbFlzSElVOTN0Snkxd1V1TndlSV9ZNWJKU3FObVpYVjg0dyIsIm5iZiI6MTY4NTYzMTk5NSwiZXhwIjoxNjg1NjM1NTk1LCJ2YyI6eyJjcmVkZW50aWFsU3ViamVjdCI6eyJhZGRpdGlvbmFsUHJvcDIiOiJUZXN0MyIsImlkIjoiZGlkOnByaXNtOmJlZWE1MjM0YWY0NjgwNDcxNGQ4ZWE4ZWM3N2I2NmNjN2YzZTgxNWM2OGFiYjQ3NWYyNTRjZjljMzA2MjY3NjM6Q3NjQkNzUUJFbVFLRDJGMWRHaGxiblJwWTJGMGFXOXVNQkFFUWs4S0NYTmxZM0F5TlRack1SSWdlU2ctMk9PMUpkbnB6VU9CaXR6SWljWGRmemVBY1RmV0FOLVlDZXVDYnlJYUlKUTRHVEkzMHRhVml3Y2hUM2UwbkxYQlM0M0I0ajlqbHNsS28yWmxkWHpqRWx3S0IyMWhjM1JsY2pBUUFVSlBDZ2x6WldOd01qVTJhekVTSUhrb1B0amp0U1haNmMxRGdZcmN5SW5GM1g4M2dIRTMxZ0RmbUFucmdtOGlHaUNVT0JreU45TFdsWXNISVU5M3RKeTF3VXVOd2VJX1k1YkpTcU5tWlhWODR3In0sInR5cGUiOlsiVmVyaWZpYWJsZUNyZWRlbnRpYWwiXSwiQGNvbnRleHQiOlsiaHR0cHM6XC9cL3d3dy53My5vcmdcLzIwMThcL2NyZWRlbnRpYWxzXC92MSJdfX0.x0SF17Y0VCDmt7HceOdTxfHlofsZmY18Rn6VQb0-r-k_Bm3hTi1-k2vkdjB25hdxyTCvxam-AkAP-Ag3Ahn5Ng"
            )
            val secpKeyPair = generateSecp256k1KeyPair()
            val signedChallenge =
                (secpKeyPair.privateKey as Secp256k1PrivateKey).sign("1f44d55f-f161-4938-a659-f8026467f126".encodeToByteArray())

            val presentationSubmissionProof = pollux.createPresentationSubmission(
                presentationDefinitionRequest = presentationDefinitionRequest,
                credential = credential,
                did = DID("did", "test", "123"),
                privateKey = secpKeyPair.privateKey
            )

            assertEquals(
                presentationDefinitionRequest.presentationDefinitionBody.id,
                presentationSubmissionProof.presentationSubmission.definitionId
            )
            assertEquals(1, presentationSubmissionProof.presentationSubmission.descriptorMap.size)
            val inputDescriptor =
                presentationDefinitionRequest.presentationDefinitionBody.inputDescriptors.first()
            val descriptorMap =
                presentationSubmissionProof.presentationSubmission.descriptorMap.first()
            assertEquals(inputDescriptor.id, descriptorMap.id)
            assertEquals("jwt_vp", descriptorMap.format)
            assertEquals("$.verifiableCredential[0]", descriptorMap.path)
            assertEquals(1, presentationSubmissionProof.verifiableCredential.size)
            val credentialSubmission = presentationSubmissionProof.verifiableCredential.first()
            if (credentialSubmission::class == W3cCredentialSubmission::class) {
                credentialSubmission as W3cCredentialSubmission
                assertEquals(credential.jwtPayload.verifiableCredential, credentialSubmission.vc)
            }
            assertEquals(
                "did:prism:asdfasdfasdfasdf#keys-1",
                presentationSubmissionProof.proof.verificationMethod
            )
            assertEquals(signedChallenge.toHexString(), presentationSubmissionProof.proof.challenge)
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
}
