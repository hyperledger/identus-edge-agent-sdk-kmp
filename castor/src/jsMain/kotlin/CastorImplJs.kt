import io.iohk.atala.prism.castor.CastorImpl
import io.iohk.atala.prism.domain.models.DIDDocument
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.promise
import kotlin.js.Promise

@JsExport
@JsName("CastorImpl")
class CastorImplJs : CastorImpl() {

    @JsName("resolveDID")
    fun resolveDIDJS(did: String): Promise<DIDDocument> {
        return GlobalScope.promise { resolveDID(did) }
    }
}
