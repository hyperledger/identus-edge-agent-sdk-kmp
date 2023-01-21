// Generated from java-escape by ANTLR 4.7.1
package io.iohk.atala.prism.castor.DIDGrammar;

import com.strumenta.kotlinmultiplatform.TypeDeclarator
import com.strumenta.kotlinmultiplatform.asCharArray
import com.strumenta.kotlinmultiplatform.getType
import org.antlr.v4.kotlinruntime.*
import org.antlr.v4.kotlinruntime.atn.ATN
import org.antlr.v4.kotlinruntime.atn.ATN.Companion.INVALID_ALT_NUMBER
import org.antlr.v4.kotlinruntime.atn.ATNDeserializer
import org.antlr.v4.kotlinruntime.atn.ParserATNSimulator
import org.antlr.v4.kotlinruntime.atn.PredictionContextCache
import org.antlr.v4.kotlinruntime.dfa.DFA
import org.antlr.v4.kotlinruntime.tree.ParseTreeListener
import org.antlr.v4.kotlinruntime.tree.TerminalNode
import kotlin.reflect.KClass

open class DIDUrlAbnfParser(input: TokenStream) : Parser(input) {

    object solver : TypeDeclarator {
        override val classesByName: List<KClass<*>> = listOf(
            DIDUrlAbnfParser.Did_urlContext::class,
            DIDUrlAbnfParser.DidContext::class,
            DIDUrlAbnfParser.Method_nameContext::class,
            DIDUrlAbnfParser.Method_specific_idContext::class,
            DIDUrlAbnfParser.PathContext::class,
            DIDUrlAbnfParser.QueryContext::class,
            DIDUrlAbnfParser.FragContext::class,
            DIDUrlAbnfParser.SearchContext::class,
            DIDUrlAbnfParser.SearchparameterContext::class,
            DIDUrlAbnfParser.StringContext::class
        )
    }


    override val grammarFileName: String
        get() = "DIDUrlAbnf.g4"

    override val tokenNames: Array<String?>?
        get() = DIDUrlAbnfParser.Companion.tokenNames
    override val ruleNames: Array<String>?
        get() = DIDUrlAbnfParser.Companion.ruleNames
    override val atn: ATN
        get() = DIDUrlAbnfParser.Companion.ATN
    override val vocabulary: Vocabulary
        get() = DIDUrlAbnfParser.Companion.VOCABULARY

    enum class Tokens(val id: Int) {
        EOF(-1),
        T__0(1),
        T__1(2),
        T__2(3),
        T__3(4),
        T__4(5),
        SCHEMA(6),
        ALPHA(7),
        DIGIT(8),
        PCT_ENCODED(9),
        PERCENT(10),
        DASH(11),
        PERIOD(12),
        COLON(13),
        UNDERSCORE(14),
        HEX(15),
        STRING(16)
    }

    enum class Rules(val id: Int) {
        RULE_did_url(0),
        RULE_did(1),
        RULE_method_name(2),
        RULE_method_specific_id(3),
        RULE_path(4),
        RULE_query(5),
        RULE_frag(6),
        RULE_search(7),
        RULE_searchparameter(8),
        RULE_string(9)
    }

