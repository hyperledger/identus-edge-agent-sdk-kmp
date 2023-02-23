package io.iohk.atala.prism.walletsdk.domain.models

import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@Serializable
@JsExport
data class SeedWords(val mnemonics: Array<String>, val seed: Seed) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as SeedWords

        if (!mnemonics.contentEquals(other.mnemonics)) return false
        if (seed != other.seed) return false

        return true
    }

    override fun hashCode(): Int {
        var result = mnemonics.contentHashCode()
        result = 31 * result + seed.hashCode()
        return result
    }
}
