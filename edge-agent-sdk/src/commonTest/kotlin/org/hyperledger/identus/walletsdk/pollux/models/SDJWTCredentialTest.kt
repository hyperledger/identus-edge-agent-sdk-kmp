package org.hyperledger.identus.walletsdk.pollux.models

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.crypto.Ed25519Signer
import com.nimbusds.jose.jwk.KeyUse
import com.nimbusds.jose.jwk.OctetKeyPair
import com.nimbusds.jose.util.Base64URL
import com.nimbusds.jwt.SignedJWT
import eu.europa.ec.eudi.sdjwt.SdJwtIssuer
import eu.europa.ec.eudi.sdjwt.exp
import eu.europa.ec.eudi.sdjwt.iat
import eu.europa.ec.eudi.sdjwt.iss
import eu.europa.ec.eudi.sdjwt.nimbus
import eu.europa.ec.eudi.sdjwt.plain
import eu.europa.ec.eudi.sdjwt.sd
import eu.europa.ec.eudi.sdjwt.sdJwt
import eu.europa.ec.eudi.sdjwt.serialize
import eu.europa.ec.eudi.sdjwt.sub
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.put
import org.hyperledger.identus.apollo.base64.base64UrlDecoded
import org.hyperledger.identus.walletsdk.apollo.utils.Ed25519KeyPair
import org.hyperledger.identus.walletsdk.domain.models.CredentialOperationsOptions
import org.hyperledger.identus.walletsdk.domain.models.CredentialType
import org.hyperledger.identus.walletsdk.domain.models.DID
import org.hyperledger.identus.walletsdk.domain.models.Message
import org.junit.Test

class SDJWTCredentialTest {

    var issuer: SdJwtIssuer<SignedJWT>? = null

