package io.iohk.atala.prism.walletsdk.mercury.resolvers

import io.iohk.atala.prism.apollo.base64.base64UrlEncoded
import io.iohk.atala.prism.walletsdk.domain.buildingBlocks.Pluto
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import org.didcommx.didcomm.common.VerificationMaterial
import org.didcommx.didcomm.common.VerificationMaterialFormat
import org.didcommx.didcomm.common.VerificationMethodType
import org.didcommx.didcomm.secret.Secret
import org.didcommx.didcomm.secret.SecretResolver
import java.util.Optional

class DIDCommSecretsResolver(val pluto: Pluto) : SecretResolver {
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
                    Optional.of(
                        Secret(
                            kid,
                            VerificationMethodType.JSON_WEB_KEY_2020,
                            VerificationMaterial(
                                VerificationMaterialFormat.MULTIBASE,
                                privateKey.value.base64UrlEncoded
                            )
                        )
                    )
                } ?: Optional.empty()
        }
    }
}
