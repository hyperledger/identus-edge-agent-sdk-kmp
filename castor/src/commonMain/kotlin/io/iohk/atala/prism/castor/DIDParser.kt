package io.iohk.atala.prism.castor

import io.iohk.atala.prism.castor.antlrGrammar.DIDAbnfLexer
import io.iohk.atala.prism.castor.antlrGrammar.DIDAbnfParser
import io.iohk.atala.prism.castor.antlrGrammar.InvalidDIDStringError
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
            context.exception = e
            context = context.readParent() as ParserRuleContext?
        }

        throw e
    }

    override fun recoverInline(recognizer: Parser): Token {
        var context = recognizer.context
        while (context != null) {
            context = context.readParent() as ParserRuleContext?
        }
        throw InvalidDIDStringError("Invalid Did char found at [line ${recognizer.currentToken?.line}, col ${recognizer.currentToken?.charPositionInLine}] \"${recognizer.currentToken?.text}\"")
    }

    override fun sync(recognizer: Parser) {}
}

class DIDParser(private var didString: String) {
    fun parse(): DID {
        var inputStream = CharStreams.fromString(didString)
        val lexer = DIDAbnfLexer(inputStream)
        val tokenStream = CommonTokenStream(lexer)
        val parser = DIDAbnfParser(tokenStream)

        parser.errorHandler = BailErrorStrategy()

        val context = parser.did()
        val listener = DIDParserListener()
        ParseTreeWalker().walk(listener, context as ParseTree)

        val scheme = listener.scheme ?: throw InvalidDIDStringError("Invalid DID string, missing scheme")
        val methodName = listener.methodName ?: throw InvalidDIDStringError("Invalid DID string, missing method name")
        val methodId = listener.methodId ?: throw InvalidDIDStringError("Invalid DID string, missing method ID")

        return DID(scheme, methodName, methodId)
    }
}
