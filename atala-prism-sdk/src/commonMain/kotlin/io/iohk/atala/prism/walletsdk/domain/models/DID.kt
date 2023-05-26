package io.iohk.atala.prism.walletsdk.domain.models

import io.iohk.atala.prism.walletsdk.domain.DID
import io.iohk.atala.prism.walletsdk.domain.DID_SEPARATOR
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic

@Serializable
data class DID @JvmOverloads constructor(
    val schema: String = DID,
    val method: String,
    val methodId: String,
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
