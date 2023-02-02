package io.iohk.atala.prism.castor

import io.iohk.atala.prism.domain.models.CastorError

class PrismDIDMethodId {

    private var value: String
    val sections: List<String>
        get() = value.split(":")

    override fun toString(): String {
        return this.value
    }

    constructor(sections: List<String>) {
        val sectionRegex = Regex("^[A-Za-z0-9_-]+$")
        if (!sections.all { sectionRegex.matches(it) }) {
            throw CastorError.MethodIdIsDoesNotSatisfyRegex()
        }
        this.value = sections.joinToString(":")
    }

    constructor(string: String) {
        val methodSpecificIdRegex = Regex("^([A-Za-z0-9_-]*:)*[A-Za-z0-9_-]+$")
        if (!methodSpecificIdRegex.matches(string)) {
            throw CastorError.MethodIdIsDoesNotSatisfyRegex()
        }
        this.value = string
    }
}