    @Test
    fun `test credential presentation`() = runTest {
        val keyPair = Ed25519KeyPair.generateKeyPair()

        val credential = createSDJWTCredential(keyPair)

        val msg = Json.decodeFromString<Message>(
            """{"id":"b3c7e130-9e90-454a-945c-58c7a4c9a181","piuri":"https://didcomm.atalaprism.io/present-proof/3.0/request-presentation","from":{"method":"peer","methodId":"asdf"},"to":{"method":"peer","methodId":"fdsafdsa"},"fromPrior":null,"body":"{\"proof_types\":[]}","created_time":"1727207690","expires_time_plus":"1727294090","attachments":[{"id":"6447a63c-17fa-4e22-a27a-ec0f6d5d753f","media_type":"application/json","data":{"base64":"eyJwcmVzZW50YXRpb25fZGVmaW5pdGlvbiI6eyJpZCI6IjU1NjlhYmQ3LTI5OTQtNDA2OC1iZTM2LTQwMjVlYmNhZjIwYiIsImlucHV0X2Rlc2NyaXB0b3JzIjpbeyJpZCI6IjU1ZmNhNzJlLWYyODQtNDNlZC1iMmNjLTNlODVmNzIwNmJjMSIsIm5hbWUiOiJQcmVzZW50YXRpb24iLCJwdXJwb3NlIjoiUHJlc2VudGF0aW9uIGRlZmluaXRpb24iLCJmb3JtYXQiOnsic2RKd3QiOnsiYWxnIjpbIkVTMjU2ayJdfX0sImNvbnN0cmFpbnRzIjp7ImZpZWxkcyI6W3sicGF0aCI6WyIkLnZjLmNyZWRlbnRpYWxTdWJqZWN0LmZpcnN0X25hbWUiLCIkLmNyZWRlbnRpYWxTdWJqZWN0LmZpcnN0X25hbWUiLCIkLmZpcnN0X25hbWUiXSwiaWQiOiIyNzVmNDcyYi0wNWE2LTRhNzAtODcxMS03NDVkODM1ZDlkZDUiLCJuYW1lIjoiZmlyc3RfbmFtZSIsImZpbHRlciI6eyJ0eXBlIjoic3RyaW5nIiwicGF0dGVybiI6IldvbmRlcmxhbmQifX0seyJwYXRoIjpbIiQudmMuY3JlZGVudGlhbFN1YmplY3QubGFzdF9uYW1lIiwiJC5jcmVkZW50aWFsU3ViamVjdC5sYXN0X25hbWUiLCIkLmxhc3RfbmFtZSJdLCJpZCI6IjA0YjhjMjMxLTE1YjQtNDI1NC1hNDRmLWZjMmM2NzAzMmE0MCIsIm5hbWUiOiJsYXN0X25hbWUiLCJmaWx0ZXIiOnsidHlwZSI6InN0cmluZyIsInBhdHRlcm4iOiJBbGljZSJ9fSx7InBhdGgiOlsiJC52Yy5jcmVkZW50aWFsU3ViamVjdC5lbWFpbEFkZHJlc3MiLCIkLmNyZWRlbnRpYWxTdWJqZWN0LmVtYWlsQWRkcmVzcyIsIiQuZW1haWxBZGRyZXNzIl0sImlkIjoiYmQxMWVlMjAtZDcyNS00ZjA2LWJmMWItNjE3NDFiMmZmM2FmIiwibmFtZSI6ImVtYWlsQWRkcmVzcyIsImZpbHRlciI6eyJ0eXBlIjoic3RyaW5nIiwicGF0dGVybiI6ImFsaWNlQHdvbmRlcmxhbmQuY29tIn19XSwibGltaXRfZGlzY2xvc3VyZSI6InJlcXVpcmVkIn19XSwiZm9ybWF0Ijp7InNkSnd0Ijp7ImFsZyI6WyJFUzI1NmsiXX19fX0"},"format":"dif/presentation-exchange/definitions@v1.0"}],"thid":"23f89714-7325-4048-a26b-978a0615efd7","ack":[],"direction":"SENT"}"""
        )
        val requestData = msg.attachments.first().data.getDataAsJsonString()

        val presentation = credential.presentation(
            attachmentFormat = CredentialType.SDJWT.type,
            request = requestData.encodeToByteArray(),
            listOf(CredentialOperationsOptions.DisclosingClaims(listOf("/emailAddress", "/first_name", "/last_name")))
        )
        val presentationSubmission = Json.decodeFromString<PresentationSubmission>(presentation)
        assertTrue(presentationSubmission.verifiablePresentation.first().contains("."))
        val jws = presentationSubmission.verifiablePresentation.first()
        val disclosures = jws.split("~")
        var foundFirstName = false
        var foundLastName = false
        var foundEmail = false
        for (i in 1..disclosures.size - 2) {
            val value = disclosures[i].base64UrlDecoded
            if (value.contains("first_name")) {
                foundFirstName = true
            }
            if (value.contains("last_name")) {
                foundLastName = true
            }
            if (value.contains("email")) {
                foundEmail = true
            }
        }
        assertTrue(foundFirstName && foundLastName && foundEmail)
    }

    fun createSDJWTCredential(keyPair: Ed25519KeyPair): SDJWTCredential {
        val subject = DID("did:prism:asdfasdf")

        val octet = OctetKeyPair.Builder(com.nimbusds.jose.jwk.Curve.Ed25519, Base64URL.encode(keyPair.publicKey.raw))
            .d(Base64URL.encode(keyPair.privateKey.raw))
            .keyUse(KeyUse.SIGNATURE)
            .build()

        issuer = SdJwtIssuer
            .nimbus(
                signer = Ed25519Signer(octet),
                signAlgorithm = JWSAlgorithm.EdDSA
            )
        val sdjwt = issuer!!.issue(
            sdJwt {
                plain {
                    sub(subject.toString())
                    iss("did:prism:ce3403b5a733883035d6ec43ba075a41c9cc0a3257977d80c75d6319ade0ed70")
                    iat(1516239022)
                    exp(1735689661)
                }
                sd {
                    put("first_name", "Cristian")
                    put("last_name", "Gonzalez")
                    put("emailAddress", "test@iohk.io")
                }
            }
        ).getOrThrow().serialize()

        return SDJWTCredential.fromSDJwtString(sdjwt)
    }
}