    @ThreadLocal
    companion object {
        protected val decisionToDFA: Array<DFA>
        protected val sharedContextCache = PredictionContextCache()

        val ruleNames = arrayOf(
            "did_url", "did", "method_name", "method_specific_id",
            "path", "query", "frag", "search", "searchparameter",
            "string"
        )

        private val LITERAL_NAMES: List<String?> = listOf(
            null, "'/'", "'?'",
            "'#'", "'&'",
            "'='", null, null,
            null, null, "'%'",
            "'-'", "'.'",
            "':'", "'_'"
        )
        private val SYMBOLIC_NAMES: List<String?> = listOf(
            null, null, null,
            null, null, null,
            "SCHEMA", "ALPHA",
            "DIGIT", "PCT_ENCODED",
            "PERCENT", "DASH",
            "PERIOD", "COLON",
            "UNDERSCORE",
            "HEX", "STRING"
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
            "\u0003\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\u0003\u0012\u005b\u0004\u0002\u0009\u0002\u0004\u0003\u0009\u0003\u0004\u0004\u0009\u0004\u0004\u0005\u0009\u0005\u0004\u0006\u0009\u0006\u0004\u0007\u0009\u0007\u0004\u0008\u0009\u0008\u0004\u0009\u0009\u0009\u0004\u000a\u0009\u000a\u0004\u000b\u0009\u000b\u0003\u0002\u0003\u0002\u0005\u0002\u0019\u000a\u0002\u0003\u0002\u0005\u0002\u001c\u000a\u0002\u0003\u0002\u0005\u0002\u001f\u000a\u0002\u0003\u0002\u0003\u0002\u0003\u0003\u0003\u0003\u0003\u0003\u0003\u0003\u0003\u0003\u0003\u0003\u0003\u0004\u0003\u0004\u0003\u0005\u0003\u0005\u0005\u0005\u002d\u000a\u0005\u0007\u0005\u002f\u000a\u0005\u000c\u0005\u000e\u0005\u0032\u000b\u0005\u0003\u0005\u0003\u0005\u0003\u0006\u0003\u0006\u0007\u0006\u0038\u000a\u0006\u000c\u0006\u000e\u0006\u003b\u000b\u0006\u0003\u0006\u0005\u0006\u003e\u000a\u0006\u0003\u0007\u0003\u0007\u0003\u0007\u0003\u0008\u0003\u0008\u0003\u0008\u0005\u0008\u0046\u000a\u0008\u0003\u0009\u0003\u0009\u0003\u0009\u0007\u0009\u004b\u000a\u0009\u000c\u0009\u000e\u0009\u004e\u000b\u0009\u0003\u000a\u0003\u000a\u0003\u000a\u0003\u000a\u0003\u000a\u0005\u000a\u0055\u000a\u000a\u0005\u000a\u0057\u000a\u000a\u0003\u000b\u0003\u000b\u0003\u000b\u0002\u0002\u000c\u0002\u0004\u0006\u0008\u000a\u000c\u000e\u0010\u0012\u0014\u0002\u0003\u0004\u0002\u000a\u000a\u0012\u0012\u0002\u005c\u0002\u0016\u0003\u0002\u0002\u0002\u0004\u0022\u0003\u0002\u0002\u0002\u0006\u0028\u0003\u0002\u0002\u0002\u0008\u0030\u0003\u0002\u0002\u0002\u000a\u0039\u0003\u0002\u0002\u0002\u000c\u003f\u0003\u0002\u0002\u0002\u000e\u0042\u0003\u0002\u0002\u0002\u0010\u0047\u0003\u0002\u0002\u0002\u0012\u004f\u0003\u0002\u0002\u0002\u0014\u0058\u0003\u0002\u0002\u0002\u0016\u0018\u0005\u0004\u0003\u0002\u0017\u0019\u0005\u000a\u0006\u0002\u0018\u0017\u0003\u0002\u0002\u0002\u0018\u0019\u0003\u0002\u0002\u0002\u0019\u001b\u0003\u0002\u0002\u0002\u001a\u001c\u0005\u000c\u0007\u0002\u001b\u001a\u0003\u0002\u0002\u0002\u001b\u001c\u0003\u0002\u0002\u0002\u001c\u001e\u0003\u0002\u0002\u0002\u001d\u001f\u0005\u000e\u0008\u0002\u001e\u001d\u0003\u0002\u0002\u0002\u001e\u001f\u0003\u0002\u0002\u0002\u001f\u0020\u0003\u0002\u0002\u0002\u0020\u0021\u0007\u0002\u0002\u0003\u0021\u0003\u0003\u0002\u0002\u0002\u0022\u0023\u0007\u0008\u0002\u0002\u0023\u0024\u0007\u000f\u0002\u0002\u0024\u0025\u0005\u0006\u0004\u0002\u0025\u0026\u0007\u000f\u0002\u0002\u0026\u0027\u0005\u0008\u0005\u0002\u0027\u0005\u0003\u0002\u0002\u0002\u0028\u0029\u0005\u0014\u000b\u0002\u0029\u0007\u0003\u0002\u0002\u0002\u002a\u002c\u0005\u0014\u000b\u0002\u002b\u002d\u0007\u000f\u0002\u0002\u002c\u002b\u0003\u0002\u0002\u0002\u002c\u002d\u0003\u0002\u0002\u0002\u002d\u002f\u0003\u0002\u0002\u0002\u002e\u002a\u0003\u0002\u0002\u0002\u002f\u0032\u0003\u0002\u0002\u0002\u0030\u002e\u0003\u0002\u0002\u0002\u0030\u0031\u0003\u0002\u0002\u0002\u0031\u0033\u0003\u0002\u0002\u0002\u0032\u0030\u0003\u0002\u0002\u0002\u0033\u0034\u0005\u0014\u000b\u0002\u0034\u0009\u0003\u0002\u0002\u0002\u0035\u0036\u0007\u0003\u0002\u0002\u0036\u0038\u0005\u0014\u000b\u0002\u0037\u0035\u0003\u0002\u0002\u0002\u0038\u003b\u0003\u0002\u0002\u0002\u0039\u0037\u0003\u0002\u0002\u0002\u0039\u003a\u0003\u0002\u0002\u0002\u003a\u003d\u0003\u0002\u0002\u0002\u003b\u0039\u0003\u0002\u0002\u0002\u003c\u003e\u0007\u0003\u0002\u0002\u003d\u003c\u0003\u0002\u0002\u0002\u003d\u003e\u0003\u0002\u0002\u0002\u003e\u000b\u0003\u0002\u0002\u0002\u003f\u0040\u0007\u0004\u0002\u0002\u0040\u0041\u0005\u0010\u0009\u0002\u0041\u000d\u0003\u0002\u0002\u0002\u0042\u0045\u0007\u0005\u0002\u0002\u0043\u0046\u0005\u0014\u000b\u0002\u0044\u0046\u0007\u000a\u0002\u0002\u0045\u0043\u0003\u0002\u0002\u0002\u0045\u0044\u0003\u0002\u0002\u0002\u0046\u000f\u0003\u0002\u0002\u0002\u0047\u004c\u0005\u0012\u000a\u0002\u0048\u0049\u0007\u0006\u0002\u0002\u0049\u004b\u0005\u0012\u000a\u0002\u004a\u0048\u0003\u0002\u0002\u0002\u004b\u004e\u0003\u0002\u0002\u0002\u004c\u004a\u0003\u0002\u0002\u0002\u004c\u004d\u0003\u0002\u0002\u0002\u004d\u0011\u0003\u0002\u0002\u0002\u004e\u004c\u0003\u0002\u0002\u0002\u004f\u0056\u0005\u0014\u000b\u0002\u0050\u0054\u0007\u0007\u0002\u0002\u0051\u0055\u0005\u0014\u000b\u0002\u0052\u0055\u0007\u000a\u0002\u0002\u0053\u0055\u0007\u0011\u0002\u0002\u0054\u0051\u0003\u0002\u0002\u0002\u0054\u0052\u0003\u0002\u0002\u0002\u0054\u0053\u0003\u0002\u0002\u0002\u0055\u0057\u0003\u0002\u0002\u0002\u0056\u0050\u0003\u0002\u0002\u0002\u0056\u0057\u0003\u0002\u0002\u0002\u0057\u0013\u0003\u0002\u0002\u0002\u0058\u0059\u0009\u0002\u0002\u0002\u0059\u0015\u0003\u0002\u0002\u0002\u000d\u0018\u001b\u001e\u002c\u0030\u0039\u003d\u0045\u004c\u0054\u0056"

        val ATN = ATNDeserializer().deserialize(serializedATN.asCharArray())

        init {
            decisionToDFA = Array<DFA>(ATN.numberOfDecisions, {
                DFA(ATN.getDecisionState(it)!!, it)
            })


        }
    }

    private val T__0 = Tokens.T__0.id
    private val T__1 = Tokens.T__1.id
    private val T__2 = Tokens.T__2.id
    private val T__3 = Tokens.T__3.id
    private val T__4 = Tokens.T__4.id
    private val SCHEMA = Tokens.SCHEMA.id
    private val ALPHA = Tokens.ALPHA.id
    private val DIGIT = Tokens.DIGIT.id
    private val PCT_ENCODED = Tokens.PCT_ENCODED.id
    private val PERCENT = Tokens.PERCENT.id
    private val DASH = Tokens.DASH.id
    private val PERIOD = Tokens.PERIOD.id
    private val COLON = Tokens.COLON.id
    private val UNDERSCORE = Tokens.UNDERSCORE.id
    private val HEX = Tokens.HEX.id
    private val STRING = Tokens.STRING.id

    /* Named actions */
    init {
        interpreter = ParserATNSimulator(this, ATN, decisionToDFA, sharedContextCache)
    }

    /* Funcs */
    open class Did_urlContext : ParserRuleContext {
        override var ruleIndex: Int
            get() = Rules.RULE_did_url.id
            set(value) {
                throw RuntimeException()
            }

        fun findDid(): DidContext? = getRuleContext(solver.getType("DidContext"), 0)
        fun EOF(): TerminalNode? = getToken(DIDUrlAbnfParser.Tokens.EOF.id, 0)
        fun findPath(): PathContext? = getRuleContext(solver.getType("PathContext"), 0)
        fun findQuery(): QueryContext? = getRuleContext(solver.getType("QueryContext"), 0)
        fun findFrag(): FragContext? = getRuleContext(solver.getType("FragContext"), 0)

        constructor(parent: ParserRuleContext?, invokingState: Int) : super(parent, invokingState) {
        }

        override fun enterRule(listener: ParseTreeListener) {
            if (listener is DIDUrlAbnfListener) (listener as DIDUrlAbnfListener).enterDid_url(this)
        }

        override fun exitRule(listener: ParseTreeListener) {
            if (listener is DIDUrlAbnfListener) (listener as DIDUrlAbnfListener).exitDid_url(this)
        }
    }

    fun did_url(): Did_urlContext {
        var _localctx: Did_urlContext = Did_urlContext(context, state)
        enterRule(_localctx, 0, Rules.RULE_did_url.id)
        var _la: Int
        try {
            enterOuterAlt(_localctx, 1)
            if (true) {
                this.state = 20
                did()
                this.state = 22
                errorHandler.sync(this)
                when (interpreter!!.adaptivePredict(_input!!, 0, context)) {
                    1 -> if (true) {
                        this.state = 21
                        path()
                    }
                }
                this.state = 25
                errorHandler.sync(this)
                _la = _input!!.LA(1)
                if (_la == T__1) {
                    if (true) {
                        this.state = 24
                        query()
                    }
                }

                this.state = 28
                errorHandler.sync(this)
                _la = _input!!.LA(1)
                if (_la == T__2) {
                    if (true) {
                        this.state = 27
                        frag()
                    }
                }

                this.state = 30
                match(EOF) as Token
            }
        } catch (re: RecognitionException) {
            _localctx.exception = re
            errorHandler.reportError(this, re)
            errorHandler.recover(this, re)
        } finally {
            exitRule()
        }
        return _localctx
    }

    open class DidContext : ParserRuleContext {
        override var ruleIndex: Int
            get() = Rules.RULE_did.id
            set(value) {
                throw RuntimeException()
            }

        fun SCHEMA(): TerminalNode? = getToken(DIDUrlAbnfParser.Tokens.SCHEMA.id, 0)
        fun findMethod_name(): Method_nameContext? = getRuleContext(solver.getType("Method_nameContext"), 0)
        fun findMethod_specific_id(): Method_specific_idContext? =
            getRuleContext(solver.getType("Method_specific_idContext"), 0)

        constructor(parent: ParserRuleContext?, invokingState: Int) : super(parent, invokingState) {
        }

        override fun enterRule(listener: ParseTreeListener) {
            if (listener is DIDUrlAbnfListener) (listener as DIDUrlAbnfListener).enterDid(this)
        }

        override fun exitRule(listener: ParseTreeListener) {
            if (listener is DIDUrlAbnfListener) (listener as DIDUrlAbnfListener).exitDid(this)
        }
    }

    fun did(): DidContext {
        var _localctx: DidContext = DidContext(context, state)
        enterRule(_localctx, 2, Rules.RULE_did.id)
        try {
            enterOuterAlt(_localctx, 1)
            if (true) {
                this.state = 32
                match(SCHEMA) as Token
                this.state = 33
                match(COLON) as Token
                this.state = 34
                method_name()
                this.state = 35
                match(COLON) as Token
                this.state = 36
                method_specific_id()
            }
        } catch (re: RecognitionException) {
            _localctx.exception = re
            errorHandler.reportError(this, re)
            errorHandler.recover(this, re)
        } finally {
            exitRule()
        }
        return _localctx
    }

    open class Method_nameContext : ParserRuleContext {
        override var ruleIndex: Int
            get() = Rules.RULE_method_name.id
            set(value) {
                throw RuntimeException()
            }

        fun findString(): StringContext? = getRuleContext(solver.getType("StringContext"), 0)

        constructor(parent: ParserRuleContext?, invokingState: Int) : super(parent, invokingState) {
        }

        override fun enterRule(listener: ParseTreeListener) {
            if (listener is DIDUrlAbnfListener) (listener as DIDUrlAbnfListener).enterMethod_name(this)
        }

        override fun exitRule(listener: ParseTreeListener) {
            if (listener is DIDUrlAbnfListener) (listener as DIDUrlAbnfListener).exitMethod_name(this)
        }
    }

    fun method_name(): Method_nameContext {
        var _localctx: Method_nameContext = Method_nameContext(context, state)
        enterRule(_localctx, 4, Rules.RULE_method_name.id)
        try {
            enterOuterAlt(_localctx, 1)
            if (true) {
                this.state = 38
                string()
            }
        } catch (re: RecognitionException) {
            _localctx.exception = re
            errorHandler.reportError(this, re)
            errorHandler.recover(this, re)
        } finally {
            exitRule()
        }
        return _localctx
    }

    open class Method_specific_idContext : ParserRuleContext {
        override var ruleIndex: Int
            get() = Rules.RULE_method_specific_id.id
            set(value) {
                throw RuntimeException()
            }

        fun findString(): List<StringContext> = getRuleContexts(solver.getType("StringContext"))
        fun findString(i: Int): StringContext? = getRuleContext(solver.getType("StringContext"), i)

        constructor(parent: ParserRuleContext?, invokingState: Int) : super(parent, invokingState) {
        }

        override fun enterRule(listener: ParseTreeListener) {
            if (listener is DIDUrlAbnfListener) (listener as DIDUrlAbnfListener).enterMethod_specific_id(this)
        }

        override fun exitRule(listener: ParseTreeListener) {
            if (listener is DIDUrlAbnfListener) (listener as DIDUrlAbnfListener).exitMethod_specific_id(this)
        }
    }

    fun method_specific_id(): Method_specific_idContext {
        var _localctx: Method_specific_idContext = Method_specific_idContext(context, state)
        enterRule(_localctx, 6, Rules.RULE_method_specific_id.id)
        var _la: Int
        try {
            var _alt: Int
            enterOuterAlt(_localctx, 1)
            if (true) {
                this.state = 46
                errorHandler.sync(this)
                _alt = interpreter!!.adaptivePredict(_input!!, 4, context)
                while (_alt != 2 && _alt != INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        if (true) {
                            if (true) {
                                this.state = 40
                                string()
                                this.state = 42
                                errorHandler.sync(this)
                                _la = _input!!.LA(1)
                                if (_la == COLON) {
                                    if (true) {
                                        this.state = 41
                                        match(COLON) as Token
                                    }
                                }

                            }
                        }
                    }
                    this.state = 48
                    errorHandler.sync(this)
                    _alt = interpreter!!.adaptivePredict(_input!!, 4, context)
                }
                this.state = 49
                string()
            }
        } catch (re: RecognitionException) {
            _localctx.exception = re
            errorHandler.reportError(this, re)
            errorHandler.recover(this, re)
        } finally {
            exitRule()
        }
        return _localctx
    }

