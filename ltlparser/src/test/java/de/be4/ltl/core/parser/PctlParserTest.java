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

	@Test
	public void testSink() throws Exception {
		check("sink", "ap(sink)");
	}

	@Test
	public void testDeadlock() throws Exception {
		check("deadlock", "ap(deadlock)");
	}

	@Test
	public void testCurrent() throws Exception {
		check("current", "ap(stateid(root))");
	}

	@Test
	public void testAnd() throws Exception {
		check("true &  false", "and(true,false)");
	}

	@Test
	public void testOr() throws Exception {
		check("true or  false", "or(true,false)");
	}

	@Test
	public void testNot() throws Exception {
		check("not true", "not(true)");
	}

	@Test
	public void testPredicate1() throws Exception {
		check("{}", "");
	}

	@Test
	public void testPredicate() throws Exception {
		check("{blubb}", "ap(dpred(blubb))");
	}
/*
	@Test
	public void testNextLoop() throws Exception {
		check("P={0.9} [X {p(b)}]", "formula_equal(0.9,x(p(b)))");
	}*/

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
