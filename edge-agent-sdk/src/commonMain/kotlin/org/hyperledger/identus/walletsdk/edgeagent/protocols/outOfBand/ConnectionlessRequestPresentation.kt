package org.hyperledger.identus.walletsdk.edgeagent.protocols.outOfBand

import kotlinx.serialization.Serializable
import org.hyperledger.identus.walletsdk.edgeagent.protocols.proofOfPresentation.RequestPresentation

@Serializable
data class ConnectionlessRequestPresentation(
    val requestPresentation: RequestPresentation
) : InvitationType()
