package org.hyperledger.identus.walletsdk.domain.models

import org.hyperledger.identus.walletsdk.domain.DID_URL_SEPARATOR
import kotlin.jvm.JvmOverloads

/**
 * Represents a DIDUrl with "did", "path", "parameters", "fragment"
 * As specified in [w3 standards](https://www.w3.org/TR/did-core/#dfn-did-urls)
 */
data class DIDUrl @JvmOverloads constructor(
    val did: DID,
    val path: Array<String>? = arrayOf(),
    val parameters: Map<String, String>? = mapOf(),
    val fragment: String? = null
) {

    /**
     * Returns a string representation of the DID URL.
     *
     * @return The string representation of the DID URL.
     */
    fun string(): String {
        return "${did}${fragmentString()}"
    }

    /**
     * Returns the path portion of a DID URL as a string.
     * If the path is null, an empty string is returned.
     *
     * @return The path portion of a DID URL.
     */
    fun pathString(): String {
        return "/${path?.joinToString(DID_URL_SEPARATOR)}"
    }

    /**
     * Generates a query string based on the provided parameters.
     *
     * @return The query string in the format "?key1=value1&key2=value2&..."
     */
    fun queryString(): String {
        return "?${parameters?.map { "${it.key}=${it.value}" }?.joinToString("&")}"
    }

    /**
     * Returns the fragment portion of a DID URL as a string.
     *
     * @return The fragment portion of a DID URL.
     */
    fun fragmentString(): String {
        return "#$fragment"
    }

    /**
     * Checks if the current [DIDUrl] instance is equal to the specified [other] object.
     *
     * @param other The object to compare with the current [DIDUrl].
     * @return `true` if the objects are equal, `false` otherwise.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as DIDUrl

        if (did != other.did) return false
        if (path != null) {
            if (other.path == null) return false
            if (!path.contentEquals(other.path)) return false
        } else if (other.path != null) return false
        if (parameters != other.parameters) return false
        if (fragment != other.fragment) return false

        return true
    }

    /**
     * Calculates the hash code for the current [DIDUrl] instance.
     *
     * @return The hash code value for the [DIDUrl] object.
     */
    override fun hashCode(): Int {
        var result = did.hashCode()
        result = 31 * result + (path?.contentHashCode() ?: 0)
        result = 31 * result + (parameters?.hashCode() ?: 0)
        result = 31 * result + (fragment?.hashCode() ?: 0)
        return result
    }

    /**
     * Returns a string representation of the current [DIDUrl] instance.
     *
     * @return The string representation of the DID URL.
     */
    override fun toString(): String {
        return string()
    }
}
