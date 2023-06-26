package io.iohk.atala.prism.walletsdk.domain.models

sealed interface Credential {
    val issuer: String
    val subject: String
    val claims: Array<Claim>
    val properties: Map<String, String>
}
