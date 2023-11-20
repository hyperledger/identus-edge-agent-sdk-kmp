package io.iohk.atala.prism.walletsdk.domain.models.keyManagement

interface StorableKey {
    val storableData: ByteArray
    val restorationIdentifier: String
}
