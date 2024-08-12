package org.hyperledger.identus.walletsdk.pluto.models

data class DidKeyLink(val id: Int, val didId: String, val keyId: String, val alias: String? = null)
