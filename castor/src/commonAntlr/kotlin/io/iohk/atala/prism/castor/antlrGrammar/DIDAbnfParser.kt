// Generated from java-escape by ANTLR 4.7.1
package io.iohk.atala.prism.castor.antlrGrammar;

import com.strumenta.kotlinmultiplatform.TypeDeclarator
import com.strumenta.kotlinmultiplatform.asCharArray
import com.strumenta.kotlinmultiplatform.getType
import kotlin.reflect.KClass
import org.antlr.v4.kotlinruntime.Parser
import org.antlr.v4.kotlinruntime.ParserRuleContext
import org.antlr.v4.kotlinruntime.RecognitionException
import org.antlr.v4.kotlinruntime.Token
import org.antlr.v4.kotlinruntime.TokenStream
import org.antlr.v4.kotlinruntime.ThreadLocal
import org.antlr.v4.kotlinruntime.Vocabulary
import org.antlr.v4.kotlinruntime.VocabularyImpl
import org.antlr.v4.kotlinruntime.atn.ATN
import org.antlr.v4.kotlinruntime.atn.ATNDeserializer
import org.antlr.v4.kotlinruntime.atn.ParserATNSimulator
import org.antlr.v4.kotlinruntime.atn.PredictionContextCache
import org.antlr.v4.kotlinruntime.dfa.DFA
import org.antlr.v4.kotlinruntime.tree.ParseTreeListener
import org.antlr.v4.kotlinruntime.tree.TerminalNode

open class DIDAbnfParser(input: TokenStream) : Parser(input) {

    object solver : TypeDeclarator {
        override val classesByName: List<KClass<*>> = listOf(
            DIDAbnfParser.DidContext::class,
            DIDAbnfParser.Method_nameContext::class,
            DIDAbnfParser.Method_specific_idContext::class,
            DIDAbnfParser.IdcharContext::class
        )
    }


    override val grammarFileName: String
        get() = "DIDAbnf.g4"

    override val tokenNames: Array<String?>?
        get() = DIDAbnfParser.Companion.tokenNames
    override val ruleNames: Array<String>?
        get() = DIDAbnfParser.Companion.ruleNames
    override val atn: ATN
        get() = DIDAbnfParser.Companion.ATN
    override val vocabulary: Vocabulary
        get() = DIDAbnfParser.Companion.VOCABULARY

    enum class Tokens(val id: Int) {
        EOF(-1),
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

    enum class Rules(val id: Int) {
        RULE_DID(0),
        RULE_METHOD_NAME(1),
        RULE_METHOD_SPECIFIC_ID(2),
        RULE_ID_CHAR(3)
    }

