package org.hyperledger.identus.walletsdk.domain.models

import kotlinx.serialization.Serializable
import org.hyperledger.identus.walletsdk.domain.DID
import org.hyperledger.identus.walletsdk.domain.DID_SEPARATOR
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic

/**
 * A DID is a unique and persistent identifier for a subject or object, such as a person, organization, or device.
 * It is created and managed using a specific DID method, and consists of a schema, method, and method ID.
 * The schema indicates the type of DID (e.g. "did"), the method indicates the specific interface or process used to resolve
 * and manage the DID (e.g. "prism"), and the method ID is a unique identifier within the DID method.
 * As specified in the [W3C DID standards](https://www.w3.org/TR/did-core/#dfn-did-schemes).
 */
@Serializable
data class DID @JvmOverloads constructor(
    val schema: String = DID,
    val method: String,
    val methodId: String,
    val alias: String? = null
) {
    /**
     * Constructor overload for creating a [DID] object based on a [String].
     *
     * @param string The input [String] from which the [DID] object will be created.
     * The [String] should have the format "schema:method:methodId".
     */
    constructor(
        string: String
    ) : this(getSchemaFromString(string), getMethodFromString(string), getMethodIdFromString(string))

    /**
     * Returns a string representation of the DID object.
     *
     * @return The string representation of the DID in the format "schema:method:methodId".
     */
    override fun toString(): String {
        return "$schema:$method:$methodId"
    }

    companion object {
        /**
         * Extracts the schema from the given string representation of a DID.
         *
         * @param string The input string in the format "schema:method:methodId".
         * @return The extracted schema from the input string.
         */
        @JvmStatic
        fun getSchemaFromString(string: String): String {
            val split = string.split(DID_SEPARATOR)
            return split[0]
        }

        /**
         * Retrieves the method component from a given string representation of a DID.
         *
         * @param string The input string in the format "schema:method:methodId".
         * @return The extracted method component from the input string.
         */
        @JvmStatic
        fun getMethodFromString(string: String): String {
            val split = string.split(DID_SEPARATOR)
            return split[1]
        }

        /**
         * Retrieves the methodId component from a given string representation of a DID.
         *
         * @param string The input string in the format "schema:method:methodId".
         * @return The extracted methodId component from the input string.
         */
        @JvmStatic
        fun getMethodIdFromString(string: String): String {
            val split = string.split(DID_SEPARATOR).toMutableList()
            split.removeFirst()
            split.removeFirst()
            return split.joinToString(DID_SEPARATOR)
        }
    }
}
