import io.iohk.atala.prism.authenticatesdk.authenticate
import io.iohk.atala.prism.authenticatesdk.createChallenge

@JsExport
@JsName("authenticate")
fun jsAuthenticate(did: String, signature: String, originalText: String): String {
    return authenticate(did, signature, originalText)
}

@JsExport
@JsName("createChallenge")
fun jsCreateChallenge(expiration: Int): String {
    return createChallenge(expiration)
}