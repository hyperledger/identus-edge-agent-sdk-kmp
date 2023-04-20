package io.iohk.atala.prism.walletsdk.castor.did.prismdid

import io.iohk.atala.prism.protos.CompressedECKeyData
import io.iohk.atala.prism.protos.KeyUsage
import io.iohk.atala.prism.walletsdk.domain.buildingblocks.Apollo
import io.iohk.atala.prism.walletsdk.domain.models.CastorError
import io.iohk.atala.prism.walletsdk.domain.models.CompressedPublicKey
import io.iohk.atala.prism.walletsdk.domain.models.PublicKey
import pbandk.ByteArr
import kotlin.jvm.Throws

class PrismDIDPublicKey {
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

    @Throws(CastorError.InvalidPublicKeyEncoding::class)
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
                compressed.toProto(),
            ),
        )
    }

    enum class Usage(val value: String) {
        MASTER_KEY("masterKey"),
        ISSUING_KEY("issuingKey"),
        AUTHENTICATION_KEY("authenticationKey"),
        REVOCATION_KEY("revocationKey"),
        CAPABILITY_DELEGATION_KEY("capabilityDelegationKey"),
        CAPABILITY_INVOCATION_KEY("capabilityInvocationKey"),
        KEY_AGREEMENT_KEY("keyAgreementKey"),
        UNKNOWN_KEY("unknownKey"),
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
        data = ByteArr(value),
    )
}

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

fun PrismDIDPublicKey.Usage.defaultId(): String {
    return this.id(0)
}
