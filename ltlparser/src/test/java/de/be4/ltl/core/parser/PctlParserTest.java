package de.be4.ltl.core.parser;

import de.prob.prolog.output.PrologTermStringOutput;

import org.junit.Assert;
import org.junit.Test;

public class PctlParserTest {

	public void testTrue() throws Exception {
		check("true", "true");
	}

	public void testFalse() throws Exception {
		check("false", "false");
	}

	public void testImplication() throws Exception {
		check("false =>   true ", "implies(false,true)");
	}

	public void testSink() throws Exception {
		check("sink", "ap(sink)");
	}

	public void testDeadlock() throws Exception {
		check("deadlock", "ap(deadlock)");
	}

	public void testCurrent() throws Exception {
		check("current", "ap(stateid(root))");
	}

	public void testAnd() throws Exception {
		check("true &  false", "and(true,false)");
	}

	public void testOr() throws Exception {
		check("true or  false", "or(true,false)");
	}

	public void testNot() throws Exception {
		check("not true", "not(true)");
	}

	public void testPredicate() throws Exception {
		check("{blubb}", "ap(dpred(blubb))");
	}

	@Test
	public void testNextLoop() throws Exception {
		check("P<{0.8}[{banana} # U @ (true)]", "formulastrictlyless(ap(dpred(0.8),u(ap(dpred(banana)),true))");
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
