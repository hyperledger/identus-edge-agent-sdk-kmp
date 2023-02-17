package io.iohk.atala.prism.walletsdk.domain.models

import kotlinx.serialization.Serializable
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlin.js.JsName
import kotlin.jvm.JvmStatic

@OptIn(ExperimentalJsExport::class)
@Serializable
@JsExport
data class DID(
    val schema: String = "did",
    val method: String,
    val methodId: String,
) {

    @JsName("fromString")
    constructor(
        string: String,
    ) : this(getSchemaFromString(string), getMethodFromString(string), getMethodIdFromString(string))

    override fun toString(): String {
        return "$schema:$method:$methodId"
    }

    companion object {
        @JvmStatic
        fun getSchemaFromString(string: String): String {
            val split = string.split(":")
            return split[0]
        }

        @JvmStatic
        fun getMethodFromString(string: String): String {
            val split = string.split(":")
            return split[1]
        }

        @JvmStatic
        fun getMethodIdFromString(string: String): String {
            val split = string.split(":")
            return split[2]
        }
    }
}
