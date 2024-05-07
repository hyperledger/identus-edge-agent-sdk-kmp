package org.hyperledger.identus.walletsdk.castor.did.prismdid

import org.hyperledger.identus.walletsdk.castor.DID_SEPARATOR
import org.hyperledger.identus.walletsdk.domain.models.CastorError
import kotlin.jvm.Throws

/**
 * Represents a Prism DID Method ID.
 *
 * @property value The string value of the Prism DID Method ID.
 */
data class PrismDIDMethodId(private val value: String) {
    val sections: List<String>
        get() = value.split(DID_SEPARATOR).map { it }

    /**
     * Represents a Prism DID Method ID.
     *
     * @property value The string value of the Prism DID Method ID.
     */
    @Throws(CastorError.MethodIdIsDoesNotSatisfyRegex::class)
    constructor(sections: List<String>) : this(sections.joinToString(DID_SEPARATOR)) {
        val sectionRegex = Regex("^[A-Za-z0-9_-]+$")
        if (!sections.all { sectionRegex.matches(it) }) {
            throw CastorError.MethodIdIsDoesNotSatisfyRegex("^[A-Za-z0-9_-]+$")
        }
        val methodSpecificIdRegex = Regex("^([A-Za-z0-9_-]*:)*[A-Za-z0-9_-]+$")
        if (!methodSpecificIdRegex.matches(value)) {
            throw CastorError.MethodIdIsDoesNotSatisfyRegex("^([A-Za-z0-9_-]*:)*[A-Za-z0-9_-]+\$")
        }
    }

    /**
     * Returns a string representation of the Prism DID Method ID.
     *
     * @return The string representation of the Prism DID Method ID.
     */
    override fun toString(): String {
        return value
    }
}
