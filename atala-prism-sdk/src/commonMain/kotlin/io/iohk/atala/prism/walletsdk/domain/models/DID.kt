package io.iohk.atala.prism.walletsdk.domain.models

import io.iohk.atala.prism.walletsdk.domain.DID
import io.iohk.atala.prism.walletsdk.domain.DID_SEPARATOR
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic

/**
 * A DID is a unique and persistent identifier for a subject or object, such as a person, organization, or device.
 * It is created and managed using a specific DID method, and consists of a schema, method, and method ID.
 * The schema indicates the type of DID (e.g. "did"), the method indicates the specific interface or process used to resolve
 * and manage the DID (e.g. "prism"), and the method ID is a unique identifier within the DID method.
 * As specified in the [W3C DID standards](https://www.w3.org/TR/did-core/#dfn-did-schemes).
 */
@Serializable
data class DID @JvmOverloads constructor(
    val schema: String = DID,
    val method: String,
    val methodId: String
) {

    // @JsName("fromString")
    constructor(
        string: String
    ) : this(getSchemaFromString(string), getMethodFromString(string), getMethodIdFromString(string))

    override fun toString(): String {
        return "$schema:$method:$methodId"
    }

    companion object {
        @JvmStatic
        fun getSchemaFromString(string: String): String {
            val split = string.split(DID_SEPARATOR)
            return split[0]
        }

        @JvmStatic
        fun getMethodFromString(string: String): String {
            val split = string.split(DID_SEPARATOR)
            return split[1]
        }

        @JvmStatic
        fun getMethodIdFromString(string: String): String {
            var split = string.split(DID_SEPARATOR).toMutableList()
            split.removeFirst()
            split.removeFirst()
            return split.joinToString(DID_SEPARATOR)
        }
    }
}
