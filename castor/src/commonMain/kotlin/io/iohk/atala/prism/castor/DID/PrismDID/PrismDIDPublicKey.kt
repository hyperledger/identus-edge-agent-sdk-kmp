package io.iohk.atala.prism.castor.did.prismdid

import io.iohk.atala.prism.domain.buildingBlocks.Apollo
import io.iohk.atala.prism.domain.models.CastorError
import io.iohk.atala.prism.domain.models.CompressedPublicKey
import io.iohk.atala.prism.domain.models.PublicKey
import io.iohk.atala.prism.protos.CompressedECKeyData
import io.iohk.atala.prism.protos.KeyUsage
import pbandk.ByteArr

class PrismDIDPublicKey {
    enum class Usage(val value: String) {
        MASTER_KEY("masterKey"),
        ISSUING_KEY("issuingKey"),
        AUTHENTICATION_KEY("authenticationKey"),
        REVOCATION_KEY("revocationKey"),
        CAPABILITY_DELEGATION_KEY("capabilityDelegationKey"),
        CAPABILITY_INVOCATION_KEY("capabilityInvocationKey"),
        KEY_AGREEMENT_KEY("keyAgreementKey"),
        UNKNOWN_KEY("unknownKey");

        val defaultId: String
            get() = id(0)

        fun toProto(): KeyUsage {
            return when (this) {
                MASTER_KEY -> KeyUsage.MASTER_KEY
                ISSUING_KEY -> KeyUsage.ISSUING_KEY
                AUTHENTICATION_KEY -> KeyUsage.AUTHENTICATION_KEY
                REVOCATION_KEY -> KeyUsage.REVOCATION_KEY
                CAPABILITY_DELEGATION_KEY -> KeyUsage.CAPABILITY_DELEGATION_KEY
                CAPABILITY_INVOCATION_KEY -> KeyUsage.CAPABILITY_INVOCATION_KEY
                KEY_AGREEMENT_KEY -> KeyUsage.KEY_AGREEMENT_KEY
                UNKNOWN_KEY -> KeyUsage.UNKNOWN_KEY
            }
        }

        fun id(index: Int): String {
            return when (this) {
                MASTER_KEY -> "master$index"
                ISSUING_KEY -> "issuing$index"
                AUTHENTICATION_KEY -> "authentication$index"
                REVOCATION_KEY -> "revocation$index"
                CAPABILITY_DELEGATION_KEY -> "capabilityDelegation$index"
                CAPABILITY_INVOCATION_KEY -> "capabilityInvocation$index"
                KEY_AGREEMENT_KEY -> "keyAgreement$index"
                UNKNOWN_KEY -> "unknown$index"
            }
        }
    }

    private val apollo: Apollo
    val id: String
    val usage: Usage
    val keyData: PublicKey

    constructor(apollo: Apollo, id: String, usage: Usage, keyData: PublicKey) {
        this.apollo = apollo
        this.id = id
        this.usage = usage
        this.keyData = keyData
    }

    constructor(apollo: Apollo, proto: io.iohk.atala.prism.protos.PublicKey) {
        this.apollo = apollo
        this.id = proto.id
        this.usage = proto.usage.fromProto()
        this.keyData = when (proto.keyData) {
            is io.iohk.atala.prism.protos.PublicKey.KeyData.CompressedEcKeyData -> {
                apollo.compressedPublicKey(compressedData = proto.keyData.value.data.array).uncompressed
            }
            else -> {
                throw CastorError.InvalidPublicKeyEncoding()
            }
        }
    }

    fun toProto(): io.iohk.atala.prism.protos.PublicKey {
        val compressed = apollo.compressedPublicKey(keyData)
        return io.iohk.atala.prism.protos.PublicKey(
            id = id,
            usage = usage.toProto(),
            keyData = io.iohk.atala.prism.protos.PublicKey.KeyData.CompressedEcKeyData(
                compressed.toProto()
            )
        )
    }
}

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

fun CompressedPublicKey.toProto(): CompressedECKeyData {
    return CompressedECKeyData(
        curve = uncompressed.curve.curve.value,
        data = ByteArr(value)
    )
}
