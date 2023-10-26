package io.iohk.atala.prism.walletsdk.pollux.models

import kotlinx.serialization.SerialName

data class AnoncredPayload(
    @SerialName("schema_id")
    val schemaId: String,
    @SerialName("cred_def_id")
    val credDefId: String,
    @SerialName("key_correctness_proof")
    val keyCorrectnessProof: KeyCorrectnessProof,
    val nonce: String,
    @SerialName("method_name")
    val methodName: String
)
