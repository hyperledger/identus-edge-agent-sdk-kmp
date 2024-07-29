@file:Suppress("ktlint:standard:import-ordering")

package org.hyperledger.identus.walletsdk.mercury.resolvers

import java.util.*
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.didcommx.didcomm.common.VerificationMaterial
import org.didcommx.didcomm.common.VerificationMaterialFormat
import org.didcommx.didcomm.common.VerificationMethodType
import org.didcommx.didcomm.secret.Secret
import org.didcommx.didcomm.secret.SecretResolver
import org.hyperledger.identus.apollo.base64.base64UrlEncoded
import org.hyperledger.identus.walletsdk.domain.buildingblocks.Apollo
import org.hyperledger.identus.walletsdk.domain.buildingblocks.Pluto
import org.hyperledger.identus.walletsdk.domain.models.OctetPrivateKey
import org.hyperledger.identus.walletsdk.mercury.OKP

/**
 * DIDCommSecretsResolver is a class that implements the SecretResolver interface.
 * It is responsible for resolving secrets using the Pluto and Apollo components.
 *
 * @property pluto The Pluto component used to get DID private keys.
 * @property apollo The Apollo component used for some other functionality.
 */
class DIDCommSecretsResolver(val pluto: Pluto, val apollo: Apollo) : SecretResolver {
    /**
     * Finds the keys associated with the provided list of kid values.
     *
     * @param kids The list of kid values to search for.
     * @return A set of keys that match the provided kid values.
     */
    override fun findKeys(kids: List<String>): Set<String> {
        return runBlocking {
            kids.filter { pluto.getDIDPrivateKeyByID(it).firstOrNull() != null }.toSet()
        }
    }

    /**
     * Finds a key associated with the provided kid value and returns it as an Optional.
     *
     * @param kid The kid value to search for.
     * @return An Optional containing the key as a Secret object, or an empty Optional if no key is found.
     */
    override fun findKey(kid: String): Optional<Secret> {
        return runBlocking {
            pluto.getDIDPrivateKeyByID(kid)
                .firstOrNull()
                ?.let { storablePrivateKey ->
                    val privateKey = apollo.restorePrivateKey(storablePrivateKey.restorationIdentifier, storablePrivateKey.data)

                    val octetJwk = OctetPrivateKey(
                        crv = privateKey.getCurve(),
                        kty = OKP,
                        d = privateKey.getValue().base64UrlEncoded,
                        x = privateKey.publicKey().getValue().base64UrlEncoded
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
