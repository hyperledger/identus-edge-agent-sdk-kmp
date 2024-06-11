package org.hyperledger.identus.walletsdk.models

import com.google.gson.annotations.SerializedName

data class JwtSchemaProperty (
    @SerializedName("type")
    var type: String = ""
)
