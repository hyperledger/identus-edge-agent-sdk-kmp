package io.iohk.atala.prism.walletsdk.domain.models

interface Credential {
    val id: String
    val issuer: String
    val subject: String?
    val claims: Array<Claim>
    val properties: Map<String, Any?>
}
