package io.iohk.atala.prism.castor.antlrGrammar

import org.antlr.v4.kotlinruntime.tree.ParseTreeListener

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link DIDUrlAbnfParser}.
 */
interface DIDUrlAbnfListener : ParseTreeListener {
    /**
     * Enter a parse tree produced by {@link DIDUrlAbnfParser#did_url}.
     * @param ctx the parse tree
     */
    fun enterDid_url(ctx: DIDUrlAbnfParser.Did_urlContext)

    /**
     * Exit a parse tree produced by {@link DIDUrlAbnfParser#did_url}.
     * @param ctx the parse tree
     */
    fun exitDid_url(ctx: DIDUrlAbnfParser.Did_urlContext)

    /**
     * Enter a parse tree produced by {@link DIDUrlAbnfParser#did}.
     * @param ctx the parse tree
     */
    fun enterDid(ctx: DIDUrlAbnfParser.DidContext)

    /**
     * Exit a parse tree produced by {@link DIDUrlAbnfParser#did}.
     * @param ctx the parse tree
     */
    fun exitDid(ctx: DIDUrlAbnfParser.DidContext)

    /**
     * Enter a parse tree produced by {@link DIDUrlAbnfParser#method_name}.
     * @param ctx the parse tree
     */
    fun enterMethod_name(ctx: DIDUrlAbnfParser.Method_nameContext)

    /**
     * Exit a parse tree produced by {@link DIDUrlAbnfParser#method_name}.
     * @param ctx the parse tree
     */
    fun exitMethod_name(ctx: DIDUrlAbnfParser.Method_nameContext)

    /**
     * Enter a parse tree produced by {@link DIDUrlAbnfParser#method_specific_id}.
     * @param ctx the parse tree
     */
    fun enterMethod_specific_id(ctx: DIDUrlAbnfParser.Method_specific_idContext)

    /**
     * Exit a parse tree produced by {@link DIDUrlAbnfParser#method_specific_id}.
     * @param ctx the parse tree
     */
    fun exitMethod_specific_id(ctx: DIDUrlAbnfParser.Method_specific_idContext)

    /**
     * Enter a parse tree produced by {@link DIDUrlAbnfParser#path}.
     * @param ctx the parse tree
     */
    fun enterPath(ctx: DIDUrlAbnfParser.PathContext)

    /**
     * Exit a parse tree produced by {@link DIDUrlAbnfParser#path}.
     * @param ctx the parse tree
     */
    fun exitPath(ctx: DIDUrlAbnfParser.PathContext)

    /**
     * Enter a parse tree produced by {@link DIDUrlAbnfParser#query}.
     * @param ctx the parse tree
     */
    fun enterQuery(ctx: DIDUrlAbnfParser.QueryContext)

    /**
     * Exit a parse tree produced by {@link DIDUrlAbnfParser#query}.
     * @param ctx the parse tree
     */
    fun exitQuery(ctx: DIDUrlAbnfParser.QueryContext)

    /**
     * Enter a parse tree produced by {@link DIDUrlAbnfParser#frag}.
     * @param ctx the parse tree
     */
    fun enterFrag(ctx: DIDUrlAbnfParser.FragContext)

    /**
     * Exit a parse tree produced by {@link DIDUrlAbnfParser#frag}.
     * @param ctx the parse tree
     */
    fun exitFrag(ctx: DIDUrlAbnfParser.FragContext)

    /**
     * Enter a parse tree produced by {@link DIDUrlAbnfParser#search}.
     * @param ctx the parse tree
     */
    fun enterSearch(ctx: DIDUrlAbnfParser.SearchContext)

    /**
     * Exit a parse tree produced by {@link DIDUrlAbnfParser#search}.
     * @param ctx the parse tree
     */
    fun exitSearch(ctx: DIDUrlAbnfParser.SearchContext)

    /**
     * Enter a parse tree produced by {@link DIDUrlAbnfParser#searchparameter}.
     * @param ctx the parse tree
     */
    fun enterSearchparameter(ctx: DIDUrlAbnfParser.SearchparameterContext)

    /**
     * Exit a parse tree produced by {@link DIDUrlAbnfParser#searchparameter}.
     * @param ctx the parse tree
     */
    fun exitSearchparameter(ctx: DIDUrlAbnfParser.SearchparameterContext)

    /**
     * Enter a parse tree produced by {@link DIDUrlAbnfParser#string}.
     * @param ctx the parse tree
     */
    fun enterString(ctx: DIDUrlAbnfParser.StringContext)

    /**
     * Exit a parse tree produced by {@link DIDUrlAbnfParser#string}.
     * @param ctx the parse tree
     */
    fun exitString(ctx: DIDUrlAbnfParser.StringContext)
}
