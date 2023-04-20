package io.iohk.atala.prism.walletsdk.mercury.resolvers

import io.iohk.atala.prism.walletsdk.domain.models.Secret

interface SecretsResolver {
    suspend fun findSecrets(secretIds: Array<String>): Array<String>
    suspend fun getSecret(secretId: String): Secret?
}
