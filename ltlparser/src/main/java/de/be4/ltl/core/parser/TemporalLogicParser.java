package de.be4.ltl.core.parser;

import de.be4.ltl.core.parser.internal.LtlAdapterException;
import de.prob.parserbase.ProBParserBase;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.output.StructuredPrologOutput;
import de.prob.prolog.term.PrologTerm;

public abstract class TemporalLogicParser<T> {
	/**
	 * @deprecated This field will become private.
	 *     External users should call {@link #getSpecParser()} instead.
	 */
	@Deprecated
	public final ProBParserBase specParser;

	protected TemporalLogicParser(final ProBParserBase specParser) {
		this.specParser = specParser;
	}

	public ProBParserBase getSpecParser() {
		return specParser;
	}

	protected abstract T parseFormula(String formula) throws LtlParseException;

	protected abstract void applyPrologGenerator(IPrologTermOutput pto, String stateID, T ast);

	public PrologTerm generatePrologTerm(final String formula,
			final String stateID) throws LtlParseException {
		T ast = parseFormula(formula);
		StructuredPrologOutput pto = new StructuredPrologOutput();
		try {
			applyPrologGenerator(pto, stateID, ast);
		} catch (LtlAdapterException e) {
			throw e.getOriginalException();
		}
		pto.fullstop();
		return pto.getSentences().iterator().next();
	}

}
