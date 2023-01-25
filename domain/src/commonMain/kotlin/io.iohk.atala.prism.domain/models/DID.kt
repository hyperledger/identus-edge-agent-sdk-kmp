package io.iohk.atala.prism.domain.models

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmStatic

@Serializable
data class DID(
    val schema: String,
    val method: String,
    val methodId: String
) {

    constructor(
        string: String
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
