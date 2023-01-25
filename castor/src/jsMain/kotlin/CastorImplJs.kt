import io.iohk.atala.prism.castor.CastorImpl
import io.iohk.atala.prism.domain.models.DID
import io.iohk.atala.prism.domain.models.DIDDocument
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.promise
import kotlin.js.Promise

@JsExport
@JsName("Castor")
class CastorImplJs : CastorImpl() {

    @JsName("resolveDID")
    fun resolveDIDJS(did: DID): Promise<DIDDocument> {
        return GlobalScope.promise { resolveDID(did) }
    }
}
