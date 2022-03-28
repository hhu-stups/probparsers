package de.be4.classicalb.core.parser;

import java.io.PrintWriter;
import java.io.StringWriter;

import de.prob.prolog.output.StructuredPrologOutput;
import de.prob.prolog.term.PrologTerm;

import org.junit.Assert;
import org.junit.Test;

public class FastWriteTest {
	private static final char ZERO = 0;
	private static final char ONE = 1;

	private StructuredPrologOutput spo = new StructuredPrologOutput();
	
	@Test
	public void testSingleNumber() {
		spo.printNumber(42);
		spo.fullstop();
		String expected = "DI42" + ZERO;
		check(expected);
	}


	@Test
	public void testSingleAtom1() {
		spo.openTerm("a").closeTerm();
		spo.fullstop();
		String expected = "DAa" + ZERO;
		check(expected);
	}

	@Test
	public void testSingleAtom3() {
		spo.openTerm("C").closeTerm();
		spo.fullstop();
		String expected = "DAC" + ZERO;
		check(expected);
	}

	@Test
	public void testSimpleFunctor2() {
		spo.openTerm("C").printAtom("a").closeTerm();
		spo.fullstop();
		String expected ="DSC" + ZERO+(char)1+"Aa"+ZERO; 
		check(expected);
	}

	@Test
	public void testSingleVariable() {
		spo.printVariable("Foo");
		spo.fullstop();
		String expected = "D_0" + ZERO;
		check(expected);
	}

	@Test
	public void testSimpleFunctor() { // a(b)
		spo.openTerm("a").printAtom("b").closeTerm();
		spo.fullstop();
		String expected = "DSa" + ZERO + ONE + "Ab" + ZERO;
		check(expected);
	}

	@Test
	public void testSingleAtom2() {
		spo.printAtom("a");
		spo.fullstop();
		String expected = "DAa" + ZERO;
		check(expected);
	}

	@Test
	public void testEmptyList() {
		spo.openList().closeList();
		spo.fullstop();
		String expected = "D]";
		check(expected);
	}

	@Test
	public void testOneAtomList() {
		spo.openList().printAtom("C").closeList();
		spo.fullstop();
		String expected = "D[AC" + ZERO + "]";
		check(expected);
	}

	@Test
	public void testComplex() { // a(['G',f([]),[[w]]]).
		spo.openTerm("a").openList();
		spo.printAtom("G");
		spo.openTerm("f").openList().closeList().closeTerm();
		spo.openList().openList().printAtom("w").closeList().closeList();
		spo.closeList().closeTerm();
		spo.fullstop();
		String expected = "DSa" + ZERO + ONE + "[AG" + ZERO + "[Sf" + ZERO + ONE + "][[[Aw" + ZERO + "]]]";
		check(expected);
	}
	
	private void check(String expected) {
		assert spo.getSentences().size() == 1;
		final PrologTerm term = spo.getSentences().get(0);
		final StringWriter sw = new StringWriter();
		try (final PrintWriter pw = new PrintWriter(sw)) {
			new FastReadWriter(pw).fastwrite(term);
		}
		Assert.assertEquals(expected, sw.toString());
	}

}
