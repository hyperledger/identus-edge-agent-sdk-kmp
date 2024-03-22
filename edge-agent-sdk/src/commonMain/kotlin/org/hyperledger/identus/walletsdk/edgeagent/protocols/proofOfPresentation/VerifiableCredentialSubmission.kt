package org.hyperledger.identus.walletsdk.edgeagent.protocols.proofOfPresentation

import org.hyperledger.identus.walletsdk.domain.models.JWTPayload
import kotlinx.serialization.Serializable

@Serializable
abstract class CredentialSubmission

@Serializable
class W3cCredentialSubmission(
    val comment: String? = null,
    val vc: JWTPayload.JWTVerifiableCredential
) : CredentialSubmission()

@Serializable
class ProofCredentialSubmission(
    val context: String,
    val id: String,
    val type: Array<String>,
    val issuer: String,
    val issuanceDate: String,
    val credentialSubject: Map<String, String>,
    val proof: Proof
) : CredentialSubmission()
