package io.iohk.atala.prism.castor

import org.antlr.v4.kotlinruntime.DefaultErrorStrategy
import org.antlr.v4.kotlinruntime.Parser
import org.antlr.v4.kotlinruntime.ParserRuleContext
import org.antlr.v4.kotlinruntime.RecognitionException
import org.antlr.v4.kotlinruntime.Token

class ErrorStrategy : DefaultErrorStrategy() {
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