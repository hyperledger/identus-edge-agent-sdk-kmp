package io.iohk.atala.prism.walletsdk.castor.did.prismdid

import io.iohk.atala.prism.walletsdk.castor.DID_SEPARATOR
import io.iohk.atala.prism.walletsdk.domain.models.CastorError
import kotlin.jvm.Throws

data class PrismDIDMethodId(private val value: String) {
    val sections: List<String>
        get() = value.split(DID_SEPARATOR).map { it }

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

    override fun toString(): String {
        return value
    }
}
