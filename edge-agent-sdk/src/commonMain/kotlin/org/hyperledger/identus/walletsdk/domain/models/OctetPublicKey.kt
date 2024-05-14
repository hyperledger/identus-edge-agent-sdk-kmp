package org.hyperledger.identus.walletsdk.domain.models

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import org.hyperledger.identus.walletsdk.mercury.OKP

/**
 * Represents an Octet Key Pair (OKP) public key.
 *
 * @property kty The key type. Must be "OKP".
 * @property crv The curve for the key.
 * @property x The value of the public key.
 */
@Serializable
internal data class OctetPublicKey
@OptIn(ExperimentalSerializationApi::class)
@JvmOverloads
internal constructor(@EncodeDefault val kty: String = OKP, val crv: String, val x: String)

/**
 * Represents an Octet Key Pair (OKP) private key.
 *
 * @property kty The key type.
 * @property crv The curve for the key.
 * @property x The public key value.
 * @property d The private key value.
 */
@Serializable
internal data class OctetPrivateKey(val kty: String, val crv: String, val x: String, val d: String)
