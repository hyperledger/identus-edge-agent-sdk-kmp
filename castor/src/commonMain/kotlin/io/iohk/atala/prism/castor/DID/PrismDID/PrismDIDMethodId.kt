package io.iohk.atala.prism.castor.did.prismdid

import io.iohk.atala.prism.domain.models.CastorError

data class PrismDIDMethodId(private val value: String) {
    val sections: List<String>
        get() = value.split(":").map { it }

    override fun toString(): String {
        return value
    }

    constructor(sections: List<String>) : this(sections.joinToString(":")) {
        val sectionRegex = Regex("^[A-Za-z0-9_-]+$")
        if (!sections.all { sectionRegex.matches(it) }) {
            throw CastorError.MethodIdIsDoesNotSatisfyRegex()
        }
        val methodSpecificIdRegex = Regex("^([A-Za-z0-9_-]*:)*[A-Za-z0-9_-]+$")
        if (!methodSpecificIdRegex.matches(value)) {
            throw CastorError.MethodIdIsDoesNotSatisfyRegex()
        }
    }
}
