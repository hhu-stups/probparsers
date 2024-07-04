package de.prob.parser;

import de.prob.core.sablecc.node.AYesResult;
import de.prob.core.sablecc.node.Start;
import de.prob.prolog.output.PrologTermStringOutput;
import de.prob.prolog.term.PrologTerm;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RoundtripTest {
	private static String roundtrip(String source) {
		Start ast = ProBResultParser.parse(source);
		PrologTerm parsedTerm = PrologTermGenerator.toPrologTerm(ast);
		assertTrue(ast.getPResult() instanceof AYesResult);
		PrologTermStringOutput pto = new PrologTermStringOutput();
		pto.term("yes", t1 -> t1.printTerm(parsedTerm));
		return pto.toString();
	}

	private static void testRoundtrip(String source) {
		assertEquals(source, roundtrip(source));
	}

	@Test
	public void testAtom() {
		testRoundtrip("yes(a)");
	}

	@Test
	public void testAtomUppercase() {
		testRoundtrip("yes('Foo')");
	}

	@Test(expected = ResultParserException.class)
	public void testString() {
		testRoundtrip("yes(\"foo\")");
	}

	@Test
	public void testVariable() {
		testRoundtrip("yes(X)");
	}

	@Test
	public void testInt() {
		testRoundtrip("yes(42)");
	}

	@Test
	public void testEmptyList() {
		testRoundtrip("yes([])");
	}

	@Test
	public void testList() {
		testRoundtrip("yes([a,'Foo',X,42,[],[a,'Foo',X,42,g(h(i)),[]]])");
	}

	@Test
	public void testDotList1() {
		assertEquals("yes([a])", roundtrip("yes('.'(a,[]))"));
	}

	@Test
	public void testDotListComplicated() {
		assertEquals(
			"yes([a,'Foo',X,42,[],[a,'Foo',X,42,g(h(i)),[]]])",
			roundtrip("yes('.'(a,'.'('Foo','.'(X,'.'(42,'.'([],'.'('.'(a,'.'('Foo','.'(X,'.'(42,'.'(g(h(i)),'.'([],[])))))),[])))))))")
		);
	}

	@Test
	public void testTerm() {
		testRoundtrip("yes(f(a,'Foo',X,42,[],[a,'Foo',X,42,g(h(i)),[]]))");
	}
}