    @ThreadLocal
    companion object {
        protected val decisionToDFA: Array<DFA>
        protected val sharedContextCache = PredictionContextCache()

        val ruleNames = arrayOf(
            "did", "method_name", "method_specific_id",
            "idchar"
        )

        private val LITERAL_NAMES: List<String?> = listOf(
            null, null, null,
            null, null, "'%'",
            "'-'", "'.'",
            "':'", "'_'"
        )
        private val SYMBOLIC_NAMES: List<String?> = listOf(
            null, "SCHEMA",
            "ALPHA", "DIGIT",
            "PCT_ENCODED",
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
            "\u0003\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\u0003\u000b\u0028\u0004\u0002\u0009\u0002\u0004\u0003\u0009\u0003\u0004\u0004\u0009\u0004\u0004\u0005\u0009\u0005\u0003\u0002\u0003\u0002\u0003\u0002\u0003\u0002\u0003\u0002\u0003\u0002\u0003\u0002\u0003\u0003\u0007\u0003\u0013\u000a\u0003\u000c\u0003\u000e\u0003\u0016\u000b\u0003\u0003\u0004\u0007\u0004\u0019\u000a\u0004\u000c\u0004\u000e\u0004\u001c\u000b\u0004\u0003\u0004\u0003\u0004\u0006\u0004\u0020\u000a\u0004\u000d\u0004\u000e\u0004\u0021\u0005\u0004\u0024\u000a\u0004\u0003\u0005\u0003\u0005\u0003\u0005\u0002\u0002\u0006\u0002\u0004\u0006\u0008\u0002\u0004\u0003\u0002\u0004\u0005\u0005\u0002\u0004\u0006\u0008\u0009\u000b\u000b\u0002\u0027\u0002\u000a\u0003\u0002\u0002\u0002\u0004\u0014\u0003\u0002\u0002\u0002\u0006\u001a\u0003\u0002\u0002\u0002\u0008\u0025\u0003\u0002\u0002\u0002\u000a\u000b\u0007\u0003\u0002\u0002\u000b\u000c\u0007\u000a\u0002\u0002\u000c\u000d\u0005\u0004\u0003\u0002\u000d\u000e\u0007\u000a\u0002\u0002\u000e\u000f\u0005\u0006\u0004\u0002\u000f\u0010\u0007\u0002\u0002\u0003\u0010\u0003\u0003\u0002\u0002\u0002\u0011\u0013\u0009\u0002\u0002\u0002\u0012\u0011\u0003\u0002\u0002\u0002\u0013\u0016\u0003\u0002\u0002\u0002\u0014\u0012\u0003\u0002\u0002\u0002\u0014\u0015\u0003\u0002\u0002\u0002\u0015\u0005\u0003\u0002\u0002\u0002\u0016\u0014\u0003\u0002\u0002\u0002\u0017\u0019\u0005\u0008\u0005\u0002\u0018\u0017\u0003\u0002\u0002\u0002\u0019\u001c\u0003\u0002\u0002\u0002\u001a\u0018\u0003\u0002\u0002\u0002\u001a\u001b\u0003\u0002\u0002\u0002\u001b\u0023\u0003\u0002\u0002\u0002\u001c\u001a\u0003\u0002\u0002\u0002\u001d\u001f\u0007\u000a\u0002\u0002\u001e\u0020\u0005\u0008\u0005\u0002\u001f\u001e\u0003\u0002\u0002\u0002\u0020\u0021\u0003\u0002\u0002\u0002\u0021\u001f\u0003\u0002\u0002\u0002\u0021\u0022\u0003\u0002\u0002\u0002\u0022\u0024\u0003\u0002\u0002\u0002\u0023\u001d\u0003\u0002\u0002\u0002\u0023\u0024\u0003\u0002\u0002\u0002\u0024\u0007\u0003\u0002\u0002\u0002\u0025\u0026\u0009\u0003\u0002\u0002\u0026\u0009\u0003\u0002\u0002\u0002\u0006\u0014\u001a\u0021\u0023"

        val ATN = ATNDeserializer().deserialize(serializedATN.asCharArray())

        init {
            decisionToDFA = Array<DFA>(ATN.numberOfDecisions, {
                DFA(ATN.getDecisionState(it)!!, it)
            })


        }
    }

    private val SCHEMA = Tokens.SCHEMA.id
    private val ALPHA = Tokens.ALPHA.id
    private val DIGIT = Tokens.DIGIT.id
    private val PCT_ENCODED = Tokens.PCT_ENCODED.id
    private val PERCENT = Tokens.PERCENT.id
    private val DASH = Tokens.DASH.id
    private val PERIOD = Tokens.PERIOD.id
    private val COLON = Tokens.COLON.id
    private val UNDERSCORE = Tokens.UNDERSCORE.id

    /* Named actions */
    init {
        interpreter = ParserATNSimulator(this, ATN, decisionToDFA, sharedContextCache)
    }

    /* Funcs */
    open class DidContext : ParserRuleContext {
        override var ruleIndex: Int
            get() = Rules.RULE_DID.id
            set(value) {
                throw RuntimeException()
            }

        fun SCHEMA(): TerminalNode? = getToken(DIDAbnfParser.Tokens.SCHEMA.id, 0)
        fun findMethod_name(): Method_nameContext? = getRuleContext(solver.getType("Method_nameContext"), 0)
        fun findMethod_specific_id(): Method_specific_idContext? =
            getRuleContext(solver.getType("Method_specific_idContext"), 0)

        fun EOF(): TerminalNode? = getToken(DIDAbnfParser.Tokens.EOF.id, 0)

        constructor(parent: ParserRuleContext?, invokingState: Int) : super(parent, invokingState) {
        }

        override fun enterRule(listener: ParseTreeListener) {
            if (listener is DIDAbnfListener) (listener as DIDAbnfListener).enterDid(this)
        }

        override fun exitRule(listener: ParseTreeListener) {
            if (listener is DIDAbnfListener) (listener as DIDAbnfListener).exitDid(this)
        }
    }

    fun did(): DidContext {
        var _localctx: DidContext = DidContext(context, state)
        enterRule(_localctx, 0, Rules.RULE_DID.id)
        try {
            enterOuterAlt(_localctx, 1)
            if (true) {
                this.state = 8
                match(SCHEMA) as Token
                this.state = 9
                match(COLON) as Token
                this.state = 10
                method_name()
                this.state = 11
                match(COLON) as Token
                this.state = 12
                method_specific_id()
                this.state = 13
                match(EOF) as Token
            }
        } catch (re: RecognitionException) {
            throw InvalidDIDStringError("Invalid Did char found at [line ${re.offendingToken?.line}, col ${re.offendingToken?.charPositionInLine}] \"${re.offendingToken?.text}\"")
        } finally {
            exitRule()
        }
        return _localctx
    }

    open class Method_nameContext : ParserRuleContext {
        override var ruleIndex: Int
            get() = Rules.RULE_METHOD_NAME.id
            set(value) {
                throw RuntimeException()
            }

        fun ALPHA(): List<TerminalNode> = getTokens(DIDAbnfParser.Tokens.ALPHA.id)
        fun ALPHA(i: Int): TerminalNode = getToken(DIDAbnfParser.Tokens.ALPHA.id, i) as TerminalNode
        fun DIGIT(): List<TerminalNode> = getTokens(DIDAbnfParser.Tokens.DIGIT.id)
        fun DIGIT(i: Int): TerminalNode = getToken(DIDAbnfParser.Tokens.DIGIT.id, i) as TerminalNode

        constructor(parent: ParserRuleContext?, invokingState: Int) : super(parent, invokingState) {
        }

        override fun enterRule(listener: ParseTreeListener) {
            if (listener is DIDAbnfListener) (listener as DIDAbnfListener).enterMethod_name(this)
        }

        override fun exitRule(listener: ParseTreeListener) {
            if (listener is DIDAbnfListener) (listener as DIDAbnfListener).exitMethod_name(this)
        }
    }

    fun method_name(): Method_nameContext {
        var _localctx: Method_nameContext = Method_nameContext(context, state)
        enterRule(_localctx, 2, Rules.RULE_METHOD_NAME.id)
        var _la: Int
        try {
            enterOuterAlt(_localctx, 1)
            if (true) {
                this.state = 18
                errorHandler.sync(this);
                _la = _input!!.LA(1)
                while (_la == ALPHA || _la == DIGIT) {
                    if (true) {
                        if (true) {
                            this.state = 15
                            _la = _input!!.LA(1)
                            if (!(_la == ALPHA || _la == DIGIT)) {
                                errorHandler.recoverInline(this)
                            } else {
                                if (_input!!.LA(1) == Tokens.EOF.id) isMatchedEOF = true
                                errorHandler.reportMatch(this)
                                consume()
                            }
                        }
                    }
                    this.state = 20
                    errorHandler.sync(this)
                    _la = _input!!.LA(1)
                }
            }
        } catch (re: RecognitionException) {
            throw InvalidDIDStringError("Invalid Did char found at [line ${re.offendingToken?.line}, col ${re.offendingToken?.charPositionInLine}] \"${re.offendingToken?.text}\"")
        } finally {
            exitRule()
        }
        return _localctx
    }

    open class Method_specific_idContext : ParserRuleContext {
        override var ruleIndex: Int
            get() = Rules.RULE_METHOD_SPECIFIC_ID.id
            set(value) {
                throw RuntimeException()
            }

        fun findIdchar(): List<IdcharContext> = getRuleContexts(solver.getType("IdcharContext"))
        fun findIdchar(i: Int): IdcharContext? = getRuleContext(solver.getType("IdcharContext"), i)

        constructor(parent: ParserRuleContext?, invokingState: Int) : super(parent, invokingState) {
        }

        override fun enterRule(listener: ParseTreeListener) {
            if (listener is DIDAbnfListener) (listener as DIDAbnfListener).enterMethod_specific_id(this)
        }

        override fun exitRule(listener: ParseTreeListener) {
            if (listener is DIDAbnfListener) (listener as DIDAbnfListener).exitMethod_specific_id(this)
        }
    }

    fun method_specific_id(): Method_specific_idContext {
        var _localctx: Method_specific_idContext = Method_specific_idContext(context, state)
        enterRule(_localctx, 4, Rules.RULE_METHOD_SPECIFIC_ID.id)
        var _la: Int
        try {
            enterOuterAlt(_localctx, 1)
            if (true) {
                this.state = 24
                errorHandler.sync(this);
                _la = _input!!.LA(1)
                while ((((_la) and 0x3f.inv()) == 0 && ((1L shl _la) and ((1L shl ALPHA) or (1L shl DIGIT) or (1L shl PCT_ENCODED) or (1L shl DASH) or (1L shl PERIOD) or (1L shl UNDERSCORE))) != 0L)) {
                    if (true) {
                        if (true) {
                            this.state = 21
                            idchar()
                        }
                    }
                    this.state = 26
                    errorHandler.sync(this)
                    _la = _input!!.LA(1)
                }
                this.state = 33
                errorHandler.sync(this)
                _la = _input!!.LA(1)
                if (_la == COLON) {
                    if (true) {
                        this.state = 27
                        match(COLON) as Token
                        this.state = 29
                        errorHandler.sync(this)
                        _la = _input!!.LA(1)
                        do {
                            if (true) {
                                if (true) {
                                    this.state = 28
                                    idchar()
                                }
                            }
                            this.state = 31
                            errorHandler.sync(this)
                            _la = _input!!.LA(1)
                        } while ((((_la) and 0x3f.inv()) == 0 && ((1L shl _la) and ((1L shl ALPHA) or (1L shl DIGIT) or (1L shl PCT_ENCODED) or (1L shl DASH) or (1L shl PERIOD) or (1L shl UNDERSCORE))) != 0L))
                    }
                }

            }
        } catch (re: RecognitionException) {
            throw InvalidDIDStringError("Invalid Did char found at [line ${re.offendingToken?.line}, col ${re.offendingToken?.charPositionInLine}] \"${re.offendingToken?.text}\"")
        } finally {
            exitRule()
        }
        return _localctx
    }

    open class IdcharContext : ParserRuleContext {
        override var ruleIndex: Int
            get() = Rules.RULE_ID_CHAR.id
            set(value) {
                throw RuntimeException()
            }

        fun ALPHA(): TerminalNode? = getToken(DIDAbnfParser.Tokens.ALPHA.id, 0)
        fun DIGIT(): TerminalNode? = getToken(DIDAbnfParser.Tokens.DIGIT.id, 0)
        fun PERIOD(): TerminalNode? = getToken(DIDAbnfParser.Tokens.PERIOD.id, 0)
        fun DASH(): TerminalNode? = getToken(DIDAbnfParser.Tokens.DASH.id, 0)
        fun UNDERSCORE(): TerminalNode? = getToken(DIDAbnfParser.Tokens.UNDERSCORE.id, 0)
        fun PCT_ENCODED(): TerminalNode? = getToken(DIDAbnfParser.Tokens.PCT_ENCODED.id, 0)

        constructor(parent: ParserRuleContext?, invokingState: Int) : super(parent, invokingState) {
        }

        override fun enterRule(listener: ParseTreeListener) {
            if (listener is DIDAbnfListener) (listener as DIDAbnfListener).enterIdchar(this)
        }

        override fun exitRule(listener: ParseTreeListener) {
            if (listener is DIDAbnfListener) (listener as DIDAbnfListener).exitIdchar(this)
        }
    }

    fun idchar(): IdcharContext {
        var _localctx: IdcharContext = IdcharContext(context, state)
        enterRule(_localctx, 6, Rules.RULE_ID_CHAR.id)
        var _la: Int
        try {
            enterOuterAlt(_localctx, 1)
            if (true) {
                this.state = 35
                _la = _input!!.LA(1)
                if (!((((_la) and 0x3f.inv()) == 0 && ((1L shl _la) and ((1L shl ALPHA) or (1L shl DIGIT) or (1L shl PCT_ENCODED) or (1L shl DASH) or (1L shl PERIOD) or (1L shl UNDERSCORE))) != 0L))) {
                    errorHandler.recoverInline(this)
                } else {
                    if (_input!!.LA(1) == Tokens.EOF.id) isMatchedEOF = true
                    errorHandler.reportMatch(this)
                    consume()
                }
            }
        } catch (re: RecognitionException) {
            throw InvalidDIDStringError("Invalid Did char found at [line ${re.offendingToken?.line}, col ${re.offendingToken?.charPositionInLine}] \"${re.offendingToken?.text}\"")
        } finally {
            exitRule()
        }
        return _localctx
    }

}