package io.iohk.atala.prism.castor

import io.iohk.atala.prism.castor.antlrgrammar.DIDUrlAbnfBaseListener
import io.iohk.atala.prism.castor.antlrgrammar.DIDUrlAbnfParser

class DIDUrlParserListener : DIDUrlAbnfBaseListener() {

    var scheme: String? = null
    var methodName: String? = null
    var methodId: String? = null
    var path: Array<String>? = null
    var query = HashMap<String, String>()
    var fragment: String? = null

    override fun exitDid(ctx: DIDUrlAbnfParser.DidContext) {
        scheme = ctx.SCHEMA()?.text
    }

    override fun exitMethod_name(ctx: DIDUrlAbnfParser.Method_nameContext) {
        methodName = ctx.text
    }

    override fun exitMethod_specific_id(ctx: DIDUrlAbnfParser.Method_specific_idContext) {
        methodId = ctx.text
    }

    override fun exitPath(ctx: DIDUrlAbnfParser.PathContext) {
        if (ctx.isEmpty) return
        path = ctx.children?.map { it.text }?.filter { it != "/" }?.toTypedArray()
    }

    override fun exitFrag(ctx: DIDUrlAbnfParser.FragContext) {
        if (ctx.isEmpty) return
        fragment = ctx.text
    }

    override fun exitSearchparameter(ctx: DIDUrlAbnfParser.SearchparameterContext) {
        if (ctx.isEmpty) return
        val key = ctx.children?.get(0)!!.text
        val value = ctx.children?.get(2)!!.text
        query[key] = value
    }

}
