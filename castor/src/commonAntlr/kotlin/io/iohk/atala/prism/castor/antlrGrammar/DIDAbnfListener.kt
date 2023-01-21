// Generated from java-escape by ANTLR 4.7.1
package io.iohk.atala.prism.castor.antlrGrammar

import org.antlr.v4.kotlinruntime.tree.ParseTreeListener

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link DIDAbnfParser}.
 */
interface DIDAbnfListener : ParseTreeListener {
    /**
     * Enter a parse tree produced by {@link DIDAbnfParser#did}.
     * @param ctx the parse tree
     */
    fun enterDid(ctx: DIDAbnfParser.DidContext)

    /**
     * Exit a parse tree produced by {@link DIDAbnfParser#did}.
     * @param ctx the parse tree
     */
    fun exitDid(ctx: DIDAbnfParser.DidContext)

    /**
     * Enter a parse tree produced by {@link DIDAbnfParser#method_name}.
     * @param ctx the parse tree
     */
    fun enterMethod_name(ctx: DIDAbnfParser.Method_nameContext)

    /**
     * Exit a parse tree produced by {@link DIDAbnfParser#method_name}.
     * @param ctx the parse tree
     */
    fun exitMethod_name(ctx: DIDAbnfParser.Method_nameContext)

    /**
     * Enter a parse tree produced by {@link DIDAbnfParser#method_specific_id}.
     * @param ctx the parse tree
     */
    fun enterMethod_specific_id(ctx: DIDAbnfParser.Method_specific_idContext)

    /**
     * Exit a parse tree produced by {@link DIDAbnfParser#method_specific_id}.
     * @param ctx the parse tree
     */
    fun exitMethod_specific_id(ctx: DIDAbnfParser.Method_specific_idContext)

    /**
     * Enter a parse tree produced by {@link DIDAbnfParser#idchar}.
     * @param ctx the parse tree
     */
    fun enterIdchar(ctx: DIDAbnfParser.IdcharContext)

    /**
     * Exit a parse tree produced by {@link DIDAbnfParser#idchar}.
     * @param ctx the parse tree
     */
    fun exitIdchar(ctx: DIDAbnfParser.IdcharContext)
}