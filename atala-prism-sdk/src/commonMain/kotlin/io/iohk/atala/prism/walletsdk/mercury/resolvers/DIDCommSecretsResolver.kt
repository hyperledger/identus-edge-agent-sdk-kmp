package io.iohk.atala.prism.walletsdk.mercury.resolvers

import io.iohk.atala.prism.apollo.base64.base64UrlEncoded
import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Apollo
import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Pluto
import io.iohk.atala.prism.walletsdk.domain.models.OctetPrivateKey
import io.iohk.atala.prism.walletsdk.mercury.OKP
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.didcommx.didcomm.common.VerificationMaterial
import org.didcommx.didcomm.common.VerificationMaterialFormat
import org.didcommx.didcomm.common.VerificationMethodType
import org.didcommx.didcomm.secret.Secret
import org.didcommx.didcomm.secret.SecretResolver
import java.util.Optional

class DIDCommSecretsResolver(val pluto: Pluto, val apollo: Apollo) : SecretResolver {
    override fun findKeys(kids: List<String>): Set<String> {
        return runBlocking {
            kids.filter { pluto.getDIDPrivateKeyByID(it).firstOrNull() != null }.toSet()
        }
    }

    override fun findKey(kid: String): Optional<Secret> {
        return runBlocking {
            pluto.getDIDPrivateKeyByID(kid)
                .firstOrNull()
                ?.let { privateKey ->
                    val keyPair = apollo.createKeyPair(privateKey = privateKey)
                    val octetJwk = OctetPrivateKey(
                        crv = privateKey.keyCurve.curve.value,
                        kty = OKP,
                        d = privateKey.value.base64UrlEncoded,
                        x = keyPair.publicKey.value.base64UrlEncoded
                    )
                    Optional.of(
                        Secret(
                            kid,
                            VerificationMethodType.JSON_WEB_KEY_2020,
                            VerificationMaterial(
                                VerificationMaterialFormat.JWK,
                                Json.encodeToString(octetJwk)
                            )
                        )
                    )
                } ?: Optional.empty()
        }
    }
}
