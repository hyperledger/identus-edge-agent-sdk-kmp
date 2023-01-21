// Generated from java-escape by ANTLR 4.7.1
package io.iohk.atala.prism.castor.antlrGrammar

import com.strumenta.kotlinmultiplatform.asCharArray
import org.antlr.v4.kotlinruntime.*
import org.antlr.v4.kotlinruntime.atn.ATN
import org.antlr.v4.kotlinruntime.atn.ATNDeserializer
import org.antlr.v4.kotlinruntime.atn.LexerATNSimulator
import org.antlr.v4.kotlinruntime.atn.PredictionContextCache
import org.antlr.v4.kotlinruntime.dfa.DFA

open class DIDAbnfLexer(val input: CharStream) : Lexer(input) {

    override val ruleNames: Array<String>?
        get() = Rules.values().map { it.name }.toTypedArray()

    override val grammarFileName: String
        get() = "DIDAbnf.g4"

    override val atn: ATN
        get() = DIDAbnfLexer.Companion.ATN

    override val vocabulary: Vocabulary
        get() = DIDAbnfLexer.Companion.VOCABULARY

    @ThreadLocal
    companion object {
        val decisionToDFA: Array<DFA>
        val sharedContextCache = PredictionContextCache()

        private val LITERAL_NAMES: List<String?> = listOf(
            null, null, null, null,
            null, "'%'", "'-'",
            "'.'", "':'", "'_'"
        )
        private val SYMBOLIC_NAMES: List<String?> = listOf(
            null, "SCHEMA", "ALPHA",
            "DIGIT", "PCT_ENCODED",
            "PERCENT", "DASH",
            "PERIOD", "COLON",
            "UNDERSCORE"
        )

        val VOCABULARY = VocabularyImpl(LITERAL_NAMES.toTypedArray(), SYMBOLIC_NAMES.toTypedArray())

        val tokenNames: Array<String?> = Array<String?>(SYMBOLIC_NAMES.size) {
            var el = VOCABULARY.getLiteralName(it)
            if (el == null) {
                el = VOCABULARY.getSymbolicName(it)
            }

            if (el == null) {
                el = "<INVALID>"
            }
            el
        }

        private const val serializedATN: String =
            "\u0003\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\u0002\u000b\u0041\u0008\u0001\u0004\u0002\u0009\u0002\u0004\u0003\u0009\u0003\u0004\u0004\u0009\u0004\u0004\u0005\u0009\u0005\u0004\u0006\u0009\u0006\u0004\u0007\u0009\u0007\u0004\u0008\u0009\u0008\u0004\u0009\u0009\u0009\u0004\u000a\u0009\u000a\u0004\u000b\u0009\u000b\u0004\u000c\u0009\u000c\u0004\u000d\u0009\u000d\u0004\u000e\u0009\u000e\u0004\u000f\u0009\u000f\u0003\u0002\u0003\u0002\u0003\u0003\u0003\u0003\u0003\u0004\u0003\u0004\u0003\u0004\u0003\u0004\u0003\u0005\u0003\u0005\u0003\u0006\u0003\u0006\u0003\u0007\u0003\u0007\u0005\u0007\u002e\u000a\u0007\u0003\u0008\u0003\u0008\u0003\u0009\u0003\u0009\u0003\u000a\u0003\u000a\u0003\u000a\u0003\u000a\u0003\u000b\u0003\u000b\u0003\u000c\u0003\u000c\u0003\u000d\u0003\u000d\u0003\u000e\u0003\u000e\u0003\u000f\u0003\u000f\u0002\u0002\u0010\u0003\u0002\u0005\u0002\u0007\u0003\u0009\u0002\u000b\u0002\u000d\u0004\u000f\u0002\u0011\u0005\u0013\u0006\u0015\u0007\u0017\u0008\u0019\u0009\u001b\u000a\u001d\u000b\u0003\u0002\u0008\u0004\u0002\u0046\u0046\u0066\u0066\u0004\u0002\u004b\u004b\u006b\u006b\u0003\u0002\u0063\u007c\u0003\u0002\u0043\u005c\u0005\u0002\u0032\u003b\u0043\u0048\u0063\u0068\u0003\u0002\u0032\u003b\u0002\u003c\u0002\u0007\u0003\u0002\u0002\u0002\u0002\u000d\u0003\u0002\u0002\u0002\u0002\u0011\u0003\u0002\u0002\u0002\u0002\u0013\u0003\u0002\u0002\u0002\u0002\u0015\u0003\u0002\u0002\u0002\u0002\u0017\u0003\u0002\u0002\u0002\u0002\u0019\u0003\u0002\u0002\u0002\u0002\u001b\u0003\u0002\u0002\u0002\u0002\u001d\u0003\u0002\u0002\u0002\u0003\u001f\u0003\u0002\u0002\u0002\u0005\u0021\u0003\u0002\u0002\u0002\u0007\u0023\u0003\u0002\u0002\u0002\u0009\u0027\u0003\u0002\u0002\u0002\u000b\u0029\u0003\u0002\u0002\u0002\u000d\u002d\u0003\u0002\u0002\u0002\u000f\u002f\u0003\u0002\u0002\u0002\u0011\u0031\u0003\u0002\u0002\u0002\u0013\u0033\u0003\u0002\u0002\u0002\u0015\u0037\u0003\u0002\u0002\u0002\u0017\u0039\u0003\u0002\u0002\u0002\u0019\u003b\u0003\u0002\u0002\u0002\u001b\u003d\u0003\u0002\u0002\u0002\u001d\u003f\u0003\u0002\u0002\u0002\u001f\u0020\u0009\u0002\u0002\u0002\u0020\u0004\u0003\u0002\u0002\u0002\u0021\u0022\u0009\u0003\u0002\u0002\u0022\u0006\u0003\u0002\u0002\u0002\u0023\u0024\u0005\u0003\u0002\u0002\u0024\u0025\u0005\u0005\u0003\u0002\u0025\u0026\u0005\u0003\u0002\u0002\u0026\u0008\u0003\u0002\u0002\u0002\u0027\u0028\u0009\u0004\u0002\u0002\u0028\u000a\u0003\u0002\u0002\u0002\u0029\u002a\u0009\u0005\u0002\u0002\u002a\u000c\u0003\u0002\u0002\u0002\u002b\u002e\u0005\u0009\u0005\u0002\u002c\u002e\u0005\u000b\u0006\u0002\u002d\u002b\u0003\u0002\u0002\u0002\u002d\u002c\u0003\u0002\u0002\u0002\u002e\u000e\u0003\u0002\u0002\u0002\u002f\u0030\u0009\u0006\u0002\u0002\u0030\u0010\u0003\u0002\u0002\u0002\u0031\u0032\u0009\u0007\u0002\u0002\u0032\u0012\u0003\u0002\u0002\u0002\u0033\u0034\u0005\u0015\u000b\u0002\u0034\u0035\u0005\u000f\u0008\u0002\u0035\u0036\u0005\u000f\u0008\u0002\u0036\u0014\u0003\u0002\u0002\u0002\u0037\u0038\u0007\u0027\u0002\u0002\u0038\u0016\u0003\u0002\u0002\u0002\u0039\u003a\u0007\u002f\u0002\u0002\u003a\u0018\u0003\u0002\u0002\u0002\u003b\u003c\u0007\u0030\u0002\u0002\u003c\u001a\u0003\u0002\u0002\u0002\u003d\u003e\u0007\u003c\u0002\u0002\u003e\u001c\u0003\u0002\u0002\u0002\u003f\u0040\u0007\u0061\u0002\u0002\u0040\u001e\u0003\u0002\u0002\u0002\u0004\u0002\u002d\u0002"

        val ATN = ATNDeserializer().deserialize(serializedATN.asCharArray())

        init {
            decisionToDFA = Array<DFA>(ATN.numberOfDecisions, {
                DFA(ATN.getDecisionState(it)!!, it)
            })


        }
    }

    enum class Tokens(val id: Int) {
        SCHEMA(1),
        ALPHA(2),
        DIGIT(3),
        PCT_ENCODED(4),
        PERCENT(5),
        DASH(6),
        PERIOD(7),
        COLON(8),
        UNDERSCORE(9)
    }

    enum class Channels(val id: Int) {
        DEFAULT_TOKEN_CHANNEL(0),
        HIDDEN(1),
    }

    override val channelNames = Channels.values().map(Channels::name).toTypedArray()

    enum class Modes(val id: Int) {
        DEFAULT_MODE(0),
    }

    enum class Rules {
        D,
        I,
        SCHEMA,
        LOWERCASE,
        UPPERCASE,
        ALPHA,
        HEX,
        DIGIT,
        PCT_ENCODED,
        PERCENT,
        DASH,
        PERIOD,
        COLON,
        UNDERSCORE
    }


    init {
        this.interpreter = LexerATNSimulator(this, ATN, decisionToDFA as Array<DFA?>, sharedContextCache)
    }

}