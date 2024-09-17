package org.hyperledger.identus.walletsdk.edgeagent.protocols.outOfBand

import kotlinx.serialization.Serializable
import org.hyperledger.identus.walletsdk.edgeagent.protocols.issueCredential.OfferCredential
import java.util.*

/**
 * Represents a connectionless credential offer.
 */
@Serializable
data class ConnectionlessCredentialOffer(
    val offerCredential: OfferCredential
) : InvitationType()
