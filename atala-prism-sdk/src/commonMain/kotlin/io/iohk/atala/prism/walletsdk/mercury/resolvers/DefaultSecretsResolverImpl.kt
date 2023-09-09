package io.iohk.atala.prism.walletsdk.mercury.resolvers

import io.iohk.atala.prism.apollo.base64.base64UrlEncoded
import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Pluto
import io.iohk.atala.prism.walletsdk.domain.models.Secret
import io.iohk.atala.prism.walletsdk.domain.models.SecretMaterialJWK
import io.iohk.atala.prism.walletsdk.domain.models.SecretType
import io.iohk.atala.prism.walletsdk.mercury.OKP
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.Serializable

class DefaultSecretsResolverImpl(val pluto: Pluto) : SecretsResolver {

    @Serializable
    data class PrivateJWK @JvmOverloads constructor(
        val kty: String = OKP,
        val kid: String,
        val crv: String,
        val d: String? = null
    )

    override suspend fun findSecrets(secretIds: Array<String>): Array<String> {
        return secretIds.filter {
            pluto.getDIDPrivateKeyByID(it)
                .firstOrNull() != null
        }.toTypedArray()
    }

    override suspend fun getSecret(secretId: String): Secret? {
        return pluto.getDIDPrivateKeyByID(secretId).firstOrNull()?.let { privateKey ->
            return Secret(
                secretId,
                SecretType.JsonWebKey2020,
                SecretMaterialJWK(
                    PrivateJWK(
                        secretId,
                        privateKey.keyCurve.curve.toString(),
                        privateKey.value.base64UrlEncoded
                    ).toString()
                )
            )
        }
    }
}
