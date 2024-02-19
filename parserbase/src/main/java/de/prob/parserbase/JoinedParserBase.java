package de.prob.parserbase;

import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.PrologTerm;

public class JoinedParserBase implements ProBParserBase {
	private final ProBParserBaseAdapter[] parsers;

	private enum Type {
		EXPR, PRED, TRANS
	}

	public JoinedParserBase(final ProBParserBase[] parsers) {
		if (parsers.length == 0)
			throw new IllegalArgumentException(
					"There should be at least one parser");
		this.parsers = new ProBParserBaseAdapter[parsers.length];
		for (int i = 0; i < parsers.length; i++) {
			this.parsers[i] = new ProBParserBaseAdapter(parsers[i]);
		}
	}

	@Override
	public void parseExpression(final IPrologTermOutput pto,
			final String expression, final boolean wrap)
			throws ProBParseException {
		parse(Type.EXPR, pto, expression, wrap);
	}

	@Override
	public void parsePredicate(final IPrologTermOutput pto,
			final String predicate, final boolean wrap)
			throws ProBParseException {
		parse(Type.PRED, pto, predicate, wrap);
	}

	@Override
	public void parseTransitionPredicate(final IPrologTermOutput pto,
			final String trans, final boolean wrap) throws ProBParseException {
		parse(Type.TRANS, pto, trans, wrap);
	}

	private void parse(final Type type, final IPrologTermOutput pto,
			final String formula, final boolean wrap) throws ProBParseException {
		ProBParseException parseException = null;
		UnsupportedOperationException unsupportedException = null;
		for (final ProBParserBaseAdapter parser : parsers) {
			try {
				final PrologTerm term;
				switch (type) {
				case EXPR:
					term = parser.parseExpression(formula, wrap);
					break;
				case PRED:
					term = parser.parsePredicate(formula, wrap);
					break;
				case TRANS:
					term = parser.parseTransitionPredicate(formula, wrap);
					break;
				default:
					throw new IllegalStateException();
				}
				pto.printTerm(term);
				return;
			} catch (ProBParseException e) {
				if (parseException == null) {
					parseException = e;
				}
			} catch (UnsupportedOperationException e) {
				if (unsupportedException == e) {
					unsupportedException = e;
				}
			}
		}
		if (parseException != null)
			throw parseException;
		else
			throw unsupportedException;
	}

}
