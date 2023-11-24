package io.iohk.atala.prism.walletsdk.castor.did

import io.iohk.atala.prism.walletsdk.domain.models.CastorError
import io.iohk.atala.prism.walletsdk.domain.models.DID
import kotlin.jvm.Throws

object DIDParser {

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
