package io.iohk.atala.prism.walletsdk.pollux.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class KeyCorrectnessProof(
    val c: String,
    @SerialName("xz_cap")
    val xzCap: String,
    @SerialName("xr_cap")
    val xrCap: Map<String, String>
)
