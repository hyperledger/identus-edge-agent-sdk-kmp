package org.hyperledger.identus.walletsdk.mercury.resolvers

import org.hyperledger.identus.walletsdk.domain.models.Secret

/**
 * Resolves secrets by finding secrets based on secret IDs or retrieving a specific secret by its ID.
 */
interface SecretsResolver {
    /**
     * Asynchronously finds secrets based on the provided secret IDs.
     *
     * @param secretIds An array of secret IDs to be searched.
     * @return An array of strings representing the secret values found.
     */
    suspend fun findSecrets(secretIds: Array<String>): Array<String>

    /**
     * Suspends the execution until the secret with the given secretId is retrieved.
     *
     * @param secretId The ID of the secret to retrieve.
     * @return The secret with the specified ID, or null if the secret does not exist.
     */
    suspend fun getSecret(secretId: String): Secret?
}
