package de.be4.ltl.core.parser;

import java.io.IOException;

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

	protected abstract T parseFormula(String formula) throws LtlParseException,
			IOException;

	protected abstract void applyPrologGenerator(IPrologTermOutput pto,
			String stateID, ProBParserBase specParser2, T ast);

	public PrologTerm generatePrologTerm(final String formula,
			final String stateID) throws LtlParseException {
		T ast;
		try {
			ast = parseFormula(formula);
		} catch (IOException e) {
			String msg = "IOException during parsing of formula (possibly pushback buffer overflow in Lexer): " + e;
			throw new IllegalStateException(msg);
		}
		StructuredPrologOutput pto = new StructuredPrologOutput();
		try {
			applyPrologGenerator(pto, stateID, this.getSpecParser(), ast);
		} catch (LtlAdapterException e) {
			throw e.getOriginalException();
		}
		pto.fullstop();
		return pto.getSentences().iterator().next();
	}

}
