package de.be4.classicalb.core.parser;

import de.be4.classicalb.core.parser.analysis.prolog.ASTProlog;
import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.node.Start;
import de.prob.parserbase.ProBParseException;
import de.prob.parserbase.ProBParserBase;
import de.prob.prolog.output.IPrologTermOutput;

public class ClassicalBParser implements ProBParserBase {
	private static final String WRAPPER_EXPR = "bexpr";
	private static final String WRAPPER_PRED = "bpred";
	private static final String WRAPPER_TRANS = "bop";
	
	private final BParser bparser;
	
	public ClassicalBParser() {
		this(new BParser());
	}

	public ClassicalBParser(final BParser bparser) {
		this.bparser = bparser;
	}

	@Override
	public void parseExpression(final IPrologTermOutput pto, final String expression, final boolean wrap) throws ProBParseException {
		this.parseExpression(pto, expression, wrap, 1, 1);
	}

	@Override
	public void parseExpression(final IPrologTermOutput pto, final String expression, final boolean wrap, int startLine, int startColumn)
			throws ProBParseException {
		bparser.setStartPosition(startLine, startColumn);
		try {
			Start ast = bparser.parseExpression(expression);
			printAst(pto, ast, wrap, WRAPPER_EXPR);
		} catch (BCompoundException e) {
			throw new ProBParseException(e.getFirstException().getMessage(), e);
		}
	}

	@Override
	public void parsePredicate(final IPrologTermOutput pto, final String predicate, final boolean wrap) throws ProBParseException {
		this.parsePredicate(pto, predicate, wrap, 1, 1);
	}

	@Override
	public void parsePredicate(final IPrologTermOutput pto, final String predicate, final boolean wrap, int startLine, int startColumn)
			throws ProBParseException {
		bparser.setStartPosition(startLine, startColumn);
		try {
			Start ast = bparser.parsePredicate(predicate);
			printAst(pto, ast, wrap, WRAPPER_PRED);
		} catch (BCompoundException e) {
			throw new ProBParseException(e.getFirstException().getMessage(), e);
		}
	}

	@Override
	public void parseTransitionPredicate(final IPrologTermOutput pto, final String transPredicate, final boolean wrap) throws ProBParseException {
		this.parseTransitionPredicate(pto, transPredicate, wrap, 1, 1);
	}

	@Override
	public void parseTransitionPredicate(final IPrologTermOutput pto, final String trans, final boolean wrap, int startLine, int startColumn)
			throws ProBParseException {
		bparser.setStartPosition(startLine, startColumn);
		try {
			Start ast = bparser.parseTransition(trans);
			printAst(pto, ast, wrap, WRAPPER_TRANS);
		} catch (BCompoundException e) {
			throw new ProBParseException(e.getFirstException().getMessage(), e);
		}
	}

	private void printAst(final IPrologTermOutput pto, Start ast, final boolean wrap, final String wrapper) {
		if (wrap) {
			pto.openTerm(wrapper);
		}
		ASTProlog.printFormula(ast, pto);
		if (wrap) {
			pto.closeTerm();
		}
	}

}
