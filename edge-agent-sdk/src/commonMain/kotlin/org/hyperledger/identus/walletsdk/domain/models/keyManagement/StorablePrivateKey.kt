package org.hyperledger.identus.walletsdk.domain.models.keyManagement

data class StorablePrivateKey(
    val id: String,
    val restorationIdentifier: String,
    val data: String,
    val keyPathIndex: Int?
)
