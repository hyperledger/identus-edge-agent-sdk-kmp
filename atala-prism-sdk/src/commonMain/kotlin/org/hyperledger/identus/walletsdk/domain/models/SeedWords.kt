package org.hyperledger.identus.walletsdk.domain.models

import kotlinx.serialization.Serializable

/**
 * Represents a set of seed words along with the corresponding seed object.
 *
 * @property mnemonics An array of seed words.
 * @property seed The seed object used for key generation.
 */
@Serializable
data class SeedWords(val mnemonics: Array<String>, val seed: Seed) {
    /**
     * Compares this SeedWords object to the specified [other] object for equality.
     *
     * Two SeedWords objects are considered equal if they have the same [mnemonics] and [seed] properties.
     *
     * @param other The object to compare for equality.
     * @return `true` if the objects are equal, `false` otherwise.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as SeedWords

        if (!mnemonics.contentEquals(other.mnemonics)) return false
        if (seed != other.seed) return false

        return true
    }

    /**
     * Calculates the hash code for the SeedWords object.
     *
     * The hash code is calculated based on the array of mnemonics and the seed object.
     *
     * @return The hash code value for the SeedWords object.
     */
    override fun hashCode(): Int {
        var result = mnemonics.contentHashCode()
        result = 31 * result + seed.hashCode()
        return result
    }
}
