package io.iohk.atala.prism.domain.models

data class DIDMethod(
    val method: String
)

data class DIDMethodId(
    val id: String
)

data class DID(
    val schema: String,
    val method: String,
    val methodId: String
) {

    constructor(
        string: String
    ) : this(getSchemaFromString(string), getMethodFromString(string), getMethodIdFromString(string))

    companion object {
        fun getSchemaFromString(string: String): String {
            val split = string.split(":")
            return split[0]
        }

        fun getMethodFromString(string: String): String {
            val split = string.split(":")
            return split[1]
        }

        fun getMethodIdFromString(string: String): String {
            val split = string.split(":")
            return split[2]
        }
    }
}
