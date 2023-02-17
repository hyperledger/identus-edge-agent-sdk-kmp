package io.iohk.atala.prism.walletsdk.mercury.resolvers

 import io.iohk.atala.prism.apollo.base64.base64UrlEncoded
 import io.iohk.atala.prism.walletsdk.domain.buildingBlocks.Pluto
 import io.iohk.atala.prism.walletsdk.domain.models.MercuryError
 import io.iohk.atala.prism.walletsdk.domain.models.Secret
 import io.iohk.atala.prism.walletsdk.domain.models.SecretMaterialJWK
 import io.iohk.atala.prism.walletsdk.domain.models.SecretType
 import kotlinx.serialization.Serializable
 import kotlin.js.ExperimentalJsExport
 import kotlin.js.JsExport

@OptIn(ExperimentalJsExport::class)
@JsExport
class DefaultSecretsResolverImpl(val pluto: Pluto): SecretsResolver {

    @Serializable
    data class PrivateJWK(
        val kty: String = "OKP",
        val kid: String,
        val crv: String,
        val d: String? = null,
    )

    override fun findSecrets(secretIds: Array<String>): Array<String> {
        return secretIds.filter {
            pluto.getDIDPrivateKeyByID(it) != null
        }.toTypedArray()
    }

    override fun getSecret(secretid: String): Secret? {
        return pluto.getDIDPrivateKeyByID(secretid)?.let {
            Secret(
                secretid,
                SecretType.JsonWebKey2020,
                SecretMaterialJWK(PrivateJWK(
                    secretid,
                    it.keyCurve.curve.toString(),
                    it.value.base64UrlEncoded
                ).toString()))
        }
    }
 }
