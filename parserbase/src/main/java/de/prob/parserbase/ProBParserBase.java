package de.prob.parserbase;

import de.prob.prolog.output.IPrologTermOutput;

/**
 * This interface describes the basic functionality of parsers for specific
 * formalisms.
 */
public interface ProBParserBase {
	/**
	 * Parses a string that should contain an expression.
	 * 
	 * @param pto
	 *            The PrologTermOutput where the result should be written t
	 * @param expression
	 *            The expression as string
	 * @param wrap
	 *            whether the Prolog representation should be wrapped by a term
	 *            that indicates the type of formalism.
	 * @throws ProBParseException
	 *             in case of a parse error (e.g. syntax or lexer errors)
	 * @throws UnsupportedOperationException
	 *             if the formalism does not support expressions
	 */
	void parseExpression(IPrologTermOutput pto, String expression, boolean wrap)
			throws ProBParseException;

	/**
	 * Parses a string that should contain an expression.
	 * 
	 * @param pto the PrologTermOutput where the result should be written to
	 * @param expression the expression as string
	 * @param wrap whether the Prolog representation should be wrapped by a term that indicates the type of formalism.
	 * @param startLine the line number (1-based) on which this expression starts
	 * @param startColumn the column number (1-based) at which this expression starts
	 * @throws ProBParseException in case of a parse error (e.g. syntax or lexer errors)
	 * @throws UnsupportedOperationException if the formalism does not support expressions
	 */
	default void parseExpression(IPrologTermOutput pto, String expression, boolean wrap, int startLine, int startColumn) throws ProBParseException {
		this.parseExpression(pto, expression, wrap);
	}

	/**
	 * Parses a string that should contain a predicate.
	 * 
	 * @param pto
	 *            The PrologTermOutput where the result should be written t
	 * @param predicate
	 *            The predicate as string
	 * @param wrap
	 *            whether the Prolog representation should be wrapped by a term
	 *            that indicates the type of formalism.
	 * @throws ProBParseException
	 *             in case of a parse error (e.g. syntax or lexer errors)
	 * @throws UnsupportedOperationException
	 *             if the formalism does not support predicates
	 */
	void parsePredicate(IPrologTermOutput pto, String predicate, boolean wrap)
			throws ProBParseException;

	/**
	 * Parses a string that should contain a predicate.
	 * 
	 * @param pto the PrologTermOutput where the result should be written to
	 * @param predicate the predicate as string
	 * @param wrap whether the Prolog representation should be wrapped by a term that indicates the type of formalism.
	 * @param startLine the line number (1-based) on which this expression starts
	 * @param startColumn the column number (1-based) at which this expression starts
	 * @throws ProBParseException in case of a parse error (e.g. syntax or lexer errors)
	 * @throws UnsupportedOperationException if the formalism does not support predicates
	 */
	default void parsePredicate(IPrologTermOutput pto, String predicate, boolean wrap, int startLine, int startColumn) throws ProBParseException {
		this.parsePredicate(pto, predicate, wrap);
	}

	/**
	 * Parses a string that should contain a predicate over a transition between
	 * two states.
	 * 
	 * @param pto
	 *            The PrologTermOutput where the result should be written t
	 * @param transPredicate
	 *            The transition predicate as string
	 * @param wrap
	 *            whether the Prolog representation should be wrapped by a term
	 *            that indicates the type of formalism.
	 * @throws ProBParseException
	 *             in case of a parse error (e.g. syntax or lexer errors)
	 * @throws UnsupportedOperationException
	 *             if the formalism does not support predicates on transitions
	 */
	void parseTransitionPredicate(IPrologTermOutput pto, String transPredicate,
			boolean wrap) throws ProBParseException;

	/**
	 * Parses a string that should contain a predicate over a transition between two states.
	 * 
	 * @param pto the PrologTermOutput where the result should be written to
	 * @param transPredicate the transition predicate as string
	 * @param wrap whether the Prolog representation should be wrapped by a term that indicates the type of formalism.
	 * @throws ProBParseException in case of a parse error (e.g. syntax or lexer errors)
	 * @throws UnsupportedOperationException if the formalism does not support predicates on transitions
	 */
	default void parseTransitionPredicate(IPrologTermOutput pto, String transPredicate, boolean wrap, int startLine, int startColumn) throws ProBParseException {
		this.parseTransitionPredicate(pto, transPredicate, wrap);
	}
}
