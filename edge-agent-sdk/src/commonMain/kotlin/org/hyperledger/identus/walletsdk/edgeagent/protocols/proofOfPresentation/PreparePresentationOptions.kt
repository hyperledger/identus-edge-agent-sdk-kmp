package org.hyperledger.identus.walletsdk.edgeagent.protocols.proofOfPresentation

sealed interface PreparePresentationOptions

data class SDJWTPreparePresentationOptions(
    val presentationFrame: Map<String, Boolean>
) : PreparePresentationOptions
