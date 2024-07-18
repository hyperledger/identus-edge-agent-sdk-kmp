package org.hyperledger.identus.walletsdk.pluto

data class StorablePrivateKey(
    val id: String,
    val restorationIdentifier: String,
    val data: String,
    val keyPathIndex: Int?
)
