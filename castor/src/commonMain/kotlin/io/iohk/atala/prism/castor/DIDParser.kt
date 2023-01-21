package io.iohk.atala.prism.castor

import io.iohk.atala.prism.castor.DIDGrammar.DIDAbnfLexer
import io.iohk.atala.prism.castor.DIDGrammar.DIDAbnfParser
import io.iohk.atala.prism.castor.DIDGrammar.InvalidDIDStringError
import io.iohk.atala.prism.domain.models.DID
import org.antlr.v4.kotlinruntime.CharStreams
import org.antlr.v4.kotlinruntime.CommonTokenStream
import org.antlr.v4.kotlinruntime.DefaultErrorStrategy
import org.antlr.v4.kotlinruntime.Parser
import org.antlr.v4.kotlinruntime.ParserRuleContext
import org.antlr.v4.kotlinruntime.RecognitionException
import org.antlr.v4.kotlinruntime.Token
import org.antlr.v4.kotlinruntime.tree.ParseTree
import org.antlr.v4.kotlinruntime.tree.ParseTreeWalker

class BailErrorStrategy : DefaultErrorStrategy() {
    override fun recover(recognizer: Parser, e: RecognitionException) {
        var context = recognizer.context
        while (context != null) {
            context!!.exception = e
            context = context!!.readParent() as ParserRuleContext?
        }

        throw e
    }

    override fun recoverInline(recognizer: Parser): Token {
        var context = recognizer.context
        while (context != null) {
            context = context!!.readParent() as ParserRuleContext?
        }
        throw InvalidDIDStringError("Invalid Did char found at [line ${recognizer.currentToken?.line}, col ${recognizer.currentToken?.charPositionInLine}] \"${recognizer.currentToken?.text}\"")
    }

    override fun sync(recognizer: Parser) {}
}

class DIDParser(var didString: String) {
    fun parse(): DID {
        var inputStream = CharStreams.fromString(didString)
        var lexer = DIDAbnfLexer(inputStream)
        var tokenStream = CommonTokenStream(lexer)
        var parser = DIDAbnfParser(tokenStream)

        parser.errorHandler = BailErrorStrategy()

        var context = parser.did()
        var listener = DIDParserListener()
        ParseTreeWalker().walk(listener, context as ParseTree)

        if (listener.scheme.isNullOrEmpty() ||
            listener.methodName.isNullOrEmpty() ||
            listener.methodId.isNullOrEmpty()
        ) {
            throw InvalidDIDStringError("InvalidDIDStringError error")
        }

        var scheme: String = listener.scheme!!
        var methodName = listener.methodName!!
        var methodId = listener.methodId!!
        return DID(scheme, methodName, methodId)
    }
}
