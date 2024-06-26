package org.hyperledger.identus.walletsdk.pollux

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.double
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.float
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlinx.serialization.json.longOrNull

class DescriptorPath(private val jsonElement: JsonElement) {

    fun getValue(path: String): Any? {
        val regex = Regex("[\\[\\]`.]+")
        val segments = path.split(regex).filter { it.isNotEmpty() }.drop(1)
        val jsonObject = jsonElement.jsonObject

        var current: Any? = jsonObject
        segments.forEach { segment ->
            when (current) {
                is JsonObject -> {
                    if ((current as JsonObject).contains(segment)) {
                        val value = (current as JsonObject).get(segment)
                        current = value
                    } else {
                        current = null
                    }
                }

                is JsonArray -> {
                    val index = segment.toInt()
                    current = if (index >= (current as JsonArray).size) {
                        null
                    } else {
                        (current as JsonArray)[index]
                    }
                }

                is JsonPrimitive -> {
                    val jp = current as JsonPrimitive
                    when {
                        jp.jsonPrimitive.isString -> jp.jsonPrimitive.content
                        jp.jsonPrimitive.floatOrNull != null -> jp.jsonPrimitive.float
                        jp.jsonPrimitive.intOrNull != null -> jp.jsonPrimitive.int
                        jp.jsonPrimitive.doubleOrNull != null -> jp.jsonPrimitive.double
                        jp.jsonPrimitive.longOrNull != null -> jp.jsonPrimitive.long
                        else -> jp.jsonPrimitive.content
                    }
                }
            }
        }
        if (current != null) {
            return if (current is JsonPrimitive) {
                if ((current as JsonPrimitive).isString) {
                    (current as JsonPrimitive).jsonPrimitive.content
                } else {
                    (current as JsonPrimitive).int
                }
            } else {
                current
            }
        }
        return null
    }
}