    open class PathContext : ParserRuleContext {
        override var ruleIndex: Int
            get() = Rules.RULE_path.id
            set(value) {
                throw RuntimeException()
            }

        fun findString(): List<StringContext> = getRuleContexts(solver.getType("StringContext"))
        fun findString(i: Int): StringContext? = getRuleContext(solver.getType("StringContext"), i)

        constructor(parent: ParserRuleContext?, invokingState: Int) : super(parent, invokingState) {
        }

        override fun enterRule(listener: ParseTreeListener) {
            if (listener is DIDUrlAbnfListener) (listener as DIDUrlAbnfListener).enterPath(this)
        }

        override fun exitRule(listener: ParseTreeListener) {
            if (listener is DIDUrlAbnfListener) (listener as DIDUrlAbnfListener).exitPath(this)
        }
    }

    fun path(): PathContext {
        var _localctx: PathContext = PathContext(context, state)
        enterRule(_localctx, 8, Rules.RULE_path.id)
        var _la: Int
        try {
            var _alt: Int
            enterOuterAlt(_localctx, 1)
            if (true) {
                this.state = 55
                errorHandler.sync(this)
                _alt = interpreter!!.adaptivePredict(_input!!, 5, context)
                while (_alt != 2 && _alt != INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        if (true) {
                            if (true) {
                                this.state = 51
                                match(T__0) as Token
                                this.state = 52
                                string()
                            }
                        }
                    }
                    this.state = 57
                    errorHandler.sync(this)
                    _alt = interpreter!!.adaptivePredict(_input!!, 5, context)
                }
                this.state = 59
                errorHandler.sync(this)
                _la = _input!!.LA(1)
                if (_la == T__0) {
                    if (true) {
                        this.state = 58
                        match(T__0) as Token
                    }
                }

            }
        } catch (re: RecognitionException) {
            _localctx.exception = re
            errorHandler.reportError(this, re)
            errorHandler.recover(this, re)
        } finally {
            exitRule()
        }
        return _localctx
    }

    open class QueryContext : ParserRuleContext {
        override var ruleIndex: Int
            get() = Rules.RULE_query.id
            set(value) {
                throw RuntimeException()
            }

        fun findSearch(): SearchContext? = getRuleContext(solver.getType("SearchContext"), 0)

        constructor(parent: ParserRuleContext?, invokingState: Int) : super(parent, invokingState) {
        }

        override fun enterRule(listener: ParseTreeListener) {
            if (listener is DIDUrlAbnfListener) (listener as DIDUrlAbnfListener).enterQuery(this)
        }

        override fun exitRule(listener: ParseTreeListener) {
            if (listener is DIDUrlAbnfListener) (listener as DIDUrlAbnfListener).exitQuery(this)
        }
    }

    fun query(): QueryContext {
        var _localctx: QueryContext = QueryContext(context, state)
        enterRule(_localctx, 10, Rules.RULE_query.id)
        try {
            enterOuterAlt(_localctx, 1)
            if (true) {
                this.state = 61
                match(T__1) as Token
                this.state = 62
                search()
            }
        } catch (re: RecognitionException) {
            _localctx.exception = re
            errorHandler.reportError(this, re)
            errorHandler.recover(this, re)
        } finally {
            exitRule()
        }
        return _localctx
    }

    open class FragContext : ParserRuleContext {
        override var ruleIndex: Int
            get() = Rules.RULE_frag.id
            set(value) {
                throw RuntimeException()
            }

        fun findString(): StringContext? = getRuleContext(solver.getType("StringContext"), 0)
        fun DIGIT(): TerminalNode? = getToken(DIDUrlAbnfParser.Tokens.DIGIT.id, 0)

        constructor(parent: ParserRuleContext?, invokingState: Int) : super(parent, invokingState) {
        }

        override fun enterRule(listener: ParseTreeListener) {
            if (listener is DIDUrlAbnfListener) (listener as DIDUrlAbnfListener).enterFrag(this)
        }

        override fun exitRule(listener: ParseTreeListener) {
            if (listener is DIDUrlAbnfListener) (listener as DIDUrlAbnfListener).exitFrag(this)
        }
    }

    fun frag(): FragContext {
        var _localctx: FragContext = FragContext(context, state)
        enterRule(_localctx, 12, Rules.RULE_frag.id)
        try {
            enterOuterAlt(_localctx, 1)
            if (true) {
                this.state = 64
                match(T__2) as Token
                this.state = 67
                errorHandler.sync(this)
                when (interpreter!!.adaptivePredict(_input!!, 7, context)) {
                    1 -> {
                        if (true) {
                            this.state = 65
                            string()
                        }
                    }

                    2 -> {
                        if (true) {
                            this.state = 66
                            match(DIGIT) as Token
                        }
                    }
                }
            }
        } catch (re: RecognitionException) {
            _localctx.exception = re
            errorHandler.reportError(this, re)
            errorHandler.recover(this, re)
        } finally {
            exitRule()
        }
        return _localctx
    }

    open class SearchContext : ParserRuleContext {
        override var ruleIndex: Int
            get() = Rules.RULE_search.id
            set(value) {
                throw RuntimeException()
            }

        fun findSearchparameter(): List<SearchparameterContext> =
            getRuleContexts(solver.getType("SearchparameterContext"))

        fun findSearchparameter(i: Int): SearchparameterContext? =
            getRuleContext(solver.getType("SearchparameterContext"), i)

        constructor(parent: ParserRuleContext?, invokingState: Int) : super(parent, invokingState) {
        }

        override fun enterRule(listener: ParseTreeListener) {
            if (listener is DIDUrlAbnfListener) (listener as DIDUrlAbnfListener).enterSearch(this)
        }

        override fun exitRule(listener: ParseTreeListener) {
            if (listener is DIDUrlAbnfListener) (listener as DIDUrlAbnfListener).exitSearch(this)
        }
    }

    fun search(): SearchContext {
        var _localctx: SearchContext = SearchContext(context, state)
        enterRule(_localctx, 14, Rules.RULE_search.id)
        var _la: Int
        try {
            enterOuterAlt(_localctx, 1)
            if (true) {
                this.state = 69
                searchparameter()
                this.state = 74
                errorHandler.sync(this);
                _la = _input!!.LA(1)
                while (_la == T__3) {
                    if (true) {
                        if (true) {
                            this.state = 70
                            match(T__3) as Token
                            this.state = 71
                            searchparameter()
                        }
                    }
                    this.state = 76
                    errorHandler.sync(this)
                    _la = _input!!.LA(1)
                }
            }
        } catch (re: RecognitionException) {
            _localctx.exception = re
            errorHandler.reportError(this, re)
            errorHandler.recover(this, re)
        } finally {
            exitRule()
        }
        return _localctx
    }

    open class SearchparameterContext : ParserRuleContext {
        override var ruleIndex: Int
            get() = Rules.RULE_searchparameter.id
            set(value) {
                throw RuntimeException()
            }

        fun findString(): List<StringContext> = getRuleContexts(solver.getType("StringContext"))
        fun findString(i: Int): StringContext? = getRuleContext(solver.getType("StringContext"), i)
        fun DIGIT(): TerminalNode? = getToken(DIDUrlAbnfParser.Tokens.DIGIT.id, 0)
        fun HEX(): TerminalNode? = getToken(DIDUrlAbnfParser.Tokens.HEX.id, 0)

        constructor(parent: ParserRuleContext?, invokingState: Int) : super(parent, invokingState) {
        }

        override fun enterRule(listener: ParseTreeListener) {
            if (listener is DIDUrlAbnfListener) (listener as DIDUrlAbnfListener).enterSearchparameter(this)
        }

        override fun exitRule(listener: ParseTreeListener) {
            if (listener is DIDUrlAbnfListener) (listener as DIDUrlAbnfListener).exitSearchparameter(this)
        }
    }

    fun searchparameter(): SearchparameterContext {
        var _localctx: SearchparameterContext = SearchparameterContext(context, state)
        enterRule(_localctx, 16, Rules.RULE_searchparameter.id)
        var _la: Int
        try {
            enterOuterAlt(_localctx, 1)
            if (true) {
                this.state = 77
                string()
                this.state = 84
                errorHandler.sync(this)
                _la = _input!!.LA(1)
                if (_la == T__4) {
                    if (true) {
                        this.state = 78
                        match(T__4) as Token
                        this.state = 82
                        errorHandler.sync(this)
                        when (interpreter!!.adaptivePredict(_input!!, 9, context)) {
                            1 -> {
                                if (true) {
                                    this.state = 79
                                    string()
                                }
                            }

                            2 -> {
                                if (true) {
                                    this.state = 80
                                    match(DIGIT) as Token
                                }
                            }

                            3 -> {
                                if (true) {
                                    this.state = 81
                                    match(HEX) as Token
                                }
                            }
                        }
                    }
                }

            }
        } catch (re: RecognitionException) {
            _localctx.exception = re
            errorHandler.reportError(this, re)
            errorHandler.recover(this, re)
        } finally {
            exitRule()
        }
        return _localctx
    }

    open class StringContext : ParserRuleContext {
        override var ruleIndex: Int
            get() = Rules.RULE_string.id
            set(value) {
                throw RuntimeException()
            }

        fun STRING(): TerminalNode? = getToken(DIDUrlAbnfParser.Tokens.STRING.id, 0)
        fun DIGIT(): TerminalNode? = getToken(DIDUrlAbnfParser.Tokens.DIGIT.id, 0)

        constructor(parent: ParserRuleContext?, invokingState: Int) : super(parent, invokingState) {
        }

        override fun enterRule(listener: ParseTreeListener) {
            if (listener is DIDUrlAbnfListener) (listener as DIDUrlAbnfListener).enterString(this)
        }

        override fun exitRule(listener: ParseTreeListener) {
            if (listener is DIDUrlAbnfListener) (listener as DIDUrlAbnfListener).exitString(this)
        }
    }

    fun string(): StringContext {
        var _localctx: StringContext = StringContext(context, state)
        enterRule(_localctx, 18, Rules.RULE_string.id)
        var _la: Int
        try {
            enterOuterAlt(_localctx, 1)
            if (true) {
                this.state = 86
                _la = _input!!.LA(1)
                if (!(_la == DIGIT || _la == STRING)) {
                    errorHandler.recoverInline(this)
                } else {
                    if (_input!!.LA(1) == Tokens.EOF.id) isMatchedEOF = true
                    errorHandler.reportMatch(this)
                    consume()
                }
            }
        } catch (re: RecognitionException) {
            _localctx.exception = re
            errorHandler.reportError(this, re)
            errorHandler.recover(this, re)
        } finally {
            exitRule()
        }
        return _localctx
    }

}