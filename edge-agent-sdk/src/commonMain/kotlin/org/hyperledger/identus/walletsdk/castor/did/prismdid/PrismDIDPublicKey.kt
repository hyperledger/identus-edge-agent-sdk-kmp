package org.hyperledger.identus.walletsdk.castor.did.prismdid

import org.hyperledger.identus.apollo.secp256k1.Secp256k1Lib
import org.hyperledger.identus.protos.CompressedECKeyData
import org.hyperledger.identus.protos.KeyUsage
import org.hyperledger.identus.walletsdk.apollo.utils.Secp256k1PublicKey
import org.hyperledger.identus.walletsdk.domain.buildingblocks.Apollo
import org.hyperledger.identus.walletsdk.domain.models.CastorError
import org.hyperledger.identus.walletsdk.domain.models.Curve
import org.hyperledger.identus.walletsdk.domain.models.keyManagement.PublicKey
import pbandk.ByteArr
import kotlin.jvm.Throws

/**
 * Represents a public key for the system.
 *
 * @property apollo The instance of [Apollo] used for cryptographic operations.
 * @property id The ID of the public key.
 * @property usage The intended usage of the public key.
 * @property keyData The actual public key data.
 */
class PrismDIDPublicKey {
    private val apollo: Apollo
    val id: String
    val usage: Usage
    val keyData: PublicKey

    /**
     * Represents a PrismDIDPublicKey.
     *
     * @param apollo The cryptography suite representation.
     * @param id The ID of the public key.
     * @param usage The usage of the public key.
     * @param keyData The actual public key data.
     */
    constructor(apollo: Apollo, id: String, usage: Usage, keyData: PublicKey) {
        this.apollo = apollo
        this.id = id
        this.usage = usage
        this.keyData = keyData
    }

    /**
     * Constructs a PrismDIDPublicKey object.
     *
     * @param apollo The Apollo object used for cryptographic operations in the Atala PRISM.
     * @param proto The protobuf representation of the public key.
     * @throws CastorError.InvalidPublicKeyEncoding if the encoding of the key is invalid.
     */
    @Throws(CastorError.InvalidPublicKeyEncoding::class)
    constructor(apollo: Apollo, proto: org.hyperledger.identus.protos.PublicKey) {
        this.apollo = apollo
        this.id = proto.id
        this.usage = proto.usage.fromProto()
        this.keyData = when (proto.keyData) {
            is org.hyperledger.identus.protos.PublicKey.KeyData.CompressedEcKeyData -> {
                Secp256k1PublicKey(proto.keyData.value.data.array)
            }

            else -> {
                throw CastorError.InvalidPublicKeyEncoding("prism", "secp256k1")
            }
        }
    }

    /**
     * Converts the PublicKey object to a Protobuf PublicKey object.
     *
     * @return the converted Protobuf PublicKey object
     */
    fun toProto(): org.hyperledger.identus.protos.PublicKey {
        val compressedPublicKey = Secp256k1PublicKey(Secp256k1Lib().compressPublicKey(keyData.getValue()))
        return org.hyperledger.identus.protos.PublicKey(
            id = id,
            usage = usage.toProto(),
            keyData = org.hyperledger.identus.protos.PublicKey.KeyData.CompressedEcKeyData(
                compressedPublicKey.toProto()
            )
        )
    }

    /**
     * Enumeration representing the possible usages of a public key.
     *
     * @property value The string representation of the usage.
     * @constructor Creates an instance of the Usage enum with the given value.
     */
    enum class Usage(val value: String) {
        MASTER_KEY("masterKey"),
        ISSUING_KEY("issuingKey"),
        AUTHENTICATION_KEY("authenticationKey"),
        REVOCATION_KEY("revocationKey"),
        CAPABILITY_DELEGATION_KEY("capabilityDelegationKey"),
        CAPABILITY_INVOCATION_KEY("capabilityInvocationKey"),
        KEY_AGREEMENT_KEY("keyAgreementKey"),
        UNKNOWN_KEY("unknownKey")
    }
}

/**
 * Converts a `KeyUsage` object to a `PrismDIDPublicKey.Usage` object.
 *
 * @return The corresponding `PrismDIDPublicKey.Usage` object.
 */
