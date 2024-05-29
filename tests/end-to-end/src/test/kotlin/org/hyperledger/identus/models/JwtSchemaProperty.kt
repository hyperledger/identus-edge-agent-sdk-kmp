package org.hyperledger.identus.models

import com.google.gson.annotations.SerializedName

data class JwtSchemaProperty (
    @SerializedName("type")
    var type: String = ""
)
