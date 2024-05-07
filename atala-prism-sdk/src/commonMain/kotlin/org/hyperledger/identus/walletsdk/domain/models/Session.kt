package org.hyperledger.identus.walletsdk.domain.models

import java.util.UUID
import kotlin.jvm.JvmOverloads

/**
 * Represents a session between two entities.
 *
 * @property uuid The unique identifier for the session.
 * @property seed The seed used for key generation.
 * @constructor Creates a Session object with the specified [uuid] and [seed].
 */
data class Session @JvmOverloads constructor(
    val uuid: UUID = UUID.randomUUID(),
    val seed: Seed
)