fun KeyUsage.fromProto(): PrismDIDPublicKey.Usage {
    return when (this) {
        is KeyUsage.MASTER_KEY -> PrismDIDPublicKey.Usage.MASTER_KEY
        is KeyUsage.ISSUING_KEY -> PrismDIDPublicKey.Usage.ISSUING_KEY
        is KeyUsage.AUTHENTICATION_KEY -> PrismDIDPublicKey.Usage.AUTHENTICATION_KEY
        is KeyUsage.REVOCATION_KEY -> PrismDIDPublicKey.Usage.REVOCATION_KEY
        is KeyUsage.CAPABILITY_DELEGATION_KEY -> PrismDIDPublicKey.Usage.CAPABILITY_DELEGATION_KEY
        is KeyUsage.CAPABILITY_INVOCATION_KEY -> PrismDIDPublicKey.Usage.CAPABILITY_INVOCATION_KEY
        is KeyUsage.KEY_AGREEMENT_KEY -> PrismDIDPublicKey.Usage.KEY_AGREEMENT_KEY
        is KeyUsage.UNKNOWN_KEY -> PrismDIDPublicKey.Usage.UNKNOWN_KEY
        is KeyUsage.UNRECOGNIZED -> PrismDIDPublicKey.Usage.UNKNOWN_KEY
    }
}

/**
 * Converts a Secp256k1PublicKey object to a CompressedECKeyData object.
 *
 * @return the converted CompressedECKeyData object.
 */
fun Secp256k1PublicKey.toProto(): CompressedECKeyData {
    return CompressedECKeyData(
        curve = Curve.SECP256K1.value,
        data = ByteArr(raw)
    )
}

/**
 * Generates the identifier for a PrismDIDPublicKey.Usage based on the given index.
 *
 * @param index The index used to generate the identifier.
 * @return The generated identifier.
 */
fun PrismDIDPublicKey.Usage.id(index: Int): String {
    return when (this) {
        PrismDIDPublicKey.Usage.MASTER_KEY -> "master$index"
        PrismDIDPublicKey.Usage.ISSUING_KEY -> "issuing$index"
        PrismDIDPublicKey.Usage.AUTHENTICATION_KEY -> "authentication$index"
        PrismDIDPublicKey.Usage.REVOCATION_KEY -> "revocation$index"
        PrismDIDPublicKey.Usage.CAPABILITY_DELEGATION_KEY -> "capabilityDelegation$index"
        PrismDIDPublicKey.Usage.CAPABILITY_INVOCATION_KEY -> "capabilityInvocation$index"
        PrismDIDPublicKey.Usage.KEY_AGREEMENT_KEY -> "keyAgreement$index"
        PrismDIDPublicKey.Usage.UNKNOWN_KEY -> "unknown$index"
    }
}

/**
 * Converts the Usage value of a PrismDIDPublicKey to the corresponding KeyUsage enum value.
 *
 * @return The KeyUsage enum value corresponding to the Usage value of the PrismDIDPublicKey.
 */
fun PrismDIDPublicKey.Usage.toProto(): KeyUsage {
    return when (this) {
        PrismDIDPublicKey.Usage.MASTER_KEY -> KeyUsage.MASTER_KEY
        PrismDIDPublicKey.Usage.ISSUING_KEY -> KeyUsage.ISSUING_KEY
        PrismDIDPublicKey.Usage.AUTHENTICATION_KEY -> KeyUsage.AUTHENTICATION_KEY
        PrismDIDPublicKey.Usage.REVOCATION_KEY -> KeyUsage.REVOCATION_KEY
        PrismDIDPublicKey.Usage.CAPABILITY_DELEGATION_KEY -> KeyUsage.CAPABILITY_DELEGATION_KEY
        PrismDIDPublicKey.Usage.CAPABILITY_INVOCATION_KEY -> KeyUsage.CAPABILITY_INVOCATION_KEY
        PrismDIDPublicKey.Usage.KEY_AGREEMENT_KEY -> KeyUsage.KEY_AGREEMENT_KEY
        PrismDIDPublicKey.Usage.UNKNOWN_KEY -> KeyUsage.UNKNOWN_KEY
    }
}

/**
 * Returns the default ID for the current usage of the PrismDIDPublicKey.
 * This method generates an ID based on the usage enum value.
 *
 * @return The default ID for the current usage.
 */
fun PrismDIDPublicKey.Usage.defaultId(): String {
    return this.id(0)
}
