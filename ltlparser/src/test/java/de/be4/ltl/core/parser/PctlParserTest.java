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
	public void testPredicate() throws Exception {
		check("{blubb}", "ap(dpred(blubb))");
	}

	@Test
	public void testNextLoop1() throws Exception {
		check("P<{blubb}[X true]", "probabilisticformula(strictlyless,ap(dpred(blubb)),x(true))");
	}

	@Test
	public void testNextLoop2() throws Exception {
		check("P={0.9}[X (P={0.5}[X {p(b)}])]",
		"probabilisticformula(equal,ap(dpred('0.9')),x(probabilisticformula(equal,ap(dpred('0.5')),x(ap(dpred('p(b)'))))))");
	}
	
	@Test
	public void testUntilLoop() throws Exception {
		check("P={prob}[not {a} U {b}]",
		"probabilisticformula(equal,ap(dpred(prob)),u(not(ap(dpred(a))),ap(dpred(b))))");
	}

	@Test
	public void testUntilBoundedLoop() throws Exception {
		check("P={prob}[not {a}U<=5 {b}]",
		"probabilisticformula(equal,ap(dpred(prob)),uk(not(ap(dpred(a))),5,ap(dpred(b))))");
	}

	@Test
	public void testAlwaysLoop() throws Exception {
		check("P>={0.1}[G not({b})]",
		"probabilisticformula(greater,ap(dpred('0.1')),g(not(ap(dpred(b)))))");
	}

	@Test
	public void testEventuallyLoop() throws Exception {
		check("P={prob}[F<=5 {b}]",
		"probabilisticformula(equal,ap(dpred(prob)),fk(5,ap(dpred(b))))");
	}

	@Test
	public void testUntilBoundedBig() throws Exception {
		check("P={0.95703125}[F<=5 {elect}]",
		"probabilisticformula(equal,ap(dpred('0.95703125')),fk(5,ap(dpred(elect))))");
	}

	@Test
	public void testUntilBig() throws Exception {
		check("P={1.0}[F ({elect} & false)]",
		"probabilisticformula(equal,ap(dpred('1.0')),f(and(ap(dpred(elect)),false)))");
	}

	@Test
	public void testUntilBig2() throws Exception {
		check("P={1.0}[F {elect} & false]",
		"probabilisticformula(equal,ap(dpred('1.0')),f(and(ap(dpred(elect)),false)))");
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
