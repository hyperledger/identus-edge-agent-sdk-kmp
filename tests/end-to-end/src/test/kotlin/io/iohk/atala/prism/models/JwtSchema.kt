package io.iohk.atala.prism.models

import com.google.gson.annotations.SerializedName

data class JwtSchema (
    @SerializedName("\$id")
    var id: String = "",

    @SerializedName("\$schema")
    var schema: String = "",

    @SerializedName("\$description")
    var description: String = "",

    @SerializedName("type")
    var type: String = "",

    @SerializedName("properties")
    val properties: MutableMap<String, JwtSchemaProperty> = mutableMapOf()
)
