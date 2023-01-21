package io.iohk.atala.prism.castor

import io.iohk.atala.prism.castor.DIDGrammar.DIDAbnfBaseListener
import io.iohk.atala.prism.castor.DIDGrammar.DIDAbnfParser

class DIDParserListener : DIDAbnfBaseListener() {

    var scheme: String? = null
    var methodName: String? = null
    var methodId: String? = null

    override fun exitDid(ctx: DIDAbnfParser.DidContext) {
        ctx.SCHEMA()?.let {
            scheme = it.text
        }
    }

    override fun exitMethod_name(ctx: DIDAbnfParser.Method_nameContext) {
        methodName = ctx.text
    }

    override fun exitMethod_specific_id(ctx: DIDAbnfParser.Method_specific_idContext) {
        methodId = ctx.text
    }
}



