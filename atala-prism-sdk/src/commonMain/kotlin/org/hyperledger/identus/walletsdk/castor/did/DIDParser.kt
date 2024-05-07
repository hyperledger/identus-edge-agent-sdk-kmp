package org.hyperledger.identus.walletsdk.castor.did

import org.hyperledger.identus.walletsdk.domain.models.CastorError
import org.hyperledger.identus.walletsdk.domain.models.DID
import kotlin.jvm.Throws

/**
 * The DIDParser class provides methods for parsing a string representation of a Decentralized Identifier (DID) into a DID object.
 */
object DIDParser {

    /**
     * Parses a string representation of a Decentralized Identifier (DID) into a DID object.
     *
     * @param didString The string representation of the DID.
     * @return The [DID] object.
     * @throws [CastorError.InvalidDIDString] if the string is not a valid DID.
     */
    @Throws(CastorError.InvalidDIDString::class)
    fun parse(didString: String): DID {
        val regex =
            """^did:(?<method>[a-z0-9]+):(?<idstring>[a-z0-9.\-_%]+:*[a-z0-9.\-_%]+[^#?:]+)$""".toRegex(RegexOption.IGNORE_CASE)
        val matchResult = regex.find(didString)
        matchResult?.let {
            val scheme = "did"
            val methodName = it.groups["method"]?.value
                ?: throw CastorError.InvalidDIDString("Invalid DID string, missing method name")
            val methodId = it.groups["idstring"]?.value
                ?: throw CastorError.InvalidDIDString("Invalid DID string, missing method ID")
            return DID(scheme, methodName, methodId)
        } ?: throw CastorError.InvalidDIDString("DID string does not match the expected structure.")
    }
}
