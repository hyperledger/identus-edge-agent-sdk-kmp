package io.iohk.atala.prism.walletsdk.castor.did

import io.iohk.atala.prism.walletsdk.castor.antlrgrammar.DIDUrlAbnfLexer
import io.iohk.atala.prism.walletsdk.castor.antlrgrammar.DIDUrlAbnfParser
import io.iohk.atala.prism.walletsdk.domain.models.CastorError
import io.iohk.atala.prism.walletsdk.domain.models.DID
import io.iohk.atala.prism.walletsdk.domain.models.DIDUrl
import org.antlr.v4.kotlinruntime.CharStreams
import org.antlr.v4.kotlinruntime.CommonTokenStream
import org.antlr.v4.kotlinruntime.tree.ParseTree
import org.antlr.v4.kotlinruntime.tree.ParseTreeWalker
import kotlin.jvm.Throws

object DIDUrlParser {
    @Throws(CastorError.InvalidDIDString::class)
    fun parse(didUrlString: String): DIDUrl {
        val inputStream = CharStreams.fromString(didUrlString)
        val lexer = DIDUrlAbnfLexer(inputStream)
        val tokenStream = CommonTokenStream(lexer)
        val parser = DIDUrlAbnfParser(tokenStream)

        parser.errorHandler = ErrorStrategy()

        val context = parser.did_url()
        val listener = DIDUrlParserListener()
        ParseTreeWalker().walk(listener, context as ParseTree)

        val scheme = listener.scheme ?: throw CastorError.InvalidDIDString("Invalid DID string, missing scheme")
        val methodName = listener.methodName ?: throw CastorError.InvalidDIDString("Invalid DID string, missing method name")
        val methodId = listener.methodId ?: throw CastorError.InvalidDIDString("Invalid DID string, missing method ID")

        val did = DID(scheme, methodName, methodId)

        return DIDUrl(
            did,
            listener.path ?: emptyArray(),
            listener.query,
            listener.fragment,
        )
    }
}
