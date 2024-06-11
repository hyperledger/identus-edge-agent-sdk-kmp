package org.hyperledger.identus.walletsdk.models

import com.google.gson.annotations.SerializedName

data class AnoncredSchema(
    @SerializedName("name")
    var name: String = "",

    @SerializedName("version")
    var version: String =  "",

    @SerializedName("attrNames")
    var attrNames: MutableList<String> = mutableListOf(),

    @SerializedName("issuerId")
    var issuerId: String = ""
)
