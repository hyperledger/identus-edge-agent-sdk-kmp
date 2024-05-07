package org.hyperledger.identus.walletsdk.pollux.models

import kotlinx.serialization.SerialName

/**
 * Represents the payload of an anonymous credential.
 *
 * @property schemaId The ID of the schema associated with the credential.
 * @property credDefId The ID of the credential definition associated with the credential.
 * @property keyCorrectnessProof The proof of correctness of the public key used for signing the credential.
 * @property nonce The cryptographic nonce used for the proof of correctness.
 * @property methodName The method name for the anonymous credential.
 */
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
