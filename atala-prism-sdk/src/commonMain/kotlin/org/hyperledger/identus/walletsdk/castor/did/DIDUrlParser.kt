package org.hyperledger.identus.walletsdk.castor.did

import org.hyperledger.identus.walletsdk.domain.models.CastorError
import org.hyperledger.identus.walletsdk.domain.models.DID
import org.hyperledger.identus.walletsdk.domain.models.DIDUrl

/**
 * Class responsible for parsing a DID URL string and returning a parsed [DIDUrl] object.
 */
object DIDUrlParser {
    /**
     * Parses a DID URL string and returns a `DIDUrl` object.
     *
     * @param didUrlString The input DID URL string to parse.
     * @return A `DIDUrl` object representing the parsed DID URL.
     * @throws CastorError.InvalidDIDString if the input DID string does not match the expected structure.
     */
    @Throws(CastorError.InvalidDIDString::class)
    fun parse(didUrlString: String): DIDUrl {
        val regex =
            """^did:(?<method>[a-z0-9]+)(?::(?<idstring>[^#?/]*))?(?<path>[^#?]*)?(?<query>\?[^#]*)?(?<fragment>#.*)?$""".toRegex(
                RegexOption.IGNORE_CASE
            )
        val matchResult = regex.find(didUrlString)

        matchResult?.let { it ->
            val method = it.groups["method"]?.value
                ?: throw CastorError.InvalidDIDString("Invalid DID string, missing method name")
            val idString = it.groups["idstring"]?.value
                ?: throw CastorError.InvalidDIDString("Invalid DID string, missing method ID")
            val path =
                it.groups["path"]?.value ?: throw CastorError.InvalidDIDString("Invalid DID string, missing path")
            val query = it.groups["query"]?.value ?: ""
            val fragment = it.groups["fragment"]?.value ?: ""
            val attributes = if (query.isNotEmpty()) {
                query.removePrefix("?").split("&")
                    .associate {
                        val (key, value) = it.split("=")
                        key to value
                    }
            } else {
                mapOf()
            }
            val paths = path.split("/").filter { it.isNotEmpty() }.toTypedArray()
            val did = DID("did", method, idString)
            val fragmentValue = if (fragment.isNotEmpty()) fragment.removePrefix("#") else null
            return DIDUrl(did, paths, attributes, fragmentValue)
        } ?: throw CastorError.InvalidDIDString("DID string does not match the expected structure.")
    }
}
