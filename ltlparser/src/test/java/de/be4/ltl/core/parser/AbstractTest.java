package de.be4.ltl.core.parser;

import de.prob.prolog.term.PrologTerm;

import org.junit.Assert;

public class AbstractTest {
	protected void check(final String input, final PrologTerm expectedTerm)
			throws LtlParseException {
		final PrologTerm term = parse(input);
		Assert.assertEquals(expectedTerm, term);
	}

	protected PrologTerm parse(final String input) throws LtlParseException {
		final LtlParser parser = new LtlParser(new DummyParser(true, true));
		return parser.generatePrologTerm(input, "root");
	}
}
