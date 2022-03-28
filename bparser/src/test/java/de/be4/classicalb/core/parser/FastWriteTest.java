package de.be4.classicalb.core.parser;

import java.io.PrintWriter;
import java.io.StringWriter;

import de.prob.prolog.output.StructuredPrologOutput;
import de.prob.prolog.term.PrologTerm;

import org.junit.Assert;
import org.junit.Test;

public class FastWriteTest {
	private StructuredPrologOutput spo = new StructuredPrologOutput();
	
	@Test
	public void testSingleNumber() {
		spo.printNumber(42);
		spo.fullstop();
		String expected = "DI42\0";
		check(expected);
	}


	@Test
	public void testSingleAtom1() {
		spo.openTerm("a").closeTerm();
		spo.fullstop();
		String expected = "DAa\0";
		check(expected);
	}

	@Test
	public void testSingleAtom3() {
		spo.openTerm("C").closeTerm();
		spo.fullstop();
		String expected = "DAC\0";
		check(expected);
	}

	@Test
	public void testSimpleFunctor2() {
		spo.openTerm("C").printAtom("a").closeTerm();
		spo.fullstop();
		String expected ="DSC\0\1Aa\0"; 
		check(expected);
	}

	@Test
	public void testSingleVariable() {
		spo.printVariable("Foo");
		spo.fullstop();
		String expected = "D_0\0";
		check(expected);
	}

	@Test
	public void testSimpleFunctor() { // a(b)
		spo.openTerm("a").printAtom("b").closeTerm();
		spo.fullstop();
		String expected = "DSa\0\1Ab\0";
		check(expected);
	}

	@Test
	public void testSingleAtom2() {
		spo.printAtom("a");
		spo.fullstop();
		String expected = "DAa\0";
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
		String expected = "D[AC\0]";
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
		String expected = "DSa\0\1[AG\0[Sf\0\1][[[Aw\0]]]";
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
