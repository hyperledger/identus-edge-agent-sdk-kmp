package io.iohk.atala.prism.walletsdk.domain.models

data class PeerDID(
    val did: DID,
    val privateKeys: Array<PrivateKey>,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as PeerDID

        if (did != other.did) return false
        if (!privateKeys.contentEquals(other.privateKeys)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = did.hashCode()
        result = 31 * result + privateKeys.contentHashCode()
        return result
    }
}
