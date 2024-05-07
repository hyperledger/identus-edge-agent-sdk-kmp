package org.hyperledger.identus.walletsdk.pollux.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents the proof of correctness of a public key used for signing a credential.
 *
 * @property c The value `c` used in the proof.
 * @property xzCap The value `xz_cap` used in the proof.
 * @property xrCap The map of `xr_cap` values used in the proof.
 */
@Serializable
data class KeyCorrectnessProof(
    val c: String,
    @SerialName("xz_cap")
    val xzCap: String,
    @SerialName("xr_cap")
    val xrCap: Map<String, String>
)
