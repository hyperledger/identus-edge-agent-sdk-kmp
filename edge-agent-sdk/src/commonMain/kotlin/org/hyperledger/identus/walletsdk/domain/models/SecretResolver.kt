package org.hyperledger.identus.walletsdk.domain.models

/**
 * Interface for resolving secrets by their ID.
 */
interface SecretResolver {
    fun resolve(secretIds: Array<String>)
}
