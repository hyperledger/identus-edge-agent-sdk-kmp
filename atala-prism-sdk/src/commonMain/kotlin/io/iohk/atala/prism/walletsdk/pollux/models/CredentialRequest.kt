package io.iohk.atala.prism.walletsdk.pollux.models

import io.iohk.atala.prism.walletsdk.domain.models.Credential
import kotlinx.serialization.Serializable

interface CredentialRequest {
    val cred_def_id: String
    val blinded_ms: CredentialRequestBlindedMS
    val blinded_ms_correctness_proof: CredentialRequestBlindedMSCorrectnessProof
    val entropy: String
    val nonce: String
}

interface CredentialRequestBlindedMS {
    val u: String
    val ur: String
}

interface CredentialRequestBlindedMSCorrectnessProof {
    val c: String
    val v_dash_cap: String
    val m_caps: Map<String, String>
}

@Serializable
data class LinkSecretBlindingData(
    var vPrime: String
)

data class CredentialDefinition(
    val schemaId: String,
    val type: String,
    val tag: String,
    val value: Array<String>,
    val issuerId: String,
)

interface CredentialIssued : Credential {
    val values: List<Pair<String, CredentialValue>>
}

interface CredentialValue {
    val encoded: String
    val raw: String
}
