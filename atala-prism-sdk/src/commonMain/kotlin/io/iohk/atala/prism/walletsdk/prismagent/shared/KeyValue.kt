package io.iohk.atala.prism.walletsdk.prismagent.shared

import kotlinx.serialization.Serializable

@Serializable
data class KeyValue(
    val key: String,
    val value: String,
)
