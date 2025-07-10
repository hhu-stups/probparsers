package de.be4.ltl.core.parser;

import de.prob.parserbase.ProBParseException;
import de.prob.parserbase.ProBParserBase;
import de.prob.prolog.output.IPrologTermOutput;

class DummyParser implements ProBParserBase {
	private final boolean suppPred, suppTransPred, suppExpr;

	DummyParser(final boolean suppPred, final boolean suppTransPred, final boolean suppExpr) {
		this.suppPred = suppPred;
		this.suppTransPred = suppTransPred;
		this.suppExpr = suppExpr;
	}

	@Override
	public void parseExpression(final IPrologTermOutput pto, final String expression, final boolean wrap) throws ProBParseException {
		if (suppExpr) {
			parse(pto, expression, wrap, null);
		} else {
			throw new UnsupportedOperationException("no dummy expressions");
		}
	}

	@Override
	public void parsePredicate(final IPrologTermOutput pto, final String predicate, final boolean wrap) throws ProBParseException {
		if (suppPred) {
			parse(pto, predicate, wrap, "dpred");
		} else {
			throw new UnsupportedOperationException("no dummy predicates");
		}
	}

	@Override
	public void parseTransitionPredicate(final IPrologTermOutput pto, final String transPredicate, final boolean wrap) throws ProBParseException {
		if (suppTransPred) {
			parse(pto, transPredicate, wrap, "dtrans");
		} else {
			throw new UnsupportedOperationException("no dummy transition predicates");
		}
	}

	private static void parse(final IPrologTermOutput pto, final String text, final boolean wrap, final String wrapper) throws ProBParseException {
		// TODO: cant we use the real B parser here?
		// Hardcoded cases for testing parse errors.
		// Everything else is considered "successfully parsed".
		if ("X".equals(text) || text.endsWith("{")) {
			throw new ProBParseException("syntax error");
		}
		System.out.println(pto.toString());
		if (wrap) {
			pto.openTerm(wrapper);
		}
		pto.printAtom(text);
		if (wrap) {
			pto.closeTerm();
		}
		
	}
}
