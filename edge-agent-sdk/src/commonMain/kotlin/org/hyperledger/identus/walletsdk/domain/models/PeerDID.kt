package org.hyperledger.identus.walletsdk.domain.models

import org.hyperledger.identus.walletsdk.domain.models.keyManagement.PrivateKey

/**
 * Represents a PeerDID, which is used as a unique and persistent identifier for a subject or object.
 * It consists of a [DID] and an array of [PrivateKey]s.
 *
 * @property did The [DID] associated with the PeerDID.
 * @property privateKeys The array of [PrivateKey]s associated with the PeerDID.
 */
data class PeerDID(
    val did: DID,
    val privateKeys: Array<out PrivateKey>
) {
    /**
     * Compares this [PeerDID] object with the specified [other] object for equality.
     *
     * @param other The object to compare with this [PeerDID] object.
     * @return `true` if the [other] object is equal to this [PeerDID] object, `false` otherwise.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as PeerDID

        if (did != other.did) return false
        if (!privateKeys.contentEquals(other.privateKeys)) return false

        return true
    }

    /**
     * Calculates the hash code value for this PeerDID object.
     *
     * The hash code is calculated using the `did` and `privateKeys` properties of the PeerDID object.
     * The hash code value is obtained by adding the hash code of the `did` property to the result variable,
     * then multiplying the result by 31 and adding the hash code of the `privateKeys` array using the `contentHashCode()` method.
     *
     * @return The hash code value for this PeerDID object.
     */
    override fun hashCode(): Int {
        var result = did.hashCode()
        result = 31 * result + privateKeys.contentHashCode()
        return result
    }
}
