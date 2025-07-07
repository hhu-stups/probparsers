package de.be4.ltl.core.parser;

import de.prob.prolog.output.PrologTermStringOutput;

import org.junit.Assert;
import org.junit.Test;

public class PctlParserTest {

	@Test
	public void testTrue() throws Exception {
		check("true", "true");
	}
	
	@Test
	public void testFalse() throws Exception {
		check("false", "false");
	}

	@Test
	public void testImplication() throws Exception {
		check("false =>   true ", "implies(false,true)");
	}

	private static void check(String input, String expectedTerm) throws LtlParseException {
		String term = parse(input);
		Assert.assertEquals(expectedTerm, term);
	}

	private static String parse(String input) throws LtlParseException {
		final PctlParser parser = new PctlParser(new DummyParser(true, true));
		PrologTermStringOutput pto = new PrologTermStringOutput();
		parser.printFormulaAsProlog(input, "root", pto);
		return pto.toString();
	}
}
