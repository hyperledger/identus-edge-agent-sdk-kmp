package io.iohk.atala.prism.walletsdk.domain.models.keyManagement

enum class SecurityLevel {
    HIGH,
    LOW
}

interface StorableKey {
    fun store()
    val securityLevel: SecurityLevel
    val storableData: ByteArray
    val restorationIdentifier: String
}
