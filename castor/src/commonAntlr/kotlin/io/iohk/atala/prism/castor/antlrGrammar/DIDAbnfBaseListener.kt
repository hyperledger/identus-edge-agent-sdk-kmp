// Generated from java-escape by ANTLR 4.7.1
package io.iohk.atala.prism.castor.antlrGrammar

import org.antlr.v4.kotlinruntime.ParserRuleContext
import org.antlr.v4.kotlinruntime.tree.ErrorNode
import org.antlr.v4.kotlinruntime.tree.TerminalNode

/**
 * This class provides an empty implementation of {@link DIDAbnfListener},
 * which can be extended to create a listener which only needs to handle a subset
 * of the available methods.
 */
open class DIDAbnfBaseListener : DIDAbnfListener {
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    override fun enterDid(ctx: DIDAbnfParser.DidContext) {}

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    override fun exitDid(ctx: DIDAbnfParser.DidContext) {}

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    override fun enterMethod_name(ctx: DIDAbnfParser.Method_nameContext) {}

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    override fun exitMethod_name(ctx: DIDAbnfParser.Method_nameContext) {}

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    override fun enterMethod_specific_id(ctx: DIDAbnfParser.Method_specific_idContext) {}

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    override fun exitMethod_specific_id(ctx: DIDAbnfParser.Method_specific_idContext) {}

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    override fun enterIdchar(ctx: DIDAbnfParser.IdcharContext) {}

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    override fun exitIdchar(ctx: DIDAbnfParser.IdcharContext) {}

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    override fun enterEveryRule(ctx: ParserRuleContext) {}

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    override fun exitEveryRule(ctx: ParserRuleContext) {}

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    override fun visitTerminal(node: TerminalNode) {}

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    override fun visitErrorNode(node: ErrorNode) {}
}