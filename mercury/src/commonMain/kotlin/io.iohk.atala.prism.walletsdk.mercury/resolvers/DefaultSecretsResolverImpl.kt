package io.iohk.atala.prism.walletsdk.mercury.resolvers

import io.iohk.atala.prism.apollo.base64.base64UrlEncoded
import io.iohk.atala.prism.walletsdk.domain.buildingBlocks.Pluto
import io.iohk.atala.prism.walletsdk.domain.models.Secret
import io.iohk.atala.prism.walletsdk.domain.models.SecretMaterialJWK
import io.iohk.atala.prism.walletsdk.domain.models.SecretType
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.Serializable
import kotlin.js.ExperimentalJsExport

@OptIn(ExperimentalJsExport::class)
class DefaultSecretsResolverImpl(val pluto: Pluto) : SecretsResolver {

    @Serializable
    data class PrivateJWK(
        val kty: String = "OKP",
        val kid: String,
        val crv: String,
        val d: String? = null,
    )

    override suspend fun findSecrets(secretIds: Array<String>): Array<String> {
        return secretIds.filter {
            pluto.getDIDPrivateKeyByID(it)
                .firstOrNull() != null
        }.toTypedArray()
    }

    override suspend fun getSecret(secretid: String): Secret? {
        return pluto.getDIDPrivateKeyByID(secretid)
            .firstOrNull()
            ?.let { privateKey ->
                return Secret(
                    secretid,
                    SecretType.JsonWebKey2020,
                    SecretMaterialJWK(
                        PrivateJWK(
                            secretid,
                            privateKey.keyCurve.curve.toString(),
                            privateKey.value.base64UrlEncoded,
                        ).toString(),
                    ),
                )
            }
    }
}
