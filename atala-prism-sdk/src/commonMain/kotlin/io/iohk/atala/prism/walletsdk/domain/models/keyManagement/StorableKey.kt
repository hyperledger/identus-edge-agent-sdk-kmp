package io.iohk.atala.prism.walletsdk.domain.models.keyManagement

/**
 * This interface defines what a key requires to be storable.
 */
interface StorableKey {
    val storableData: ByteArray
    val restorationIdentifier: String
}
