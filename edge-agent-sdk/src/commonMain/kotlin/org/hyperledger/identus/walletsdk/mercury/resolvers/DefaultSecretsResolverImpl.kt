@file:Suppress("ktlint:standard:import-ordering")

package org.hyperledger.identus.walletsdk.mercury.resolvers

import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.Serializable
import org.hyperledger.identus.apollo.base64.base64UrlEncoded
import org.hyperledger.identus.walletsdk.domain.buildingblocks.Apollo
import org.hyperledger.identus.walletsdk.domain.buildingblocks.Pluto
import org.hyperledger.identus.walletsdk.domain.models.Secret
import org.hyperledger.identus.walletsdk.domain.models.SecretMaterialJWK
import org.hyperledger.identus.walletsdk.domain.models.SecretType
import org.hyperledger.identus.walletsdk.mercury.OKP

/**
 * Default implementation of the [SecretsResolver] interface.
 *
 * @property pluto Instance of the Pluto class used for resolving secrets.
 */
class DefaultSecretsResolverImpl(val pluto: Pluto, val apollo: Apollo) : SecretsResolver {

    /**
     * Represents a Private JSON Web Key (JWK).
     * This class is used to hold the necessary information for a private key used in JSON Web Signature (JWS).
     *
     * @property kty The key type. Default value is "OKP".
     * @property kid The key ID.
     * @property crv The cryptographic curve used by the key.
     * @property d The private key value. It is nullable and defaults to null.
     */
    @Serializable
    data class PrivateJWK @JvmOverloads constructor(
        val kty: String = OKP,
        val kid: String,
        val crv: String,
        val d: String? = null
    )

    /**
     * Finds secrets based on the provided secret IDs.
     *
     * @param secretIds An array of secret IDs.
     * @return An array of secrets that match the provided secret IDs.
     */
    override suspend fun findSecrets(secretIds: Array<String>): Array<String> {
        return secretIds.filter {
            pluto.getDIDPrivateKeyByID(it)
                .firstOrNull() != null
        }.toTypedArray()
    }

    /**
     * Retrieves a secret based on its ID.
     *
     * @param secretId The ID of the secret.
     * @return The secret object if found, otherwise null.
     */
    override suspend fun getSecret(secretId: String): Secret? {
        return pluto.getDIDPrivateKeyByID(secretId).firstOrNull()?.let { storablePrivateKey ->
            val privateKey = apollo.restorePrivateKey(storablePrivateKey.restorationIdentifier, storablePrivateKey.data)
            return Secret(
                secretId,
                SecretType.JsonWebKey2020,
                SecretMaterialJWK(
                    PrivateJWK(
                        secretId,
                        privateKey.getCurve(),
                        privateKey.getValue().base64UrlEncoded
                    ).toString()
                )
            )
        }
    }
}
